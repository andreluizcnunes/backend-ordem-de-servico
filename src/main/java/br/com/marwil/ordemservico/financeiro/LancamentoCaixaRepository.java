package br.com.marwil.ordemservico.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LancamentoCaixaRepository {

	private final JdbcClient jdbc;

	public LancamentoCaixaRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public void registrarEntrada(long pagamentoId, BigDecimal valor, LocalDate dataLancamento) {
		jdbc.sql("""
				INSERT INTO lancamentos_caixa (pagamento_id, valor, data_lancamento)
				VALUES (:pagamentoId, :valor, :data)
				""")
				.param("pagamentoId", pagamentoId)
				.param("valor", valor)
				.param("data", dataLancamento)
				.update();
	}

}
