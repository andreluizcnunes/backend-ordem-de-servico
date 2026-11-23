package br.com.marwil.ordemservico.auth;

import br.com.marwil.ordemservico.enums.Perfil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.comum.Paginacao;

@Repository
public class UsuarioRepository {

	private static final String COLUNAS = "id, nome, email, perfil, ativo, criado_em, atualizado_em";

	private final JdbcClient jdbc;

	public UsuarioRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public Optional<CredencialUsuario> buscarCredencialPorEmail(String email) {
		return jdbc.sql("""
				SELECT id, nome, perfil, senha_hash, ativo
				FROM usuarios
				WHERE lower(email) = lower(:email)
				""")
				.param("email", email)
				.query(UsuarioRepository::mapearCredencial)
				.optional();
	}

	public Optional<CredencialUsuario> buscarCredencialPorId(long id) {
		return jdbc.sql("SELECT id, nome, perfil, senha_hash, ativo FROM usuarios WHERE id = :id")
				.param("id", id)
				.query(UsuarioRepository::mapearCredencial)
				.optional();
	}

	public Optional<Usuario> buscarPorId(long id) {
		return jdbc.sql("SELECT " + COLUNAS + " FROM usuarios WHERE id = :id")
				.param("id", id)
				.query(UsuarioRepository::mapearUsuario)
				.optional();
	}

	public boolean existeAlgumUsuario() {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM usuarios)").query(Boolean.class).single();
	}

	public boolean existeEmail(String email, Long ignorarId) {
		return jdbc.sql("""
				SELECT EXISTS (
				    SELECT 1 FROM usuarios
				    WHERE lower(email) = lower(:email) AND (CAST(:ignorarId AS BIGINT) IS NULL OR id <> :ignorarId)
				)
				""")
				.param("email", email)
				.param("ignorarId", ignorarId)
				.query(Boolean.class)
				.single();
	}

	public long inserir(String nome, String email, String senhaHash, Perfil perfil) {
		return jdbc.sql("""
				INSERT INTO usuarios (nome, email, senha_hash, perfil)
				VALUES (:nome, :email, :senhaHash, :perfil)
				RETURNING id
				""")
				.param("nome", nome)
				.param("email", email)
				.param("senhaHash", senhaHash)
				.param("perfil", perfil.name())
				.query(Long.class)
				.single();
	}

	public void atualizar(long id, String nome, String email, Perfil perfil, boolean ativo) {
		jdbc.sql("""
				UPDATE usuarios
				SET nome = :nome, email = :email, perfil = :perfil, ativo = :ativo
				WHERE id = :id
				""")
				.param("id", id)
				.param("nome", nome)
				.param("email", email)
				.param("perfil", perfil.name())
				.param("ativo", ativo)
				.update();
	}

	public void atualizarSenha(long id, String senhaHash) {
		jdbc.sql("UPDATE usuarios SET senha_hash = :senhaHash WHERE id = :id")
				.param("id", id)
				.param("senhaHash", senhaHash)
				.update();
	}

	public List<Usuario> listar(Perfil perfil, Boolean ativo, Paginacao paginacao) {
		return jdbc.sql("SELECT " + COLUNAS + " FROM usuarios" + filtroListagem() + """
				 ORDER BY lower(nome), id
				 LIMIT :limite OFFSET :deslocamento
				""")
				.param("perfil", perfil == null ? null : perfil.name())
				.param("ativo", ativo)
				.param("limite", paginacao.tamanho())
				.param("deslocamento", paginacao.deslocamento())
				.query(UsuarioRepository::mapearUsuario)
				.list();
	}

	public long contar(Perfil perfil, Boolean ativo) {
		return jdbc.sql("SELECT count(*) FROM usuarios" + filtroListagem())
				.param("perfil", perfil == null ? null : perfil.name())
				.param("ativo", ativo)
				.query(Long.class)
				.single();
	}

	public boolean possuiOrdensAtribuidas(long tecnicoId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM ordens_servico_tecnicos WHERE tecnico_id = :id)")
				.param("id", tecnicoId)
				.query(Boolean.class)
				.single();
	}

	private static String filtroListagem() {
		return """
				 WHERE (CAST(:perfil AS VARCHAR) IS NULL OR perfil = :perfil)
				   AND (CAST(:ativo AS BOOLEAN) IS NULL OR ativo = :ativo)
				""";
	}

	private static CredencialUsuario mapearCredencial(ResultSet rs, int linha) throws SQLException {
		return new CredencialUsuario(
				rs.getLong("id"),
				rs.getString("nome"),
				Perfil.valueOf(rs.getString("perfil")),
				rs.getString("senha_hash"),
				rs.getBoolean("ativo"));
	}

	private static Usuario mapearUsuario(ResultSet rs, int linha) throws SQLException {
		return new Usuario(
				rs.getLong("id"),
				rs.getString("nome"),
				rs.getString("email"),
				Perfil.valueOf(rs.getString("perfil")),
				rs.getBoolean("ativo"),
				rs.getObject("criado_em", OffsetDateTime.class),
				rs.getObject("atualizado_em", OffsetDateTime.class));
	}

}
