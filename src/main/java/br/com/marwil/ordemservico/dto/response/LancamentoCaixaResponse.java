package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LancamentoCaixaResponse(
		Long id,
		LocalDate dataLancamento,
		BigDecimal valor,
		String tipo,
		String origem,
		Long pagamentoId,
		Long cobrancaId,
		Long ordemServicoId,
		ReferenciaResponse cliente,
		OffsetDateTime criadoEm) {
}
