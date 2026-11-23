package br.com.marwil.ordemservico.ordensservico;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.enums.Perfil;

class PrecificacaoTest extends TesteIntegracao {

	private long adminId;
	private long ordem;
	private String tokenAdmin;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		long carlosId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		long clienteId = criarCliente("Condomínio Solar", "Rua A, 100", adminId);
		ordem = criarOrdem(clienteId, "Reparo elétrico", adminId);
		atribuir(ordem, carlosId, adminId);
		marcarConcluida(ordem, carlosId);
		tokenAdmin = token("ana@empresa.com");
	}

	@Test
	void calculaSubtotaisETotalComDecimais() throws Exception {
		mvc.perform(json(put(rotaItens()), tokenAdmin, """
				{"itens": [
				  {"tipo": "PRODUTO", "descricao": "Fio isolante 2,5mm", "quantidade": 2.5, "precoUnitario": 10.33},
				  {"tipo": "PRODUTO", "descricao": "Fita isolante", "quantidade": 3, "precoUnitario": 0.10},
				  {"tipo": "SERVICO", "descricao": "Troca de quadro", "localRealizacao": "Hall", "quantidade": 1,
				   "precoUnitario": 0},
				  {"tipo": "MAO_DE_OBRA", "descricao": "Eletricista", "quantidade": 1, "precoUnitario": 150}
				]}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itens.length()").value(4))
				.andExpect(jsonPath("$.itens[0].valorTotal").value(25.83))
				.andExpect(jsonPath("$.itens[1].valorTotal").value(0.30))
				.andExpect(jsonPath("$.itens[2].valorTotal").value(0.00))
				.andExpect(jsonPath("$.itens[2].localRealizacao").value("Hall"))
				.andExpect(jsonPath("$.totalProdutos").value(26.13))
				.andExpect(jsonPath("$.totalServicos").value(0.00))
				.andExpect(jsonPath("$.totalMaoDeObra").value(150.00))
				.andExpect(jsonPath("$.total").value(176.13))
				.andExpect(jsonPath("$.editavel").value(true));
	}

	@Test
	void precoEmBrancoNaoViraZero() throws Exception {
		mvc.perform(json(put(rotaItens()), tokenAdmin, """
				{"itens": [{"tipo": "PRODUTO", "descricao": "Disjuntor", "quantidade": 1}]}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("itens[0].precoUnitario"));

		mvc.perform(autenticado(get(rotaItens()), tokenAdmin))
				.andExpect(jsonPath("$.itens.length()").value(0));
	}

	@Test
	void rejeitaQuantidadeZeroEPrecoNegativo() throws Exception {
		mvc.perform(json(put(rotaItens()), tokenAdmin, """
				{"itens": [{"tipo": "PRODUTO", "descricao": "Item", "quantidade": 0, "precoUnitario": -1}]}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(2));
	}

	@Test
	void editaERemoveItensExistentes() throws Exception {
		String corpo = mvc.perform(json(put(rotaItens()), tokenAdmin, """
				{"itens": [
				  {"tipo": "PRODUTO", "descricao": "Lâmpada", "quantidade": 4, "precoUnitario": 12.00},
				  {"tipo": "SERVICO", "descricao": "Instalação", "quantidade": 1, "precoUnitario": 80.00}
				]}
				""")).andReturn().getResponse().getContentAsString();
		int lampadaId = JsonPath.read(corpo, "$.itens[0].id");

		mvc.perform(json(put(rotaItens()), tokenAdmin, """
				{"itens": [{"id": %d, "tipo": "PRODUTO", "descricao": "Lâmpada LED", "quantidade": 5,
				            "precoUnitario": 12.00}]}
				""".formatted(lampadaId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itens.length()").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(lampadaId))
				.andExpect(jsonPath("$.itens[0].descricao").value("Lâmpada LED"))
				.andExpect(jsonPath("$.total").value(60.00));
	}

	@Test
	void rejeitaItemDeOutraOrdem() throws Exception {
		mvc.perform(json(put(rotaItens()), tokenAdmin, """
				{"itens": [{"id": 999, "tipo": "PRODUTO", "descricao": "Item", "quantidade": 1, "precoUnitario": 1}]}
				""")).andExpect(status().isBadRequest());
	}

	@Test
	void naoPrecificaOrdemAindaEmExecucao() throws Exception {
		long clienteId = criarCliente("Outro", "Rua B", adminId);
		long aberta = criarOrdem(clienteId, "Aberta", adminId);

		mvc.perform(json(put("/api/v1/ordens-servico/" + aberta + "/itens"), tokenAdmin, """
				{"itens": [{"tipo": "PRODUTO", "descricao": "Item", "quantidade": 1, "precoUnitario": 1}]}
				""")).andExpect(status().isConflict());
	}

	@Test
	void tecnicoNaoConsultaNemAlteraPrecos() throws Exception {
		String tokenCarlos = token("carlos@empresa.com");

		mvc.perform(autenticado(get(rotaItens()), tokenCarlos)).andExpect(status().isForbidden());
		mvc.perform(json(put(rotaItens()), tokenCarlos, """
				{"itens": []}
				""")).andExpect(status().isForbidden());
	}

	private String rotaItens() {
		return "/api/v1/ordens-servico/" + ordem + "/itens";
	}

}
