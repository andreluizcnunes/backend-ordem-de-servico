package br.com.marwil.ordemservico.cobrancas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record Cobranca(
		long id,
		long ordemServicoId,
		UsuarioResumido cliente,
		BigDecimal valorTotal,
		StatusCobranca status,
		LocalDate dataVencimento,
		boolean vencida,
		UsuarioResumido enviadaPor,
		OffsetDateTime enviadaEm,
		Pagamento pagamento) {

	public record Pagamento(
			long id,
			BigDecimal valorRecebido,
			LocalDate dataPagamento,
			UsuarioResumido registradoPor,
			OffsetDateTime registradoEm) {
	}

}
