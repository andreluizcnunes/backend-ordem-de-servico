package br.com.marwil.ordemservico.auth;

public record UsuarioAutenticado(long id, String nome, Perfil perfil) {

	public boolean ehGestor() {
		return perfil.ehGestor();
	}

	public boolean ehTecnico() {
		return perfil == Perfil.TECNICO;
	}

}
