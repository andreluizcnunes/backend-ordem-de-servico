package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.cobrancas.StatusCobranca;

public record FiltroOrdensServico(
		StatusAtendimento statusAtendimento,
		StatusCobranca statusCobranca,
		Long clienteId,
		Long tecnicoId,
		String busca) {
}
