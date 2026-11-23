package br.com.marwil.ordemservico.ordensservico;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.marwil.ordemservico.cobrancas.StatusCobranca;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.comum.UsuarioResumido;

@Repository
public class OrdemServicoRepository {

	static final String SEM_TECNICOS = """
			NOT EXISTS (SELECT 1 FROM ordens_servico_tecnicos e WHERE e.ordem_servico_id = os.id)
			""";

	static final String ATRIBUIDA_AO_TECNICO = """
			EXISTS (SELECT 1 FROM ordens_servico_tecnicos e
			        WHERE e.ordem_servico_id = os.id AND e.tecnico_id = :tecnicoAtual)
			""";

	static final String DISPONIVEL = "os.status_atendimento = 'ABERTA' AND " + SEM_TECNICOS;

	static final String DO_TECNICO = "os.status_atendimento <> 'CONCLUIDA' AND " + ATRIBUIDA_AO_TECNICO;

	static final String VISIVEL_AO_TECNICO = "((" + DISPONIVEL + ") OR (" + DO_TECNICO + "))";

	private static final String FROM_ORDENS = """
			 FROM ordens_servico os
			 JOIN clientes c ON c.id = os.cliente_id
			 LEFT JOIN cobrancas cb ON cb.ordem_servico_id = os.id
			""";

	private static final String FILTRO_GESTOR = """
			(CAST(:statusAtendimento AS VARCHAR) IS NULL OR os.status_atendimento = :statusAtendimento)
			AND (CAST(:statusCobranca AS VARCHAR) IS NULL OR coalesce(cb.status, 'NAO_ENVIADA') = :statusCobranca)
			AND (CAST(:clienteId AS BIGINT) IS NULL OR os.cliente_id = :clienteId)
			AND (CAST(:tecnicoId AS BIGINT) IS NULL OR EXISTS (
			     SELECT 1 FROM ordens_servico_tecnicos f WHERE f.ordem_servico_id = os.id AND f.tecnico_id = :tecnicoId))
			AND (CAST(:busca AS VARCHAR) IS NULL
			     OR strpos(lower(os.descricao), lower(:busca)) > 0
			     OR strpos(lower(c.nome), lower(:busca)) > 0
			     OR CAST(os.id AS VARCHAR) = :busca)
			""";

	private static final String FILTRO_STATUS = """
			(CAST(:statusAtendimento AS VARCHAR) IS NULL OR os.status_atendimento = :statusAtendimento)
			""";

	private final JdbcClient jdbc;

	public OrdemServicoRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public long inserir(NovaOrdemServico nova) {
		return jdbc.sql("""
				INSERT INTO ordens_servico (cliente_id, data_abertura, descricao, endereco_atendimento,
				                            contato_atendimento, telefone_atendimento, email_atendimento, criado_por)
				VALUES (:clienteId, coalesce(CAST(:dataAbertura AS TIMESTAMPTZ), now()), :descricao, :endereco,
				        :contato, :telefone, :email, :criadoPor)
				RETURNING id
				""")
				.param("clienteId", nova.clienteId())
				.param("dataAbertura", nova.dataAbertura())
				.param("descricao", nova.descricao())
				.param("endereco", nova.enderecoAtendimento())
				.param("contato", nova.contatoAtendimento())
				.param("telefone", nova.telefoneAtendimento())
				.param("email", nova.emailAtendimento())
				.param("criadoPor", nova.criadoPor())
				.query(Long.class)
				.single();
	}

	public void atualizarDados(long id, String descricao, String endereco, String contato, String telefone,
			String email) {
		jdbc.sql("""
				UPDATE ordens_servico
				SET descricao = :descricao, endereco_atendimento = :endereco, contato_atendimento = :contato,
				    telefone_atendimento = :telefone, email_atendimento = :email
				WHERE id = :id
				""")
				.param("id", id)
				.param("descricao", descricao)
				.param("endereco", endereco)
				.param("contato", contato)
				.param("telefone", telefone)
				.param("email", email)
				.update();
	}

	public void iniciarAtendimento(long id) {
		jdbc.sql("""
				UPDATE ordens_servico
				SET status_atendimento = 'EM_ATENDIMENTO', motivo_aguardando = NULL,
				    data_inicio_atendimento = coalesce(data_inicio_atendimento, now()),
				    data_conclusao = NULL, concluida_por = NULL
				WHERE id = :id
				""")
				.param("id", id)
				.update();
	}

