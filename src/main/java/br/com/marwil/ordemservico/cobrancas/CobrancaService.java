package br.com.marwil.ordemservico.cobrancas;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.cobrancas.CobrancaRepository.SituacaoCobranca;
import br.com.marwil.ordemservico.comum.ConflitoException;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.comum.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.comum.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.financeiro.LancamentoCaixaRepository;
import br.com.marwil.ordemservico.ordensservico.AcessoOrdemServico;
import br.com.marwil.ordemservico.ordensservico.EstadoOrdemServico;
import br.com.marwil.ordemservico.ordensservico.ItemOrdemServicoRepository;
import br.com.marwil.ordemservico.ordensservico.OrdemServicoRepository;
import br.com.marwil.ordemservico.ordensservico.StatusAtendimento;

@Service
public class CobrancaService {

	private final CobrancaRepository cobrancas;
	private final PagamentoRepository pagamentos;
	private final LancamentoCaixaRepository caixa;
	private final OrdemServicoRepository ordens;
	private final ItemOrdemServicoRepository itens;
	private final Clock relogio;

	public CobrancaService(CobrancaRepository cobrancas, PagamentoRepository pagamentos,
			LancamentoCaixaRepository caixa, OrdemServicoRepository ordens, ItemOrdemServicoRepository itens,
			Clock relogio) {
		this.cobrancas = cobrancas;
		this.pagamentos = pagamentos;
		this.caixa = caixa;
		this.ordens = ordens;
		this.itens = itens;
		this.relogio = relogio;
	}

	@Transactional
	public Cobranca enviar(UsuarioAutenticado responsavel, long ordemServicoId, LocalDate dataVencimento) {
		EstadoOrdemServico estado = ordens.bloquearParaAlteracao(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (estado.statusAtendimento() != StatusAtendimento.CONCLUIDA) {
			throw new ConflitoException("Somente ordens de serviço concluídas podem ser enviadas à cobrança.");
		}
		if (estado.cobrancaEnviada()) {
			throw new ConflitoException("A ordem de serviço já foi enviada à cobrança.");
		}
		if (itens.idsDaOrdem(ordemServicoId).isEmpty()) {
			throw new ConflitoException("Precifique a ordem de serviço antes de enviá-la à cobrança.");
		}
		BigDecimal total = itens.somarTotal(ordemServicoId);
		if (total.signum() <= 0) {
			throw new ConflitoException("O valor total da ordem de serviço precisa ser maior que zero para cobrança.");
		}
		LocalDate hoje = LocalDate.now(relogio);
		if (dataVencimento != null && dataVencimento.isBefore(hoje)) {
			throw new RequisicaoInvalidaException("A data de vencimento não pode estar no passado.");
		}
		long id = cobrancas.inserir(ordemServicoId, total, dataVencimento, responsavel.id());
		return buscar(id);
	}

	@Transactional
	public Cobranca registrarPagamento(UsuarioAutenticado responsavel, long cobrancaId, LocalDate dataPagamento,
			BigDecimal valorInformado) {
		SituacaoCobranca situacao = cobrancas.bloquearParaPagamento(cobrancaId)
				.orElseThrow(CobrancaService::naoEncontrada);
		if (situacao.status() == StatusCobranca.PAGA) {
			throw new ConflitoException("O pagamento desta cobrança já foi registrado.");
		}
		BigDecimal valorRecebido = valorInformado == null ? situacao.valorTotal() : valorInformado;
		if (valorRecebido.compareTo(situacao.valorTotal()) != 0) {
			throw new RequisicaoInvalidaException(
					"Somente o pagamento integral de " + situacao.valorTotal() + " é aceito nesta versão.");
		}
		LocalDate data = dataPagamento == null ? LocalDate.now(relogio) : dataPagamento;
		validarDataPagamento(situacao, data);
		if (!cobrancas.marcarPaga(cobrancaId)) {
			throw new ConflitoException("O pagamento desta cobrança já foi registrado.");
		}
		long pagamentoId = pagamentos.registrar(cobrancaId, valorRecebido, data, responsavel.id());
		caixa.registrarEntrada(pagamentoId, valorRecebido, data);
		return buscar(cobrancaId);
	}

	@Transactional(readOnly = true)
	public Cobranca buscar(long id) {
		return cobrancas.buscar(id, LocalDate.now(relogio)).orElseThrow(CobrancaService::naoEncontrada);
	}

	@Transactional(readOnly = true)
	public Pagina<Cobranca> listar(FiltroCobrancas filtro, Paginacao paginacao) {
		LocalDate hoje = LocalDate.now(relogio);
		return Pagina.de(cobrancas.listar(filtro, hoje, paginacao), paginacao, cobrancas.contar(filtro, hoje));
	}

	private void validarDataPagamento(SituacaoCobranca situacao, LocalDate data) {
		if (data.isAfter(LocalDate.now(relogio))) {
			throw new RequisicaoInvalidaException("A data do pagamento não pode estar no futuro.");
		}
		LocalDate envio = situacao.enviadaEm().atZoneSameInstant(relogio.getZone()).toLocalDate();
		if (data.isBefore(envio)) {
			throw new RequisicaoInvalidaException("A data do pagamento não pode ser anterior ao envio da cobrança.");
		}
	}

	private static RecursoNaoEncontradoException naoEncontrada() {
		return new RecursoNaoEncontradoException("Cobrança não encontrada.");
	}

}
