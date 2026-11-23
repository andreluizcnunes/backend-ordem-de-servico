package br.com.marwil.ordemservico.repository.projection;

import java.math.BigDecimal;

public record Totalizador(BigDecimal total, Long quantidade) {

	private static final BigDecimal ZERO = new BigDecimal("0.00");

	public Totalizador {
		total = total == null ? ZERO : total;
		quantidade = quantidade == null ? 0L : quantidade;
	}

}
