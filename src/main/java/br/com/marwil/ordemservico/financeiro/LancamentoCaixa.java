package br.com.marwil.ordemservico.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record LancamentoCaixa(
		long id,
		LocalDate dataLancamento,
		BigDecimal valor,
		String tipo,
		String origem,
		long pagamentoId,
		long cobrancaId,
		long ordemServicoId,
		UsuarioResumido cliente,
		OffsetDateTime criadoEm) {
}
