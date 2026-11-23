package br.com.marwil.ordemservico.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.enums.Perfil;

class FinanceiroTest extends TesteIntegracao {

	private long adminId;
	private long tecnicoId;
	private String tokenAdmin;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.DONO);
		tecnicoId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		tokenAdmin = token("ana@empresa.com");
	}

	@Test
	void separaRecebidoPendenteEVencidoPorCliente() throws Exception {
		long solar = criarCliente("Condomínio Solar", "Rua A", adminId);
		long padaria = criarCliente("Padaria Central", "Rua B", adminId);
		long paga = enviarCobranca(solar, "100.00");
		long vencida = enviarCobranca(solar, "50.00");
		enviarCobranca(padaria, "30.00");
		mvc.perform(json(post("/api/v1/cobrancas/" + paga + "/pagamentos"), tokenAdmin, "{}"))
				.andExpect(status().isCreated());
		jdbc.sql("UPDATE cobrancas SET data_vencimento = DATE '2000-01-01' WHERE id = :id")
				.param("id", vencida).update();

		mvc.perform(autenticado(get("/api/v1/financeiro/resumo"), tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalRecebido").value(100.00))
				.andExpect(jsonPath("$.quantidadeRecebimentos").value(1))
				.andExpect(jsonPath("$.totalPendente").value(80.00))
				.andExpect(jsonPath("$.quantidadePendentes").value(2))
				.andExpect(jsonPath("$.totalVencido").value(50.00))
				.andExpect(jsonPath("$.quantidadeVencidas").value(1))
				.andExpect(jsonPath("$.porCliente.length()").value(2))
				.andExpect(jsonPath("$.porCliente[0].cliente.nome").value("Condomínio Solar"))
				.andExpect(jsonPath("$.porCliente[0].totalRecebido").value(100.00))
				.andExpect(jsonPath("$.porCliente[0].totalPendente").value(50.00))
				.andExpect(jsonPath("$.porCliente[0].possuiVencidas").value(true))
				.andExpect(jsonPath("$.porCliente[1].cliente.nome").value("Padaria Central"))
				.andExpect(jsonPath("$.porCliente[1].possuiVencidas").value(false));

		mvc.perform(autenticado(get("/api/v1/financeiro/lancamentos"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].valor").value(100.00))
				.andExpect(jsonPath("$.itens[0].tipo").value("ENTRADA"))
				.andExpect(jsonPath("$.itens[0].origem").value("ORDEM_SERVICO"))
				.andExpect(jsonPath("$.itens[0].cliente.nome").value("Condomínio Solar"));
	}

	@Test
	void filtraRecebimentosPorPeriodo() throws Exception {
		long cliente = criarCliente("Condomínio Solar", "Rua A", adminId);
		long cobranca = enviarCobranca(cliente, "100.00");
		mvc.perform(json(post("/api/v1/cobrancas/" + cobranca + "/pagamentos"), tokenAdmin, "{}"))
				.andExpect(status().isCreated());
		LocalDate amanha = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1);

		mvc.perform(autenticado(get("/api/v1/financeiro/resumo?inicio=" + amanha), tokenAdmin))
				.andExpect(jsonPath("$.totalRecebido").value(0))
				.andExpect(jsonPath("$.quantidadeRecebimentos").value(0));
		mvc.perform(autenticado(get("/api/v1/financeiro/resumo?inicio=2030-01-02&fim=2030-01-01"), tokenAdmin))
				.andExpect(status().isBadRequest());
	}

	@Test
	void tecnicoNaoAcessaFinanceiro() throws Exception {
		String tokenTecnico = token("carlos@empresa.com");

		mvc.perform(autenticado(get("/api/v1/financeiro/resumo"), tokenTecnico)).andExpect(status().isForbidden());
		mvc.perform(autenticado(get("/api/v1/financeiro/lancamentos"), tokenTecnico))
				.andExpect(status().isForbidden());
	}

	private long enviarCobranca(long clienteId, String valor) throws Exception {
		long ordem = criarOrdem(clienteId, "Serviço", adminId);
		atribuir(ordem, tecnicoId, adminId);
		marcarConcluida(ordem, tecnicoId);
		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/itens"), tokenAdmin, """
				{"itens": [{"tipo": "SERVICO", "descricao": "Serviço", "quantidade": 1, "precoUnitario": %s}]}
				""".formatted(valor))).andExpect(status().isOk());
		return lerId(mvc.perform(autenticado(post("/api/v1/ordens-servico/" + ordem + "/enviar-cobranca"),
				tokenAdmin)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
	}

}
