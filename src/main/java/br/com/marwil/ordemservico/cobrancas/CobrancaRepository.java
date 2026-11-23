package br.com.marwil.ordemservico.cobrancas;

import br.com.marwil.ordemservico.enums.StatusCobranca;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.comum.UsuarioResumido;

@Repository
public class CobrancaRepository {

	private static final String VENCIDA = """
			(cb.status = 'PENDENTE' AND cb.data_vencimento IS NOT NULL AND cb.data_vencimento < :hoje)
			""";

	private static final String SELECAO = """
			SELECT cb.id, cb.ordem_servico_id, cb.valor_total, cb.status, cb.data_vencimento, cb.enviada_em,
			       cb.enviada_por, envio.nome AS enviada_por_nome, c.id AS cliente_id, c.nome AS cliente_nome,
			""" + VENCIDA + """
			        AS vencida,
			       p.id AS pagamento_id, p.valor_recebido, p.data_pagamento, p.registrado_em,
			       p.registrado_por, registro.nome AS registrado_por_nome
			FROM cobrancas cb
			JOIN ordens_servico os ON os.id = cb.ordem_servico_id
			JOIN clientes c ON c.id = os.cliente_id
			JOIN usuarios envio ON envio.id = cb.enviada_por
			LEFT JOIN pagamentos p ON p.cobranca_id = cb.id
			LEFT JOIN usuarios registro ON registro.id = p.registrado_por
			""";

	private static final String FILTRO = """
			 WHERE (CAST(:status AS VARCHAR) IS NULL OR cb.status = :status)
			   AND (CAST(:clienteId AS BIGINT) IS NULL OR os.cliente_id = :clienteId)
			   AND (CAST(:ordemId AS BIGINT) IS NULL OR cb.ordem_servico_id = :ordemId)
			   AND (CAST(:vencidas AS BOOLEAN) IS NULL OR
			""" + VENCIDA + " = :vencidas)";

	private final JdbcClient jdbc;

	public CobrancaRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public long inserir(long ordemServicoId, BigDecimal valorTotal, LocalDate dataVencimento, long enviadaPor) {
		return jdbc.sql("""
				INSERT INTO cobrancas (ordem_servico_id, valor_total, data_vencimento, enviada_por)
				VALUES (:ordemId, :valor, :vencimento, :enviadaPor)
				RETURNING id
				""")
				.param("ordemId", ordemServicoId)
				.param("valor", valorTotal)
				.param("vencimento", dataVencimento)
				.param("enviadaPor", enviadaPor)
				.query(Long.class)
				.single();
	}

	public Optional<SituacaoCobranca> bloquearParaPagamento(long id) {
		return jdbc.sql("SELECT id, status, valor_total, enviada_em FROM cobrancas WHERE id = :id FOR UPDATE")
				.param("id", id)
				.query((rs, linha) -> new SituacaoCobranca(
						rs.getLong("id"),
						StatusCobranca.valueOf(rs.getString("status")),
						rs.getBigDecimal("valor_total"),
						rs.getObject("enviada_em", OffsetDateTime.class)))
				.optional();
	}

	public boolean marcarPaga(long id) {
		return jdbc.sql("UPDATE cobrancas SET status = 'PAGA' WHERE id = :id AND status = 'PENDENTE'")
				.param("id", id)
				.update() == 1;
	}

	public Optional<Cobranca> buscar(long id, LocalDate hoje) {
		return jdbc.sql(SELECAO + " WHERE cb.id = :id")
				.param("id", id)
				.param("hoje", hoje)
				.query(CobrancaRepository::mapear)
				.optional();
	}

	public List<Cobranca> listar(FiltroCobrancas filtro, LocalDate hoje, Paginacao paginacao) {
		return jdbc.sql(SELECAO + FILTRO + """
				 ORDER BY CASE cb.status WHEN 'PENDENTE' THEN 0 ELSE 1 END,
				          cb.data_vencimento NULLS LAST, cb.enviada_em DESC, cb.id DESC
				 LIMIT :limite OFFSET :deslocamento
				""")
				.params(parametros(filtro, hoje))
				.param("limite", paginacao.tamanho())
				.param("deslocamento", paginacao.deslocamento())
				.query(CobrancaRepository::mapear)
				.list();
	}

	public long contar(FiltroCobrancas filtro, LocalDate hoje) {
		return jdbc.sql("""
				SELECT count(*)
				FROM cobrancas cb
				JOIN ordens_servico os ON os.id = cb.ordem_servico_id
				""" + FILTRO)
				.params(parametros(filtro, hoje))
				.query(Long.class)
				.single();
	}

	private static Map<String, Object> parametros(FiltroCobrancas filtro, LocalDate hoje) {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("status", filtro.status() == null ? null : filtro.status().name());
		parametros.put("clienteId", filtro.clienteId());
		parametros.put("ordemId", filtro.ordemServicoId());
		parametros.put("vencidas", filtro.vencidas());
		parametros.put("hoje", hoje);
		return parametros;
	}

	private static Cobranca mapear(ResultSet rs, int linha) throws SQLException {
		long pagamentoId = rs.getLong("pagamento_id");
		Cobranca.Pagamento pagamento = rs.wasNull() ? null : new Cobranca.Pagamento(
				pagamentoId,
				rs.getBigDecimal("valor_recebido"),
				rs.getObject("data_pagamento", LocalDate.class),
				new UsuarioResumido(rs.getLong("registrado_por"), rs.getString("registrado_por_nome")),
				rs.getObject("registrado_em", OffsetDateTime.class));
		return new Cobranca(
				rs.getLong("id"),
				rs.getLong("ordem_servico_id"),
				new UsuarioResumido(rs.getLong("cliente_id"), rs.getString("cliente_nome")),
				rs.getBigDecimal("valor_total"),
				StatusCobranca.valueOf(rs.getString("status")),
				rs.getObject("data_vencimento", LocalDate.class),
				rs.getBoolean("vencida"),
				new UsuarioResumido(rs.getLong("enviada_por"), rs.getString("enviada_por_nome")),
				rs.getObject("enviada_em", OffsetDateTime.class),
				pagamento);
	}

	public record SituacaoCobranca(long id, StatusCobranca status, BigDecimal valorTotal, OffsetDateTime enviadaEm) {
	}

}
