package br.com.marwil.ordemservico.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public record PagamentoRequest(
		LocalDate dataPagamento,
		@DecimalMin(value = "0", inclusive = false) @Digits(integer = 12, fraction = 2) BigDecimal valorRecebido) {
}
