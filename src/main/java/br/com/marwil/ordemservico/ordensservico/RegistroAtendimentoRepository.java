package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoRegistro;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

@Repository
public class RegistroAtendimentoRepository {

	private static final String SELECAO = """
			SELECT r.id, r.tipo, r.descricao, r.materiais_utilizados, r.registrado_em, r.autor_id, u.nome AS autor_nome
			FROM registros_atendimento r
			JOIN usuarios u ON u.id = r.autor_id
			""";

	private final JdbcClient jdbc;

	public RegistroAtendimentoRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public List<RegistroAtendimento> listar(long ordemServicoId) {
		return jdbc.sql(SELECAO + " WHERE r.ordem_servico_id = :id ORDER BY r.registrado_em, r.id")
				.param("id", ordemServicoId)
				.query(RegistroAtendimentoRepository::mapear)
				.list();
	}

	public RegistroAtendimento inserir(long ordemServicoId, long autorId, TipoRegistro tipo, String descricao,
			String materiaisUtilizados) {
		long id = jdbc.sql("""
				INSERT INTO registros_atendimento (ordem_servico_id, autor_id, tipo, descricao, materiais_utilizados)
				VALUES (:ordemId, :autorId, :tipo, :descricao, :materiais)
				RETURNING id
				""")
				.param("ordemId", ordemServicoId)
				.param("autorId", autorId)
				.param("tipo", tipo.name())
				.param("descricao", descricao)
				.param("materiais", materiaisUtilizados)
				.query(Long.class)
				.single();
		return jdbc.sql(SELECAO + " WHERE r.id = :id")
				.param("id", id)
				.query(RegistroAtendimentoRepository::mapear)
				.single();
	}

	private static RegistroAtendimento mapear(ResultSet rs, int linha) throws SQLException {
		return new RegistroAtendimento(
				rs.getLong("id"),
				TipoRegistro.valueOf(rs.getString("tipo")),
				rs.getString("descricao"),
				rs.getString("materiais_utilizados"),
				new UsuarioResumido(rs.getLong("autor_id"), rs.getString("autor_nome")),
				rs.getObject("registrado_em", OffsetDateTime.class));
	}

}
