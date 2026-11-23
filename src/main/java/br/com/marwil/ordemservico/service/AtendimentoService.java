package br.com.marwil.ordemservico.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.RegistroAtendimentoRequest;
import br.com.marwil.ordemservico.dto.request.SituacaoAtendimentoRequest;
import br.com.marwil.ordemservico.dto.response.RegistroAtendimentoResponse;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.entity.RegistroAtendimento;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.TipoRegistro;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.mapper.RegistroAtendimentoMapper;
import br.com.marwil.ordemservico.repository.OrdemServicoRepository;
import br.com.marwil.ordemservico.repository.RegistroAtendimentoRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;

@Service
public class AtendimentoService {

	private static final Map<StatusAtendimento, Set<StatusAtendimento>> TRANSICOES = Map.of(
			StatusAtendimento.ABERTA, Set.of(StatusAtendimento.EM_ATENDIMENTO, StatusAtendimento.AGUARDANDO),
			StatusAtendimento.EM_ATENDIMENTO, Set.of(StatusAtendimento.AGUARDANDO, StatusAtendimento.CONCLUIDA),
			StatusAtendimento.AGUARDANDO, Set.of(StatusAtendimento.EM_ATENDIMENTO),
			StatusAtendimento.CONCLUIDA, Set.of(StatusAtendimento.EM_ATENDIMENTO));

	private final OrdemServicoRepository ordens;
	private final RegistroAtendimentoRepository registros;
	private final UsuarioRepository usuarios;
	private final AcessoOrdemServico acesso;
	private final RegistroAtendimentoMapper mapper;
	private final Clock relogio;

	public AtendimentoService(OrdemServicoRepository ordens, RegistroAtendimentoRepository registros,
			UsuarioRepository usuarios, AcessoOrdemServico acesso, RegistroAtendimentoMapper mapper, Clock relogio) {
		this.ordens = ordens;
		this.registros = registros;
		this.usuarios = usuarios;
		this.acesso = acesso;
		this.mapper = mapper;
		this.relogio = relogio;
	}

	@Transactional
	public void alterarSituacao(UsuarioAutenticado usuario, Long ordemServicoId, SituacaoAtendimentoRequest requisicao) {
		OrdemServico ordem = ordens.findComBloqueioById(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		acesso.garantirGestorOuIntegrante(usuario, ordemServicoId);
		StatusAtendimento novoStatus = requisicao.status();
		garantirTransicaoPermitida(usuario, ordem, novoStatus);
		OffsetDateTime agora = OffsetDateTime.now(relogio);
		switch (novoStatus) {
			case EM_ATENDIMENTO -> iniciar(ordem, agora);
			case AGUARDANDO -> colocarEmEspera(usuario, ordem, requisicao.motivo(), agora);
			case CONCLUIDA -> concluir(usuario, ordem, agora);
			case ABERTA -> throw transicaoInvalida(ordem.getStatusAtendimento(), novoStatus);
		}
	}

	@Transactional
	public RegistroAtendimentoResponse registrar(UsuarioAutenticado usuario, Long ordemServicoId,
			RegistroAtendimentoRequest requisicao) {
		OrdemServico ordem = ordens.findComBloqueioById(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		acesso.garantirGestorOuIntegrante(usuario, ordemServicoId);
		RegistroAtendimento registro = novoRegistro(usuario, ordem, TipoRegistro.ATIVIDADE, requisicao.descricao(),
				OffsetDateTime.now(relogio));
		registro.setMateriaisUtilizados(requisicao.materiaisUtilizados());
		return mapper.paraResponse(registros.saveAndFlush(registro));
	}

	@Transactional(readOnly = true)
	public List<RegistroAtendimentoResponse> listarRegistros(UsuarioAutenticado usuario, Long ordemServicoId) {
		acesso.garantirVisivel(usuario, ordemServicoId);
		if (!ordens.existsById(ordemServicoId)) {
			throw AcessoOrdemServico.ordemNaoEncontrada();
		}
		return registros.findByOrdemServicoIdOrderByRegistradoEmAscIdAsc(ordemServicoId).stream()
				.map(mapper::paraResponse)
				.toList();
	}

	private static void iniciar(OrdemServico ordem, OffsetDateTime agora) {
		ordem.setStatusAtendimento(StatusAtendimento.EM_ATENDIMENTO);
		ordem.setMotivoAguardando(null);
		ordem.setDataConclusao(null);
		ordem.setConcluidaPor(null);
		if (ordem.getDataInicioAtendimento() == null) {
			ordem.setDataInicioAtendimento(agora);
		}
	}

	private void colocarEmEspera(UsuarioAutenticado usuario, OrdemServico ordem, String motivo, OffsetDateTime agora) {
		if (motivo == null) {
			throw new RequisicaoInvalidaException("Informe o motivo para colocar a ordem de serviço em espera.");
		}
		ordem.setStatusAtendimento(StatusAtendimento.AGUARDANDO);
		ordem.setMotivoAguardando(motivo);
		registros.save(novoRegistro(usuario, ordem, TipoRegistro.IMPEDIMENTO, motivo, agora));
	}

	private void concluir(UsuarioAutenticado usuario, OrdemServico ordem, OffsetDateTime agora) {
		ordem.setStatusAtendimento(StatusAtendimento.CONCLUIDA);
		ordem.setMotivoAguardando(null);
		ordem.setDataConclusao(agora);
		ordem.setConcluidaPor(usuarios.getReferenceById(usuario.id()));
	}

	private RegistroAtendimento novoRegistro(UsuarioAutenticado usuario, OrdemServico ordem, TipoRegistro tipo,
			String descricao, OffsetDateTime agora) {
		RegistroAtendimento registro = new RegistroAtendimento();
		registro.setOrdemServico(ordem);
		registro.setAutor(usuarios.getReferenceById(usuario.id()));
		registro.setTipo(tipo);
		registro.setDescricao(descricao);
		registro.setRegistradoEm(agora);
		return registro;
	}

	private void garantirTransicaoPermitida(UsuarioAutenticado usuario, OrdemServico ordem,
			StatusAtendimento novoStatus) {
		StatusAtendimento atual = ordem.getStatusAtendimento();
		if (!TRANSICOES.get(atual).contains(novoStatus)) {
			throw transicaoInvalida(atual, novoStatus);
		}
		if (atual == StatusAtendimento.CONCLUIDA) {
			if (!usuario.ehGestor()) {
				throw new AccessDeniedException("Somente gestores reabrem ordens de serviço concluídas.");
			}
			if (ordens.findStatusCobranca(ordem.getId()).isPresent()) {
				throw new ConflitoException("A ordem de serviço já foi enviada à cobrança e não pode ser reaberta.");
			}
		}
	}

	private static ConflitoException transicaoInvalida(StatusAtendimento atual, StatusAtendimento novoStatus) {
		return new ConflitoException("Não é permitido alterar a situação de " + atual + " para " + novoStatus + ".");
	}

}
