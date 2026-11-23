package br.com.marwil.ordemservico.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record ResumoFinanceiro(
		LocalDate inicio,
		LocalDate fim,
		BigDecimal totalRecebido,
		long quantidadeRecebimentos,
		BigDecimal totalPendente,
		long quantidadePendentes,
		BigDecimal totalVencido,
		long quantidadeVencidas,
		List<PorCliente> porCliente) {

	public record PorCliente(
			UsuarioResumido cliente,
			BigDecimal totalRecebido,
			BigDecimal totalPendente,
			long quantidadePendentes,
			boolean possuiVencidas) {
	}

}
