package br.com.marwil.ordemservico.enums;

public enum StatusAtendimento {

	ABERTA,
	EM_ATENDIMENTO,
	AGUARDANDO,
	CONCLUIDA;

	public boolean emCicloOperacional() {
		return this != CONCLUIDA;
	}

}