	public void colocarEmEspera(long id, String motivo) {
		jdbc.sql("""
				UPDATE ordens_servico
				SET status_atendimento = 'AGUARDANDO', motivo_aguardando = :motivo
				WHERE id = :id
				""")
				.param("id", id)
				.param("motivo", motivo)
				.update();
	}

	public void concluir(long id, long concluidaPor) {
		jdbc.sql("""
				UPDATE ordens_servico
				SET status_atendimento = 'CONCLUIDA', motivo_aguardando = NULL,
				    data_conclusao = now(), concluida_por = :concluidaPor
				WHERE id = :id
				""")
				.param("id", id)
				.param("concluidaPor", concluidaPor)
				.update();
	}

	public Optional<EstadoOrdemServico> bloquearParaAlteracao(long id) {
		return jdbc.sql("""
				SELECT os.id, os.status_atendimento, coalesce(cb.status, 'NAO_ENVIADA') AS status_cobranca
				FROM ordens_servico os
				LEFT JOIN cobrancas cb ON cb.ordem_servico_id = os.id
				WHERE os.id = :id
				FOR UPDATE OF os
				""")
				.param("id", id)
				.query((rs, linha) -> new EstadoOrdemServico(
						rs.getLong("id"),
						StatusAtendimento.valueOf(rs.getString("status_atendimento")),
						StatusCobranca.valueOf(rs.getString("status_cobranca"))))
				.optional();
	}

