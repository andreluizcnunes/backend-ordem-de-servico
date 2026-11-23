package br.com.marwil.ordemservico.repository.projection;

import java.math.BigDecimal;

public record PendenciaPorCliente(Long clienteId, String nome, BigDecimal total, Long quantidade,
		Integer vencidas) {

	public boolean possuiVencidas() {
		return vencidas != null && vencidas > 0;
	}

}
