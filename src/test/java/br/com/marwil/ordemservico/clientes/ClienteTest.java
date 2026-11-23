package br.com.marwil.ordemservico.clientes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.enums.Perfil;

class ClienteTest extends TesteIntegracao {

	private String tokenAdmin;

	@BeforeEach
	void prepararUsuarios() throws Exception {
		criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		tokenAdmin = token("ana@empresa.com");
	}

	@Test
	void administrativoCadastraEAtualizaCliente() throws Exception {
		String corpo = mvc.perform(json(post("/api/v1/clientes"), tokenAdmin, """
				{"nome": "  Condomínio Solar  ", "contato": "Síndico João", "telefone": "",
				 "email": "solar@condominio.com", "endereco": "Rua A, 100"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Condomínio Solar"))
				.andExpect(jsonPath("$.telefone").doesNotExist())
				.andReturn().getResponse().getContentAsString();
		long id = lerId(corpo);

		mvc.perform(json(put("/api/v1/clientes/" + id), tokenAdmin, """
				{"nome": "Condomínio Solar II", "telefone": "(11) 99999-0000"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Condomínio Solar II"))
				.andExpect(jsonPath("$.telefone").value("(11) 99999-0000"))
				.andExpect(jsonPath("$.endereco").doesNotExist());
	}

	@Test
	void buscaClientesPorTrechoDoNome() throws Exception {
		mvc.perform(json(post("/api/v1/clientes"), tokenAdmin, """
				{"nome": "Padaria Central"}
				""")).andExpect(status().isCreated());
		mvc.perform(json(post("/api/v1/clientes"), tokenAdmin, """
				{"nome": "Mercado Bom Preço"}
				""")).andExpect(status().isCreated());

		mvc.perform(autenticado(get("/api/v1/clientes?busca=central"), tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].nome").value("Padaria Central"));
	}

	@Test
	void clienteInexistenteRetornaNaoEncontrado() throws Exception {
		mvc.perform(autenticado(get("/api/v1/clientes/999"), tokenAdmin)).andExpect(status().isNotFound());
	}

	@Test
	void tecnicoNaoAcessaCadastroDeClientes() throws Exception {
		criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);

		mvc.perform(autenticado(get("/api/v1/clientes"), token("carlos@empresa.com")))
				.andExpect(status().isForbidden());
	}

}
