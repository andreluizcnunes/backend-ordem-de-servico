package br.com.marwil.ordemservico.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.repository.OrdemServicoRepository;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;

import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.comId;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.doTecnico;
import static br.com.marwil.ordemservico.repository.specification.OrdemServicoSpecifications.visivelAoTecnico;

@Component
public class AcessoOrdemServico {

	private final OrdemServicoRepository ordens;

	public AcessoOrdemServico(OrdemServicoRepository ordens) {
		this.ordens = ordens;
	}

	public boolean visivel(UsuarioAutenticado usuario, Long ordemServicoId) {
		return usuario.ehGestor() || ordens.exists(comId(ordemServicoId).and(visivelAoTecnico(usuario.id())));
	}

	public void garantirVisivel(UsuarioAutenticado usuario, Long ordemServicoId) {
		if (!visivel(usuario, ordemServicoId)) {
			throw ordemNaoEncontrada();
		}
	}

	public void garantirGestorOuIntegrante(UsuarioAutenticado usuario, Long ordemServicoId) {
		garantirVisivel(usuario, ordemServicoId);
		if (usuario.ehTecnico() && !ordens.exists(comId(ordemServicoId).and(doTecnico(usuario.id())))) {
			throw new AccessDeniedException("O técnico precisa integrar a equipe da ordem de serviço.");
		}
	}

	public static RecursoNaoEncontradoException ordemNaoEncontrada() {
		return new RecursoNaoEncontradoException("Ordem de serviço não encontrada.");
	}

}
