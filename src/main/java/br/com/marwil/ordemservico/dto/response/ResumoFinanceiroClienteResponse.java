package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;

public record ResumoFinanceiroClienteResponse(
		ReferenciaResponse cliente,
		BigDecimal totalRecebido,
		BigDecimal totalPendente,
		long quantidadePendentes,
		boolean possuiVencidas) {
}
