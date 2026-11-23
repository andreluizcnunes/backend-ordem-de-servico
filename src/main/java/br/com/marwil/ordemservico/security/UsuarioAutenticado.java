package br.com.marwil.ordemservico.security;

import br.com.marwil.ordemservico.enums.Perfil;

public record UsuarioAutenticado(long id, String nome, Perfil perfil) {

	public boolean ehGestor() {
		return perfil.ehGestor();
	}

	public boolean ehTecnico() {
		return perfil == Perfil.TECNICO;
	}

}
