package br.com.marwil.ordemservico.service;

import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.comStatus;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.comStatusCobranca;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.comTecnico;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.comTermo;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.disponivel;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.doCliente;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.doTecnico;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.visivelAoTecnico;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.OrdemServicoAtualizacaoRequest;
import br.com.marwil.ordemservico.dto.request.OrdemServicoRequest;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResponse;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResumoResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.entity.Cliente;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.mapper.OrdemServicoMapper;
import br.com.marwil.ordemservico.repository.ClienteRepository;
import br.com.marwil.ordemservico.repository.OrdemServicoRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.repository.projection.StatusCobrancaDaOrdem;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.util.Textos;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

	private final OrdemServicoRepository ordens;
	private final ClienteRepository clientes;
	private final UsuarioRepository usuarios;
	private final EquipeService equipes;
	private final AcessoOrdemServico acesso;
	private final OrdemServicoMapper mapper;
	private final Clock relogio;

	@Transactional
	public Long abrir(UsuarioAutenticado responsavel, OrdemServicoRequest requisicao) {
		Cliente cliente = clientes.findById(requisicao.clienteId())
				.orElseThrow(() -> new RequisicaoInvalidaException("O cliente informado não existe."));
		OrdemServico ordem = mapper.paraEntidade(requisicao);
		ordem.setCliente(cliente);
		ordem.setCriadoPor(usuarios.getReferenceById(responsavel.id()));
		ordem.setStatusAtendimento(StatusAtendimento.ABERTA);
		if (ordem.getDataAbertura() == null) {
			ordem.setDataAbertura(OffsetDateTime.now(relogio));
		}
		ordem.setEnderecoAtendimento(Textos.primeiroPreenchido(requisicao.enderecoAtendimento(), cliente.getEndereco()));
		if (ordem.getEnderecoAtendimento() == null) {
			throw new RequisicaoInvalidaException(
					"Informe o endereço de atendimento ou cadastre o endereço do cliente.");
		}
		ordem.setContatoAtendimento(Textos.primeiroPreenchido(requisicao.contatoAtendimento(), cliente.getContato()));
		ordem.setTelefoneAtendimento(
				Textos.primeiroPreenchido(requisicao.telefoneAtendimento(), cliente.getTelefone()));
		ordem.setEmailAtendimento(Textos.primeiroPreenchido(requisicao.emailAtendimento(), cliente.getEmail()));
		OrdemServico salva = ordens.saveAndFlush(ordem);
		equipes.atribuirNaAbertura(responsavel, salva, requisicao.tecnicosIds());
		return salva.getId();
	}

	@Transactional
	public void atualizar(Long id, OrdemServicoAtualizacaoRequest requisicao) {
		OrdemServico ordem = ordens.findComBloqueioById(id).orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (ordens.findStatusCobranca(id).isPresent()) {
			throw new ConflitoException("A ordem de serviço já foi enviada à cobrança e não pode ser editada.");
		}
		mapper.atualizar(requisicao, ordem);
	}

	@Transactional(readOnly = true)
	public OrdemServicoResponse detalhar(UsuarioAutenticado usuario, Long id) {
		acesso.garantirVisivel(usuario, id);
		OrdemServico ordem = ordens.findById(id).orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		StatusCobranca statusCobranca = usuario.ehGestor()
				? ordens.findStatusCobranca(id).orElse(StatusCobranca.NAO_ENVIADA)
				: null;
		return mapper.paraResponse(ordem, statusCobranca);
	}

	@Transactional(readOnly = true)
	public Optional<OrdemServicoResponse> detalharSeVisivel(UsuarioAutenticado usuario, Long id) {
		return acesso.visivel(usuario, id) ? Optional.of(detalhar(usuario, id)) : Optional.empty();
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OrdemServicoResumoResponse> listar(UsuarioAutenticado usuario,
			StatusAtendimento statusAtendimento, StatusCobranca statusCobranca, Long clienteId, Long tecnicoId,
			String busca, Pageable paginacao) {
		Specification<OrdemServico> filtro = usuario.ehGestor()
				? Specification.allOf(List.of(comStatus(statusAtendimento), comStatusCobranca(statusCobranca),
						doCliente(clienteId), comTecnico(tecnicoId), comTermo(Textos.opcional(busca))))
				: visivelAoTecnico(usuario.id()).and(comStatus(statusAtendimento));
		return resumir(usuario, ordens.findAll(filtro, paginacao));
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OrdemServicoResumoResponse> listarDisponiveis(UsuarioAutenticado usuario,
			Pageable paginacao) {
		return resumir(usuario, ordens.findAll(disponivel(), paginacao));
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OrdemServicoResumoResponse> listarDoTecnico(UsuarioAutenticado tecnico,
			StatusAtendimento status, Pageable paginacao) {
		return resumir(tecnico, ordens.findAll(doTecnico(tecnico.id()).and(comStatus(status)), paginacao));
	}

	private PaginaResponse<OrdemServicoResumoResponse> resumir(UsuarioAutenticado usuario, Page<OrdemServico> pagina) {
		Map<Long, StatusCobranca> statusPorOrdem = usuario.ehGestor() ? statusCobranca(pagina.getContent()) : Map.of();
		return PaginaResponse.de(pagina.map(ordem -> mapper.paraResumo(ordem, usuario.ehGestor()
				? statusPorOrdem.getOrDefault(ordem.getId(), StatusCobranca.NAO_ENVIADA)
				: null)));
	}

	private Map<Long, StatusCobranca> statusCobranca(List<OrdemServico> ordensDaPagina) {
		if (ordensDaPagina.isEmpty()) {
			return Map.of();
		}
		return ordens.findStatusCobrancaPorOrdens(ordensDaPagina.stream().map(OrdemServico::getId).toList())
				.stream()
				.collect(Collectors.toMap(StatusCobrancaDaOrdem::ordemServicoId, StatusCobrancaDaOrdem::status));
	}

}
