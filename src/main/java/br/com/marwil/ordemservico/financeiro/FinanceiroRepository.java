package br.com.marwil.ordemservico.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.comum.UsuarioResumido;

@Repository
public class FinanceiroRepository {

	private static final String PERIODO = """
			(CAST(:inicio AS DATE) IS NULL OR l.data_lancamento >= :inicio)
			AND (CAST(:fim AS DATE) IS NULL OR l.data_lancamento <= :fim)
			""";

	private final JdbcClient jdbc;

	public FinanceiroRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public Totais totalRecebido(LocalDate inicio, LocalDate fim) {
		return jdbc.sql("SELECT coalesce(sum(l.valor), 0) AS total, count(*) AS quantidade FROM lancamentos_caixa l"
				+ " WHERE " + PERIODO)
				.param("inicio", inicio)
				.param("fim", fim)
				.query((rs, linha) -> new Totais(rs.getBigDecimal("total"), rs.getLong("quantidade")))
				.single();
	}

	public Totais totalPendente(LocalDate hoje, boolean somenteVencidas) {
		return jdbc.sql("""
				SELECT coalesce(sum(cb.valor_total), 0) AS total, count(*) AS quantidade
				FROM cobrancas cb
				WHERE cb.status = 'PENDENTE'
				  AND (NOT :somenteVencidas OR (cb.data_vencimento IS NOT NULL AND cb.data_vencimento < :hoje))
				""")
				.param("hoje", hoje)
				.param("somenteVencidas", somenteVencidas)
				.query((rs, linha) -> new Totais(rs.getBigDecimal("total"), rs.getLong("quantidade")))
				.single();
	}

	public List<ResumoFinanceiro.PorCliente> porCliente(LocalDate inicio, LocalDate fim, LocalDate hoje) {
		return jdbc.sql("""
				WITH recebidos AS (
				    SELECT os.cliente_id, sum(l.valor) AS total
				    FROM lancamentos_caixa l
				    JOIN pagamentos p ON p.id = l.pagamento_id
				    JOIN cobrancas cb ON cb.id = p.cobranca_id
				    JOIN ordens_servico os ON os.id = cb.ordem_servico_id
				    WHERE
				""" + PERIODO + """
				    GROUP BY os.cliente_id
				), pendentes AS (
				    SELECT os.cliente_id, sum(cb.valor_total) AS total, count(*) AS quantidade,
				           bool_or(cb.data_vencimento IS NOT NULL AND cb.data_vencimento < :hoje) AS possui_vencidas
				    FROM cobrancas cb
				    JOIN ordens_servico os ON os.id = cb.ordem_servico_id
				    WHERE cb.status = 'PENDENTE'
				    GROUP BY os.cliente_id
				)
				SELECT c.id, c.nome,
				       coalesce(r.total, 0) AS total_recebido,
				       coalesce(p.total, 0) AS total_pendente,
				       coalesce(p.quantidade, 0) AS quantidade_pendentes,
				       coalesce(p.possui_vencidas, FALSE) AS possui_vencidas
				FROM clientes c
				LEFT JOIN recebidos r ON r.cliente_id = c.id
				LEFT JOIN pendentes p ON p.cliente_id = c.id
				WHERE r.cliente_id IS NOT NULL OR p.cliente_id IS NOT NULL
				ORDER BY coalesce(p.total, 0) DESC, lower(c.nome), c.id
				""")
				.param("inicio", inicio)
				.param("fim", fim)
				.param("hoje", hoje)
				.query((rs, linha) -> new ResumoFinanceiro.PorCliente(
						new UsuarioResumido(rs.getLong("id"), rs.getString("nome")),
						rs.getBigDecimal("total_recebido"),
						rs.getBigDecimal("total_pendente"),
						rs.getLong("quantidade_pendentes"),
						rs.getBoolean("possui_vencidas")))
				.list();
	}

	public List<LancamentoCaixa> listarLancamentos(LocalDate inicio, LocalDate fim, Paginacao paginacao) {
		return jdbc.sql("""
				SELECT l.id, l.data_lancamento, l.valor, l.criado_em, l.pagamento_id, cb.id AS cobranca_id,
				       os.id AS ordem_servico_id, c.id AS cliente_id, c.nome AS cliente_nome
				FROM lancamentos_caixa l
				JOIN pagamentos p ON p.id = l.pagamento_id
				JOIN cobrancas cb ON cb.id = p.cobranca_id
				JOIN ordens_servico os ON os.id = cb.ordem_servico_id
				JOIN clientes c ON c.id = os.cliente_id
				WHERE
				""" + PERIODO + """
				ORDER BY l.data_lancamento DESC, l.id DESC
				LIMIT :limite OFFSET :deslocamento
				""")
				.param("inicio", inicio)
				.param("fim", fim)
				.param("limite", paginacao.tamanho())
				.param("deslocamento", paginacao.deslocamento())
				.query((rs, linha) -> new LancamentoCaixa(
						rs.getLong("id"),
						rs.getObject("data_lancamento", LocalDate.class),
						rs.getBigDecimal("valor"),
						"ENTRADA",
						"ORDEM_SERVICO",
						rs.getLong("pagamento_id"),
						rs.getLong("cobranca_id"),
						rs.getLong("ordem_servico_id"),
						new UsuarioResumido(rs.getLong("cliente_id"), rs.getString("cliente_nome")),
						rs.getObject("criado_em", OffsetDateTime.class)))
				.list();
	}

	public record Totais(BigDecimal total, long quantidade) {
	}

}
