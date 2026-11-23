package br.com.marwil.ordemservico.enums;

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
