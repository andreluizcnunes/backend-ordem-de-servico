package br.com.marwil.ordemservico.cobrancas;

public record FiltroCobrancas(StatusCobranca status, Long clienteId, Long ordemServicoId, Boolean vencidas) {
}
