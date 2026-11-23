package br.com.marwil.ordemservico.auth;

public enum Perfil {

	DONO,
	ADMINISTRATIVO,
	TECNICO;

	public boolean ehGestor() {
		return this == DONO || this == ADMINISTRATIVO;
	}

	public String autoridade() {
		return "ROLE_" + name();
	}

}
