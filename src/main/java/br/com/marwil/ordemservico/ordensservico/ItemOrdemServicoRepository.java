package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoItem;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ItemOrdemServicoRepository {

	private final JdbcClient jdbc;

	public ItemOrdemServicoRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public List<ItemOrdemServico> listar(long ordemServicoId) {
		return jdbc.sql("""
				SELECT id, tipo, descricao, local_realizacao, quantidade, preco_unitario, valor_total, atualizado_em
				FROM itens_ordem_servico
				WHERE ordem_servico_id = :id
				ORDER BY id
				""")
				.param("id", ordemServicoId)
				.query(ItemOrdemServicoRepository::mapear)
				.list();
	}

	public Set<Long> idsDaOrdem(long ordemServicoId) {
		return new HashSet<>(jdbc.sql("SELECT id FROM itens_ordem_servico WHERE ordem_servico_id = :id")
				.param("id", ordemServicoId)
				.query(Long.class)
				.list());
	}

	public void inserir(long ordemServicoId, DadosItem item, long criadoPor) {
		jdbc.sql("""
				INSERT INTO itens_ordem_servico (ordem_servico_id, tipo, descricao, local_realizacao, quantidade,
				                                 preco_unitario, criado_por)
				VALUES (:ordemId, :tipo, :descricao, :local, :quantidade, :preco, :criadoPor)
				""")
				.param("ordemId", ordemServicoId)
				.param("tipo", item.tipo().name())
				.param("descricao", item.descricao())
				.param("local", item.localRealizacao())
				.param("quantidade", item.quantidade())
				.param("preco", item.precoUnitario())
				.param("criadoPor", criadoPor)
				.update();
	}

	public void atualizar(long ordemServicoId, long itemId, DadosItem item) {
		jdbc.sql("""
				UPDATE itens_ordem_servico
				SET tipo = :tipo, descricao = :descricao, local_realizacao = :local, quantidade = :quantidade,
				    preco_unitario = :preco
				WHERE id = :itemId AND ordem_servico_id = :ordemId
				  AND (tipo, descricao, coalesce(local_realizacao, ''), quantidade, preco_unitario)
				      IS DISTINCT FROM (CAST(:tipo AS VARCHAR), CAST(:descricao AS VARCHAR),
				                        coalesce(CAST(:local AS VARCHAR), ''), CAST(:quantidade AS NUMERIC),
				                        CAST(:preco AS NUMERIC))
				""")
				.param("itemId", itemId)
				.param("ordemId", ordemServicoId)
				.param("tipo", item.tipo().name())
				.param("descricao", item.descricao())
				.param("local", item.localRealizacao())
				.param("quantidade", item.quantidade())
				.param("preco", item.precoUnitario())
				.update();
	}

	public void remover(long ordemServicoId, Collection<Long> itensIds) {
		if (itensIds.isEmpty()) {
			return;
		}
		jdbc.sql("DELETE FROM itens_ordem_servico WHERE ordem_servico_id = :ordemId AND id IN (:ids)")
				.param("ordemId", ordemServicoId)
				.param("ids", itensIds)
				.update();
	}

	public BigDecimal somarTotal(long ordemServicoId) {
		return jdbc.sql("SELECT coalesce(sum(valor_total), 0) FROM itens_ordem_servico WHERE ordem_servico_id = :id")
				.param("id", ordemServicoId)
				.query(BigDecimal.class)
				.single();
	}

	private static ItemOrdemServico mapear(ResultSet rs, int linha) throws SQLException {
		return new ItemOrdemServico(
				rs.getLong("id"),
				TipoItem.valueOf(rs.getString("tipo")),
				rs.getString("descricao"),
				rs.getString("local_realizacao"),
				rs.getBigDecimal("quantidade"),
				rs.getBigDecimal("preco_unitario"),
				rs.getBigDecimal("valor_total"),
				rs.getObject("atualizado_em", OffsetDateTime.class));
	}

	public record DadosItem(
			TipoItem tipo,
			String descricao,
			String localRealizacao,
			BigDecimal quantidade,
			BigDecimal precoUnitario) {
	}

}
