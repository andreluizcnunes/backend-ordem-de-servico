package br.com.marwil.ordemservico.ordensservico;

public enum StatusAtendimento {

	ABERTA,
	EM_ATENDIMENTO,
	AGUARDANDO,
	CONCLUIDA;

	public boolean emCicloOperacional() {
		return this != CONCLUIDA;
	}

}
