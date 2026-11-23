package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.cobrancas.StatusCobranca;

public record EstadoOrdemServico(long id, StatusAtendimento statusAtendimento, StatusCobranca statusCobranca) {

	public boolean cobrancaEnviada() {
		return statusCobranca != StatusCobranca.NAO_ENVIADA;
	}

}
