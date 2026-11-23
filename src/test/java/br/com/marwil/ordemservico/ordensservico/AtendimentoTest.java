package br.com.marwil.ordemservico.ordensservico;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.enums.Perfil;

class AtendimentoTest extends TesteIntegracao {

	private long adminId;
	private long carlosId;
	private long ordem;
	private String tokenAdmin;
	private String tokenCarlos;
	private String tokenBruno;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		carlosId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		criarUsuario("Bruno", "bruno@empresa.com", Perfil.TECNICO);
		long clienteId = criarCliente("Condomínio Solar", "Rua A, 100", adminId);
		ordem = criarOrdem(clienteId, "Reparo elétrico", adminId);
		atribuir(ordem, carlosId, adminId);
		tokenAdmin = token("ana@empresa.com");
		tokenCarlos = token("carlos@empresa.com");
		tokenBruno = token("bruno@empresa.com");
	}

	@Test
	void tecnicoRegistraMateriaisImpedeRetomaEConclui() throws Exception {
		alterar(tokenCarlos, """
				{"status": "EM_ATENDIMENTO"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusAtendimento").value("EM_ATENDIMENTO"))
				.andExpect(jsonPath("$.dataInicioAtendimento").isNotEmpty());

		mvc.perform(json(post("/api/v1/ordens-servico/" + ordem + "/registros"), tokenCarlos, """
				{"descricao": "Troquei a fiação do quadro", "materiaisUtilizados": "1 metro de fio isolante"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("ATIVIDADE"))
				.andExpect(jsonPath("$.materiaisUtilizados").value("1 metro de fio isolante"))
				.andExpect(jsonPath("$.autor.nome").value("Carlos"));

		alterar(tokenCarlos, """
				{"status": "AGUARDANDO", "motivo": "Falta disjuntor de 40A"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusAtendimento").value("AGUARDANDO"))
				.andExpect(jsonPath("$.motivoAguardando").value("Falta disjuntor de 40A"));

		alterar(tokenCarlos, """
				{"status": "EM_ATENDIMENTO"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.motivoAguardando").doesNotExist());

		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem + "/registros"), tokenCarlos))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].tipo").value("IMPEDIMENTO"))
				.andExpect(jsonPath("$[1].descricao").value("Falta disjuntor de 40A"));

		alterar(tokenCarlos, """
				{"status": "CONCLUIDA"}
				""").andExpect(status().isNoContent());

		mvc.perform(autenticado(get("/api/v1/ordens-servico/minhas"), tokenCarlos))
				.andExpect(jsonPath("$.total").value(0));
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), tokenAdmin))
				.andExpect(jsonPath("$.statusAtendimento").value("CONCLUIDA"))
				.andExpect(jsonPath("$.concluidaPor.nome").value("Carlos"))
				.andExpect(jsonPath("$.registros.length()").value(2));
	}

	@Test
	void esperaExigeMotivo() throws Exception {
		alterar(tokenCarlos, """
				{"status": "AGUARDANDO", "motivo": "   "}
				""").andExpect(status().isBadRequest());
	}

	@Test
	void transicaoInvalidaRetornaConflito() throws Exception {
		alterar(tokenCarlos, """
				{"status": "CONCLUIDA"}
				""").andExpect(status().isConflict());
		alterar(tokenCarlos, """
				{"status": "ABERTA"}
				""").andExpect(status().isConflict());
	}

	@Test
	void tecnicoDeOutraEquipeNaoAlteraNemRegistra() throws Exception {
		alterar(tokenBruno, """
				{"status": "EM_ATENDIMENTO"}
				""").andExpect(status().isNotFound());
		mvc.perform(json(post("/api/v1/ordens-servico/" + ordem + "/registros"), tokenBruno, """
				{"descricao": "Tentativa"}
				""")).andExpect(status().isNotFound());
	}

	@Test
	void tecnicoPrecisaAssumirAntesDeAlterarOrdemDisponivel() throws Exception {
		long clienteId = criarCliente("Outro", "Rua B", adminId);
		long disponivel = criarOrdem(clienteId, "Disponível", adminId);

		mvc.perform(json(patch("/api/v1/ordens-servico/" + disponivel + "/atendimento"), tokenBruno, """
				{"status": "EM_ATENDIMENTO"}
				""")).andExpect(status().isForbidden());
	}

	@Test
	void somenteGestorReabreOrdemConcluida() throws Exception {
		alterar(tokenCarlos, """
				{"status": "EM_ATENDIMENTO"}
				""").andExpect(status().isOk());
		alterar(tokenCarlos, """
				{"status": "CONCLUIDA"}
				""").andExpect(status().isNoContent());

		alterar(tokenCarlos, """
				{"status": "EM_ATENDIMENTO"}
				""").andExpect(status().isNotFound());
		alterar(tokenAdmin, """
				{"status": "EM_ATENDIMENTO"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusAtendimento").value("EM_ATENDIMENTO"))
				.andExpect(jsonPath("$.dataConclusao").doesNotExist());
	}

	private ResultActions alterar(String token, String corpo) throws Exception {
		return mvc.perform(json(patch("/api/v1/ordens-servico/" + ordem + "/atendimento"), token, corpo));
	}

}
