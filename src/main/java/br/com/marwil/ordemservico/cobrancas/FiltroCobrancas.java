package br.com.marwil.ordemservico.cobrancas;

import br.com.marwil.ordemservico.enums.StatusCobranca;

public record FiltroCobrancas(StatusCobranca status, Long clienteId, Long ordemServicoId, Boolean vencidas) {
}
