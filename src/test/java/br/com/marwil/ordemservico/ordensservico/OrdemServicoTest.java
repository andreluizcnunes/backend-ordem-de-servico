package br.com.marwil.ordemservico.ordensservico;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.auth.Perfil;

class OrdemServicoTest extends TesteIntegracao {

	private long adminId;
	private long carlosId;
	private long brunoId;
	private long clienteId;
	private String tokenAdmin;
	private String tokenCarlos;
	private String tokenBruno;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		carlosId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		brunoId = criarUsuario("Bruno", "bruno@empresa.com", Perfil.TECNICO);
		clienteId = criarCliente("Condomínio Solar", "Rua A, 100", adminId);
		tokenAdmin = token("ana@empresa.com");
		tokenCarlos = token("carlos@empresa.com");
		tokenBruno = token("bruno@empresa.com");
	}

	@Test
	void administrativoAbreOrdemCopiandoContatoDoCliente() throws Exception {
		mvc.perform(json(post("/api/v1/ordens-servico"), tokenAdmin, """
				{"clienteId": %d, "descricao": "Trocar disjuntor do hall"}
				""".formatted(clienteId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.statusAtendimento").value("ABERTA"))
				.andExpect(jsonPath("$.statusCobranca").value("NAO_ENVIADA"))
				.andExpect(jsonPath("$.enderecoAtendimento").value("Rua A, 100"))
				.andExpect(jsonPath("$.contatoAtendimento").value("Contato do cliente"))
				.andExpect(jsonPath("$.cliente.nome").value("Condomínio Solar"))
				.andExpect(jsonPath("$.criadoPor.nome").value("Ana"))
				.andExpect(jsonPath("$.tecnicos.length()").value(0));
	}

	@Test
	void aberturaSemEnderecoDisponivelRetornaRequisicaoInvalida() throws Exception {
		long semEndereco = jdbc.sql("""
				INSERT INTO clientes (nome, criado_por) VALUES ('Sem Endereço', :criadoPor) RETURNING id
				""").param("criadoPor", adminId).query(Long.class).single();

		mvc.perform(json(post("/api/v1/ordens-servico"), tokenAdmin, """
				{"clienteId": %d, "descricao": "Vistoria"}
				""".formatted(semEndereco))).andExpect(status().isBadRequest());
	}

	@Test
	void tecnicoNaoAbreOrdem() throws Exception {
		mvc.perform(json(post("/api/v1/ordens-servico"), tokenCarlos, """
				{"clienteId": %d, "descricao": "Tentativa"}
				""".formatted(clienteId))).andExpect(status().isForbidden());
	}

	@Test
	void tecnicoVeDisponiveisESuasOrdensMasNaoAsDeOutros() throws Exception {
		long disponivel = criarOrdem(clienteId, "Disponível", adminId);
		long doCarlos = criarOrdem(clienteId, "Do Carlos", adminId);
		long doBruno = criarOrdem(clienteId, "Do Bruno", adminId);
		atribuir(doCarlos, carlosId, adminId);
		atribuir(doBruno, brunoId, adminId);

		mvc.perform(autenticado(get("/api/v1/ordens-servico"), tokenCarlos))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2))
				.andExpect(jsonPath("$.itens[0].id").value(doCarlos))
				.andExpect(jsonPath("$.itens[0].statusCobranca").doesNotExist())
				.andExpect(jsonPath("$.itens[1].id").value(disponivel));

		mvc.perform(autenticado(get("/api/v1/ordens-servico/disponiveis"), tokenCarlos))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(disponivel));

		mvc.perform(autenticado(get("/api/v1/ordens-servico/minhas"), tokenCarlos))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(doCarlos))
				.andExpect(jsonPath("$.itens[0].tecnicos[0].nome").value("Carlos"));

		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + doBruno), tokenCarlos))
				.andExpect(status().isNotFound());
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + doCarlos), tokenCarlos))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusCobranca").doesNotExist());
	}

	@Test
	void ordemConcluidaSomeParaOTecnicoQueParticipou() throws Exception {
		long ordem = criarOrdem(clienteId, "Concluída", adminId);
		atribuir(ordem, carlosId, adminId);
		marcarConcluida(ordem, carlosId);

		mvc.perform(autenticado(get("/api/v1/ordens-servico"), tokenCarlos))
				.andExpect(jsonPath("$.total").value(0));
		mvc.perform(autenticado(get("/api/v1/ordens-servico/minhas"), tokenCarlos))
				.andExpect(jsonPath("$.total").value(0));
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), tokenCarlos))
				.andExpect(status().isNotFound());
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusAtendimento").value("CONCLUIDA"))
				.andExpect(jsonPath("$.concluidaPor.nome").value("Carlos"));
	}

	@Test
	void gestorFiltraPorStatusTecnicoEBusca() throws Exception {
		long primeira = criarOrdem(clienteId, "Pintura da fachada", adminId);
		long segunda = criarOrdem(clienteId, "Troca de lâmpadas", adminId);
		atribuir(segunda, brunoId, adminId);
		marcarConcluida(primeira, adminId);

		mvc.perform(autenticado(get("/api/v1/ordens-servico?statusAtendimento=CONCLUIDA"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(primeira))
				.andExpect(jsonPath("$.itens[0].statusCobranca").value("NAO_ENVIADA"));

		mvc.perform(autenticado(get("/api/v1/ordens-servico?tecnicoId=" + brunoId), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(segunda));

		mvc.perform(autenticado(get("/api/v1/ordens-servico?busca=lâmpadas"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(segunda));
	}

	@Test
	void gestorEditaDadosDaOrdem() throws Exception {
		long ordem = criarOrdem(clienteId, "Original", adminId);

		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem), tokenAdmin, """
				{"descricao": "Revisada", "enderecoAtendimento": "Bloco B", "telefoneAtendimento": "1199"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.descricao").value("Revisada"))
				.andExpect(jsonPath("$.enderecoAtendimento").value("Bloco B"))
				.andExpect(jsonPath("$.telefoneAtendimento").value("1199"));
	}

	@Test
	void ordemInexistenteRetornaNaoEncontrado() throws Exception {
		mvc.perform(autenticado(get("/api/v1/ordens-servico/9999"), tokenAdmin)).andExpect(status().isNotFound());
		mvc.perform(autenticado(get("/api/v1/ordens-servico/9999"), tokenBruno)).andExpect(status().isNotFound());
	}

}
