package br.com.marwil.ordemservico.ordensservico;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.cobrancas.StatusCobranca;

public record EstadoOrdemServico(
		long id,
		StatusAtendimento statusAtendimento,
		OffsetDateTime dataAbertura,
		OffsetDateTime dataInicioAtendimento,
		StatusCobranca statusCobranca) {

	public boolean cobrancaEnviada() {
		return statusCobranca != StatusCobranca.NAO_ENVIADA;
	}

}
