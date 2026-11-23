package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PagamentoResponse(
		Long id,
		BigDecimal valorRecebido,
		LocalDate dataPagamento,
		ReferenciaResponse registradoPor,
		OffsetDateTime registradoEm) {
}
