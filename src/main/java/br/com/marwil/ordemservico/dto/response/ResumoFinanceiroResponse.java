package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumoFinanceiroResponse(
		LocalDate inicio,
		LocalDate fim,
		BigDecimal totalRecebido,
		long quantidadeRecebimentos,
		BigDecimal totalPendente,
		long quantidadePendentes,
		BigDecimal totalVencido,
		long quantidadeVencidas,
		List<ResumoFinanceiroClienteResponse> porCliente) {
}
