package br.com.marwil.ordemservico.ordensservico;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;

@Component
public class AcessoOrdemServico {

	private final OrdemServicoRepository ordens;

	public AcessoOrdemServico(OrdemServicoRepository ordens) {
		this.ordens = ordens;
	}

	public void garantirVisivel(UsuarioAutenticado usuario, long ordemServicoId) {
		if (usuario.ehGestor()) {
			return;
		}
		if (!ordens.visivelAoTecnico(ordemServicoId, usuario.id())) {
			throw ordemNaoEncontrada();
		}
	}

	public void garantirGestorOuIntegrante(UsuarioAutenticado usuario, long ordemServicoId) {
		if (usuario.ehGestor()) {
			return;
		}
		if (!ordens.visivelAoTecnico(ordemServicoId, usuario.id())) {
			throw ordemNaoEncontrada();
		}
		if (!ordens.atribuidaAoTecnico(ordemServicoId, usuario.id())) {
			throw new AccessDeniedException("O técnico precisa integrar a equipe da ordem de serviço.");
		}
	}

	public static RecursoNaoEncontradoException ordemNaoEncontrada() {
		return new RecursoNaoEncontradoException("Ordem de serviço não encontrada.");
	}

}
