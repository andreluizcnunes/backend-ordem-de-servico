package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import br.com.marwil.ordemservico.enums.StatusCobranca;

public record FiltroOrdensServico(
		StatusAtendimento statusAtendimento,
		StatusCobranca statusCobranca,
		Long clienteId,
		Long tecnicoId,
		String busca) {
}
