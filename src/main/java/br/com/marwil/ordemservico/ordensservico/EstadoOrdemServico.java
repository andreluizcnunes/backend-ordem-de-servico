package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import br.com.marwil.ordemservico.enums.StatusCobranca;

public record EstadoOrdemServico(long id, StatusAtendimento statusAtendimento, StatusCobranca statusCobranca) {

	public boolean cobrancaEnviada() {
		return statusCobranca != StatusCobranca.NAO_ENVIADA;
	}

}
