package br.com.marwil.ordemservico.clientes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.comum.Paginacao;

@Repository
public class ClienteRepository {

	private static final String COLUNAS = "id, nome, contato, telefone, email, endereco, criado_em, atualizado_em";

	private static final String FILTRO_BUSCA = """
			 WHERE CAST(:busca AS VARCHAR) IS NULL
			    OR strpos(lower(nome), lower(:busca)) > 0
			    OR strpos(lower(coalesce(contato, '')), lower(:busca)) > 0
			    OR strpos(coalesce(telefone, ''), :busca) > 0
			    OR strpos(lower(coalesce(email, '')), lower(:busca)) > 0
			""";

	private final JdbcClient jdbc;

	public ClienteRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public long inserir(DadosCliente dados, long criadoPor) {
		return jdbc.sql("""
				INSERT INTO clientes (nome, contato, telefone, email, endereco, criado_por)
				VALUES (:nome, :contato, :telefone, :email, :endereco, :criadoPor)
				RETURNING id
				""")
				.param("nome", dados.nome())
				.param("contato", dados.contato())
				.param("telefone", dados.telefone())
				.param("email", dados.email())
				.param("endereco", dados.endereco())
				.param("criadoPor", criadoPor)
				.query(Long.class)
				.single();
	}

	public boolean atualizar(long id, DadosCliente dados) {
		return jdbc.sql("""
				UPDATE clientes
				SET nome = :nome, contato = :contato, telefone = :telefone, email = :email, endereco = :endereco
				WHERE id = :id
				""")
				.param("id", id)
				.param("nome", dados.nome())
				.param("contato", dados.contato())
				.param("telefone", dados.telefone())
				.param("email", dados.email())
				.param("endereco", dados.endereco())
				.update() == 1;
	}

	public Optional<Cliente> buscarPorId(long id) {
		return jdbc.sql("SELECT " + COLUNAS + " FROM clientes WHERE id = :id")
				.param("id", id)
				.query(ClienteRepository::mapear)
				.optional();
	}

	public List<Cliente> listar(String busca, Paginacao paginacao) {
		return jdbc.sql("SELECT " + COLUNAS + " FROM clientes" + FILTRO_BUSCA + """
				 ORDER BY lower(nome), id
				 LIMIT :limite OFFSET :deslocamento
				""")
				.param("busca", busca)
				.param("limite", paginacao.tamanho())
				.param("deslocamento", paginacao.deslocamento())
				.query(ClienteRepository::mapear)
				.list();
	}

	public long contar(String busca) {
		return jdbc.sql("SELECT count(*) FROM clientes" + FILTRO_BUSCA)
				.param("busca", busca)
				.query(Long.class)
				.single();
	}

	private static Cliente mapear(ResultSet rs, int linha) throws SQLException {
		return new Cliente(
				rs.getLong("id"),
				rs.getString("nome"),
				rs.getString("contato"),
				rs.getString("telefone"),
				rs.getString("email"),
				rs.getString("endereco"),
				rs.getObject("criado_em", OffsetDateTime.class),
				rs.getObject("atualizado_em", OffsetDateTime.class));
	}

}
