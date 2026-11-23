package br.com.marwil.ordemservico.auth;

public record CredencialUsuario(long id, String nome, Perfil perfil, String senhaHash, boolean ativo) {

	public UsuarioAutenticado comoAutenticado() {
		return new UsuarioAutenticado(id, nome, perfil);
	}

}
