package br.com.marwil.ordemservico.cobrancas;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PagamentoRepository {

	private final JdbcClient jdbc;

	public PagamentoRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public long registrar(long cobrancaId, BigDecimal valorRecebido, LocalDate dataPagamento, long registradoPor) {
		return jdbc.sql("""
				INSERT INTO pagamentos (cobranca_id, valor_recebido, data_pagamento, registrado_por)
				VALUES (:cobrancaId, :valor, :data, :registradoPor)
				RETURNING id
				""")
				.param("cobrancaId", cobrancaId)
				.param("valor", valorRecebido)
				.param("data", dataPagamento)
				.param("registradoPor", registradoPor)
				.query(Long.class)
				.single();
	}

}
