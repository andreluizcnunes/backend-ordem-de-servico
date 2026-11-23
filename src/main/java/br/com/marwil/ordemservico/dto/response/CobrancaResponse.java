package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.enums.StatusCobranca;

public record CobrancaResponse(
		Long id,
		Long ordemServicoId,
		ReferenciaResponse cliente,
		BigDecimal valorTotal,
		StatusCobranca status,
		LocalDate dataVencimento,
		boolean vencida,
		ReferenciaResponse enviadaPor,
		OffsetDateTime enviadaEm,
		PagamentoResponse pagamento) {
}
