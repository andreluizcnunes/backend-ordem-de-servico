package br.com.marwil.ordemservico.ordensservico;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

@Repository
public class EquipeRepository {

	private final JdbcClient jdbc;

	public EquipeRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public List<TecnicoAtribuido> listar(long ordemServicoId) {
		return jdbc.sql("""
				SELECT t.ordem_servico_id, u.id, u.nome, t.atribuido_em, t.atribuido_por, a.nome AS atribuido_por_nome
				FROM ordens_servico_tecnicos t
				JOIN usuarios u ON u.id = t.tecnico_id
				JOIN usuarios a ON a.id = t.atribuido_por
				WHERE t.ordem_servico_id = :id
				ORDER BY t.atribuido_em, lower(u.nome)
				""")
				.param("id", ordemServicoId)
				.query(EquipeRepository::mapear)
				.list();
	}

	public Map<Long, List<UsuarioResumido>> listarPorOrdens(Collection<Long> ordensIds) {
		if (ordensIds.isEmpty()) {
			return Map.of();
		}
		return jdbc.sql("""
				SELECT t.ordem_servico_id, u.id, u.nome
				FROM ordens_servico_tecnicos t
				JOIN usuarios u ON u.id = t.tecnico_id
				WHERE t.ordem_servico_id IN (:ids)
				ORDER BY t.atribuido_em, lower(u.nome)
				""")
				.param("ids", ordensIds)
				.query((rs, linha) -> Map.entry(rs.getLong("ordem_servico_id"),
						new UsuarioResumido(rs.getLong("id"), rs.getString("nome"))))
				.list()
				.stream()
				.collect(Collectors.groupingBy(Map.Entry::getKey,
						Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	public Set<Long> idsDaEquipe(long ordemServicoId) {
		return new HashSet<>(jdbc.sql("SELECT tecnico_id FROM ordens_servico_tecnicos WHERE ordem_servico_id = :id")
				.param("id", ordemServicoId)
				.query(Long.class)
				.list());
	}

	public boolean possuiTecnicos(long ordemServicoId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM ordens_servico_tecnicos WHERE ordem_servico_id = :id)")
				.param("id", ordemServicoId)
				.query(Boolean.class)
				.single();
	}

	public Set<Long> filtrarTecnicosAtivos(Collection<Long> usuariosIds) {
		if (usuariosIds.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(jdbc.sql("""
				SELECT id FROM usuarios WHERE id IN (:ids) AND perfil = 'TECNICO' AND ativo
				""")
				.param("ids", usuariosIds)
				.query(Long.class)
				.list());
	}

	public void atribuir(long ordemServicoId, long tecnicoId, long atribuidoPor) {
		jdbc.sql("""
				INSERT INTO ordens_servico_tecnicos (ordem_servico_id, tecnico_id, atribuido_por)
				VALUES (:ordemId, :tecnicoId, :atribuidoPor)
				""")
				.param("ordemId", ordemServicoId)
				.param("tecnicoId", tecnicoId)
				.param("atribuidoPor", atribuidoPor)
				.update();
	}

	public void remover(long ordemServicoId, Collection<Long> tecnicosIds) {
		if (tecnicosIds.isEmpty()) {
			return;
		}
		jdbc.sql("DELETE FROM ordens_servico_tecnicos WHERE ordem_servico_id = :ordemId AND tecnico_id IN (:ids)")
				.param("ordemId", ordemServicoId)
				.param("ids", tecnicosIds)
				.update();
	}

	private static TecnicoAtribuido mapear(ResultSet rs, int linha) throws SQLException {
		return new TecnicoAtribuido(
				rs.getLong("id"),
				rs.getString("nome"),
				rs.getObject("atribuido_em", OffsetDateTime.class),
				new UsuarioResumido(rs.getLong("atribuido_por"), rs.getString("atribuido_por_nome")));
	}

}
