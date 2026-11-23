package br.com.marwil.ordemservico;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import br.com.marwil.ordemservico.auth.Perfil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class TesteIntegracao {

	protected static final String SENHA_PADRAO = "senha-segura-123";

	private static String hashSenhaPadrao;

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected JdbcClient jdbc;

	@Autowired
	private PasswordEncoder codificador;

	@BeforeEach
	void limparBanco() {
		jdbc.sql("""
				TRUNCATE lancamentos_caixa, pagamentos, cobrancas, itens_ordem_servico, registros_atendimento,
				         ordens_servico_tecnicos, ordens_servico, clientes, usuarios
				RESTART IDENTITY CASCADE
				""").update();
	}

	protected long criarUsuario(String nome, String email, Perfil perfil) {
		if (hashSenhaPadrao == null) {
			hashSenhaPadrao = codificador.encode(SENHA_PADRAO);
		}
		return jdbc.sql("""
				INSERT INTO usuarios (nome, email, senha_hash, perfil)
				VALUES (:nome, :email, :hash, :perfil)
				RETURNING id
				""")
				.param("nome", nome)
				.param("email", email)
				.param("hash", hashSenhaPadrao)
				.param("perfil", perfil.name())
				.query(Long.class)
				.single();
	}

	protected long criarCliente(String nome, String endereco, long criadoPor) {
		return jdbc.sql("""
				INSERT INTO clientes (nome, contato, telefone, endereco, criado_por)
				VALUES (:nome, 'Contato do cliente', '(11) 3333-4444', :endereco, :criadoPor)
				RETURNING id
				""")
				.param("nome", nome)
				.param("endereco", endereco)
				.param("criadoPor", criadoPor)
				.query(Long.class)
				.single();
	}

	protected long criarOrdem(long clienteId, String descricao, long criadoPor) {
		return jdbc.sql("""
				INSERT INTO ordens_servico (cliente_id, descricao, endereco_atendimento, criado_por)
				VALUES (:clienteId, :descricao, 'Rua do Atendimento, 10', :criadoPor)
				RETURNING id
				""")
				.param("clienteId", clienteId)
				.param("descricao", descricao)
				.param("criadoPor", criadoPor)
				.query(Long.class)
				.single();
	}

	protected void atribuir(long ordemId, long tecnicoId, long atribuidoPor) {
		jdbc.sql("""
				INSERT INTO ordens_servico_tecnicos (ordem_servico_id, tecnico_id, atribuido_por)
				VALUES (:ordemId, :tecnicoId, :atribuidoPor)
				""")
				.param("ordemId", ordemId)
				.param("tecnicoId", tecnicoId)
				.param("atribuidoPor", atribuidoPor)
				.update();
	}

	protected void marcarConcluida(long ordemId, long concluidaPor) {
		jdbc.sql("""
				UPDATE ordens_servico
				SET status_atendimento = 'CONCLUIDA', data_inicio_atendimento = now(), data_conclusao = now(),
				    concluida_por = :concluidaPor
				WHERE id = :id
				""")
				.param("id", ordemId)
				.param("concluidaPor", concluidaPor)
				.update();
	}

	protected String token(String email) throws Exception {
		String corpo = mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "senha": "%s"}
						""".formatted(email, SENHA_PADRAO)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(corpo, "$.token");
	}

	protected static MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder requisicao, String token) {
		return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
	}

	protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder requisicao, String token,
			String corpo) {
		return autenticado(requisicao, token).contentType(MediaType.APPLICATION_JSON).content(corpo);
	}

	protected static long lerId(String corpo) {
		return ((Number) JsonPath.read(corpo, "$.id")).longValue();
	}

}
