package br.com.marwil.ordemservico.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.EnvioCobrancaRequest;
import br.com.marwil.ordemservico.dto.request.PagamentoRequest;
import br.com.marwil.ordemservico.dto.response.CobrancaResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.entity.Cobranca;
import br.com.marwil.ordemservico.entity.LancamentoCaixa;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.entity.Pagamento;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.mapper.CobrancaMapper;
import br.com.marwil.ordemservico.repository.CobrancaRepository;
import br.com.marwil.ordemservico.repository.ItemOrdemServicoRepository;
import br.com.marwil.ordemservico.repository.LancamentoCaixaRepository;
import br.com.marwil.ordemservico.repository.OrdemServicoRepository;
import br.com.marwil.ordemservico.repository.PagamentoRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.repository.specification.CobrancaSpecifications;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;

@Service
public class CobrancaService {

	private final CobrancaRepository cobrancas;
	private final PagamentoRepository pagamentos;
	private final LancamentoCaixaRepository caixa;
	private final OrdemServicoRepository ordens;
	private final ItemOrdemServicoRepository itens;
	private final UsuarioRepository usuarios;
	private final CobrancaMapper mapper;
	private final Clock relogio;

	public CobrancaService(CobrancaRepository cobrancas, PagamentoRepository pagamentos,
			LancamentoCaixaRepository caixa, OrdemServicoRepository ordens, ItemOrdemServicoRepository itens,
			UsuarioRepository usuarios, CobrancaMapper mapper, Clock relogio) {
		this.cobrancas = cobrancas;
		this.pagamentos = pagamentos;
		this.caixa = caixa;
		this.ordens = ordens;
		this.itens = itens;
		this.usuarios = usuarios;
		this.mapper = mapper;
		this.relogio = relogio;
	}

	@Transactional
	public CobrancaResponse enviar(UsuarioAutenticado responsavel, Long ordemServicoId,
			EnvioCobrancaRequest requisicao) {
		OrdemServico ordem = ordens.findComBloqueioById(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (ordem.getStatusAtendimento() != StatusAtendimento.CONCLUIDA) {
			throw new ConflitoException("Somente ordens de serviço concluídas podem ser enviadas à cobrança.");
		}
		if (ordens.findStatusCobranca(ordemServicoId).isPresent()) {
			throw new ConflitoException("A ordem de serviço já foi enviada à cobrança.");
		}
		if (!itens.existsByOrdemServicoId(ordemServicoId)) {
			throw new ConflitoException("Precifique a ordem de serviço antes de enviá-la à cobrança.");
		}
		BigDecimal total = itens.somarValorTotal(ordemServicoId);
		if (total.signum() <= 0) {
			throw new ConflitoException("O valor total da ordem de serviço precisa ser maior que zero para cobrança.");
		}
		LocalDate hoje = hoje();
		LocalDate vencimento = requisicao == null ? null : requisicao.dataVencimento();
		if (vencimento != null && vencimento.isBefore(hoje)) {
			throw new RequisicaoInvalidaException("A data de vencimento não pode estar no passado.");
		}
		Cobranca cobranca = new Cobranca();
		cobranca.setOrdemServico(ordem);
		cobranca.setValorTotal(total);
		cobranca.setStatus(StatusCobranca.PENDENTE);
		cobranca.setDataVencimento(vencimento);
		cobranca.setEnviadaPor(usuarios.getReferenceById(responsavel.id()));
		cobranca.setEnviadaEm(OffsetDateTime.now(relogio));
		return mapper.paraResponse(cobrancas.saveAndFlush(cobranca), hoje);
	}

	@Transactional
	public CobrancaResponse registrarPagamento(UsuarioAutenticado responsavel, Long cobrancaId,
			PagamentoRequest requisicao) {
		Cobranca cobranca = cobrancas.findComBloqueioById(cobrancaId).orElseThrow(CobrancaService::naoEncontrada);
		if (cobranca.getStatus() == StatusCobranca.PAGA) {
			throw new ConflitoException("O pagamento desta cobrança já foi registrado.");
		}
		BigDecimal valorInformado = requisicao == null ? null : requisicao.valorRecebido();
		BigDecimal valorRecebido = valorInformado == null ? cobranca.getValorTotal() : valorInformado;
		if (valorRecebido.compareTo(cobranca.getValorTotal()) != 0) {
			throw new RequisicaoInvalidaException(
					"Somente o pagamento integral de " + cobranca.getValorTotal() + " é aceito nesta versão.");
		}
		LocalDate dataInformada = requisicao == null ? null : requisicao.dataPagamento();
		LocalDate dataPagamento = dataInformada == null ? hoje() : dataInformada;
		validarDataPagamento(cobranca, dataPagamento);
		cobranca.setStatus(StatusCobranca.PAGA);
		cobrancas.saveAndFlush(cobranca);
		Pagamento pagamento = new Pagamento();
		pagamento.setCobranca(cobranca);
		pagamento.setValorRecebido(valorRecebido);
		pagamento.setDataPagamento(dataPagamento);
		pagamento.setRegistradoPor(usuarios.getReferenceById(responsavel.id()));
		pagamento.setRegistradoEm(OffsetDateTime.now(relogio));
		cobranca.setPagamento(pagamentos.saveAndFlush(pagamento));
		LancamentoCaixa lancamento = new LancamentoCaixa();
		lancamento.setPagamento(pagamento);
		lancamento.setValor(valorRecebido);
		lancamento.setDataLancamento(dataPagamento);
		caixa.saveAndFlush(lancamento);
		return mapper.paraResponse(cobranca, hoje());
	}

	@Transactional(readOnly = true)
	public CobrancaResponse buscar(Long id) {
		return mapper.paraResponse(cobrancas.findById(id).orElseThrow(CobrancaService::naoEncontrada), hoje());
	}

	@Transactional(readOnly = true)
	public PaginaResponse<CobrancaResponse> listar(StatusCobranca status, Long clienteId, Long ordemServicoId,
			Boolean vencidas, Pageable paginacao) {
		if (status == StatusCobranca.NAO_ENVIADA) {
			return PaginaResponse.de(Page.empty(paginacao));
		}
		LocalDate hoje = hoje();
		Specification<Cobranca> filtro = Specification.allOf(List.of(
				CobrancaSpecifications.comStatus(status),
				CobrancaSpecifications.doCliente(clienteId),
				CobrancaSpecifications.daOrdem(ordemServicoId),
				CobrancaSpecifications.vencidas(vencidas, hoje)));
		return PaginaResponse.de(cobrancas.findAll(filtro, paginacao)
				.map(cobranca -> mapper.paraResponse(cobranca, hoje)));
	}

	private void validarDataPagamento(Cobranca cobranca, LocalDate data) {
		if (data.isAfter(hoje())) {
			throw new RequisicaoInvalidaException("A data do pagamento não pode estar no futuro.");
		}
		LocalDate envio = cobranca.getEnviadaEm().atZoneSameInstant(relogio.getZone()).toLocalDate();
		if (data.isBefore(envio)) {
			throw new RequisicaoInvalidaException("A data do pagamento não pode ser anterior ao envio da cobrança.");
		}
	}

	private LocalDate hoje() {
		return LocalDate.now(relogio);
	}

	private static RecursoNaoEncontradoException naoEncontrada() {
		return new RecursoNaoEncontradoException("Cobrança não encontrada.");
	}

}
