package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoRegistro;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.util.Textos;

@Service
public class AtendimentoService {

	private static final Map<StatusAtendimento, Set<StatusAtendimento>> TRANSICOES = Map.of(
			StatusAtendimento.ABERTA, Set.of(StatusAtendimento.EM_ATENDIMENTO, StatusAtendimento.AGUARDANDO),
			StatusAtendimento.EM_ATENDIMENTO, Set.of(StatusAtendimento.AGUARDANDO, StatusAtendimento.CONCLUIDA),
			StatusAtendimento.AGUARDANDO, Set.of(StatusAtendimento.EM_ATENDIMENTO),
			StatusAtendimento.CONCLUIDA, Set.of(StatusAtendimento.EM_ATENDIMENTO));

	private final OrdemServicoRepository ordens;
	private final RegistroAtendimentoRepository registros;
	private final AcessoOrdemServico acesso;

	public AtendimentoService(OrdemServicoRepository ordens, RegistroAtendimentoRepository registros,
			AcessoOrdemServico acesso) {
		this.ordens = ordens;
		this.registros = registros;
		this.acesso = acesso;
	}

	@Transactional
	public void alterarSituacao(UsuarioAutenticado usuario, long ordemServicoId, StatusAtendimento novoStatus,
			String motivo) {
		EstadoOrdemServico estado = ordens.bloquearParaAlteracao(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		acesso.garantirGestorOuIntegrante(usuario, ordemServicoId);
		garantirTransicaoPermitida(usuario, estado, novoStatus);
		switch (novoStatus) {
			case EM_ATENDIMENTO -> ordens.iniciarAtendimento(ordemServicoId);
			case AGUARDANDO -> colocarEmEspera(usuario, ordemServicoId, motivo);
			case CONCLUIDA -> ordens.concluir(ordemServicoId, usuario.id());
			case ABERTA -> throw transicaoInvalida(estado.statusAtendimento(), novoStatus);
		}
	}

	@Transactional
	public RegistroAtendimento registrar(UsuarioAutenticado usuario, long ordemServicoId, String descricao,
			String materiaisUtilizados) {
		ordens.bloquearParaAlteracao(ordemServicoId).orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		acesso.garantirGestorOuIntegrante(usuario, ordemServicoId);
		return registros.inserir(ordemServicoId, usuario.id(), TipoRegistro.ATIVIDADE,
				Textos.obrigatorio(descricao), Textos.opcional(materiaisUtilizados));
	}

	@Transactional(readOnly = true)
	public List<RegistroAtendimento> listarRegistros(UsuarioAutenticado usuario, long ordemServicoId) {
		acesso.garantirVisivel(usuario, ordemServicoId);
		ordens.buscarDetalhe(ordemServicoId).orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		return registros.listar(ordemServicoId);
	}

	private void colocarEmEspera(UsuarioAutenticado usuario, long ordemServicoId, String motivo) {
		String motivoInformado = Textos.opcional(motivo);
		if (motivoInformado == null) {
			throw new RequisicaoInvalidaException("Informe o motivo para colocar a ordem de serviço em espera.");
		}
		ordens.colocarEmEspera(ordemServicoId, motivoInformado);
		registros.inserir(ordemServicoId, usuario.id(), TipoRegistro.IMPEDIMENTO, motivoInformado, null);
	}

	private static void garantirTransicaoPermitida(UsuarioAutenticado usuario, EstadoOrdemServico estado,
			StatusAtendimento novoStatus) {
		StatusAtendimento atual = estado.statusAtendimento();
		if (!TRANSICOES.get(atual).contains(novoStatus)) {
			throw transicaoInvalida(atual, novoStatus);
		}
		if (atual == StatusAtendimento.CONCLUIDA) {
			if (!usuario.ehGestor()) {
				throw new AccessDeniedException("Somente gestores reabrem ordens de serviço concluídas.");
			}
			if (estado.cobrancaEnviada()) {
				throw new ConflitoException("A ordem de serviço já foi enviada à cobrança e não pode ser reaberta.");
			}
		}
	}

	private static ConflitoException transicaoInvalida(StatusAtendimento atual, StatusAtendimento novoStatus) {
		return new ConflitoException("Não é permitido alterar a situação de " + atual + " para " + novoStatus + ".");
	}

}