	public boolean visivelAoTecnico(long id, long tecnicoId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM ordens_servico os WHERE os.id = :id AND "
				+ VISIVEL_AO_TECNICO + ")")
				.param("id", id)
				.param("tecnicoAtual", tecnicoId)
				.query(Boolean.class)
				.single();
	}

	public boolean atribuidaAoTecnico(long id, long tecnicoId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM ordens_servico os WHERE os.id = :id AND "
				+ ATRIBUIDA_AO_TECNICO + ")")
				.param("id", id)
				.param("tecnicoAtual", tecnicoId)
				.query(Boolean.class)
				.single();
	}

	public Optional<OrdemServicoDetalhe> buscarDetalhe(long id) {
		return jdbc.sql("""
				SELECT os.*, coalesce(cb.status, 'NAO_ENVIADA') AS status_cobranca,
				       c.nome AS cliente_nome, c.contato AS cliente_contato, c.telefone AS cliente_telefone,
				       c.email AS cliente_email, c.endereco AS cliente_endereco,
				       criador.nome AS criado_por_nome, concluinte.nome AS concluida_por_nome
				FROM ordens_servico os
				JOIN clientes c ON c.id = os.cliente_id
				JOIN usuarios criador ON criador.id = os.criado_por
				LEFT JOIN usuarios concluinte ON concluinte.id = os.concluida_por
				LEFT JOIN cobrancas cb ON cb.ordem_servico_id = os.id
				WHERE os.id = :id
				""")
				.param("id", id)
				.query(OrdemServicoRepository::mapearDetalhe)
				.optional();
	}

	public List<ResumoOrdemServico> listarParaGestor(FiltroOrdensServico filtro, Paginacao paginacao) {
		return listar(FILTRO_GESTOR, parametrosGestor(filtro), paginacao);
	}

	public long contarParaGestor(FiltroOrdensServico filtro) {
		return contar(FILTRO_GESTOR, parametrosGestor(filtro));
	}

	public List<ResumoOrdemServico> listarVisiveisAoTecnico(long tecnicoId, StatusAtendimento status,
			Paginacao paginacao) {
		return listar(VISIVEL_AO_TECNICO + " AND " + FILTRO_STATUS, parametrosTecnico(tecnicoId, status), paginacao);
	}

	public long contarVisiveisAoTecnico(long tecnicoId, StatusAtendimento status) {
		return contar(VISIVEL_AO_TECNICO + " AND " + FILTRO_STATUS, parametrosTecnico(tecnicoId, status));
	}

	public List<ResumoOrdemServico> listarDisponiveis(Paginacao paginacao) {
		return listar(DISPONIVEL, Map.of(), paginacao);
	}

	public long contarDisponiveis() {
		return contar(DISPONIVEL, Map.of());
	}

	public List<ResumoOrdemServico> listarDoTecnico(long tecnicoId, StatusAtendimento status, Paginacao paginacao) {
		return listar(DO_TECNICO + " AND " + FILTRO_STATUS, parametrosTecnico(tecnicoId, status), paginacao);
	}

	public long contarDoTecnico(long tecnicoId, StatusAtendimento status) {
		return contar(DO_TECNICO + " AND " + FILTRO_STATUS, parametrosTecnico(tecnicoId, status));
	}

	private List<ResumoOrdemServico> listar(String condicao, Map<String, ?> parametros, Paginacao paginacao) {
		return jdbc.sql("""
				SELECT os.id, os.cliente_id, c.nome AS cliente_nome, os.data_abertura, os.descricao,
				       os.endereco_atendimento, os.status_atendimento, os.data_conclusao,
				       coalesce(cb.status, 'NAO_ENVIADA') AS status_cobranca
				""" + FROM_ORDENS + " WHERE " + condicao + """
				 ORDER BY os.data_abertura DESC, os.id DESC
				 LIMIT :limite OFFSET :deslocamento
				""")
				.params(parametros)
				.param("limite", paginacao.tamanho())
				.param("deslocamento", paginacao.deslocamento())
				.query(OrdemServicoRepository::mapearResumo)
				.list();
	}

	private long contar(String condicao, Map<String, ?> parametros) {
		return jdbc.sql("SELECT count(*)" + FROM_ORDENS + " WHERE " + condicao)
				.params(parametros)
				.query(Long.class)
				.single();
	}

	private static Map<String, Object> parametrosGestor(FiltroOrdensServico filtro) {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("statusAtendimento", nomeOuNulo(filtro.statusAtendimento()));
		parametros.put("statusCobranca", nomeOuNulo(filtro.statusCobranca()));
		parametros.put("clienteId", filtro.clienteId());
		parametros.put("tecnicoId", filtro.tecnicoId());
		parametros.put("busca", filtro.busca());
		return parametros;
	}

	private static Map<String, Object> parametrosTecnico(long tecnicoId, StatusAtendimento status) {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("tecnicoAtual", tecnicoId);
		parametros.put("statusAtendimento", nomeOuNulo(status));
		return parametros;
	}

	private static String nomeOuNulo(Enum<?> valor) {
		return valor == null ? null : valor.name();
	}

	private static ResumoOrdemServico mapearResumo(ResultSet rs, int linha) throws SQLException {
		return new ResumoOrdemServico(
				rs.getLong("id"),
				new UsuarioResumido(rs.getLong("cliente_id"), rs.getString("cliente_nome")),
				rs.getObject("data_abertura", OffsetDateTime.class),
				rs.getString("descricao"),
				rs.getString("endereco_atendimento"),
				StatusAtendimento.valueOf(rs.getString("status_atendimento")),
				rs.getObject("data_conclusao", OffsetDateTime.class),
				StatusCobranca.valueOf(rs.getString("status_cobranca")),
				List.of());
	}

	private static OrdemServicoDetalhe mapearDetalhe(ResultSet rs, int linha) throws SQLException {
		long concluidaPorId = rs.getLong("concluida_por");
		UsuarioResumido concluidaPor = rs.wasNull() ? null
				: new UsuarioResumido(concluidaPorId, rs.getString("concluida_por_nome"));
		return new OrdemServicoDetalhe(
				rs.getLong("id"),
				new ClienteDaOrdem(
						rs.getLong("cliente_id"),
						rs.getString("cliente_nome"),
						rs.getString("cliente_contato"),
						rs.getString("cliente_telefone"),
						rs.getString("cliente_email"),
						rs.getString("cliente_endereco")),
				rs.getObject("data_abertura", OffsetDateTime.class),
				rs.getString("descricao"),
				rs.getString("endereco_atendimento"),
				rs.getString("contato_atendimento"),
				rs.getString("telefone_atendimento"),
				rs.getString("email_atendimento"),
				StatusAtendimento.valueOf(rs.getString("status_atendimento")),
				rs.getString("motivo_aguardando"),
				rs.getObject("data_inicio_atendimento", OffsetDateTime.class),
				rs.getObject("data_conclusao", OffsetDateTime.class),
				concluidaPor,
				StatusCobranca.valueOf(rs.getString("status_cobranca")),
				new UsuarioResumido(rs.getLong("criado_por"), rs.getString("criado_por_nome")),
				rs.getObject("criado_em", OffsetDateTime.class),
				rs.getObject("atualizado_em", OffsetDateTime.class),
				List.of(),
				List.of());
	}

	public record NovaOrdemServico(
			long clienteId,
			OffsetDateTime dataAbertura,
			String descricao,
			String enderecoAtendimento,
			String contatoAtendimento,
			String telefoneAtendimento,
			String emailAtendimento,
			long criadoPor) {
	}

}
