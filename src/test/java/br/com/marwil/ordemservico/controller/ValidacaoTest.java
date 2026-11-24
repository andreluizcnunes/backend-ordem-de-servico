package br.com.marwil.ordemservico.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.enums.Perfil;

class ValidacaoTest extends TesteIntegracao {

	private String tokenDono;

	@BeforeEach
	void prepararDono() throws Exception {
		criarUsuario("Dono", "dono@empresa.com", Perfil.DONO);
		tokenDono = token("dono@empresa.com");
	}

	@Test
	void cadastroDeUsuarioInvalidoDescreveCadaCampoEmPortugues() throws Exception {
		mvc.perform(json(post("/api/v1/usuarios"), tokenDono, """
				{"nome": " ", "email": "invalido", "senha": "curta"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Um ou mais campos possuem valores inválidos."))
				.andExpect(jsonPath("$.erros[?(@.campo == 'nome')].mensagem").value(contains("Informe o nome do usuário.")))
				.andExpect(jsonPath("$.erros[?(@.campo == 'email')].mensagem").value(contains("Informe um e-mail válido.")))
				.andExpect(jsonPath("$.erros[?(@.campo == 'senha')].mensagem")
						.value(contains("A senha deve ter entre 8 e 72 caracteres.")))
				.andExpect(jsonPath("$.erros[?(@.campo == 'perfil')].mensagem")
						.value(contains("Informe o perfil: DONO, ADMINISTRATIVO ou TECNICO.")));
	}

	@Test
	void limiteDeTamanhoInformaOMaximoPermitido() throws Exception {
		String nomeLongo = "A".repeat(201);

		mvc.perform(json(post("/api/v1/clientes"), tokenDono, """
				{"nome": "%s"}
				""".formatted(nomeLongo)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"))
				.andExpect(jsonPath("$.erros[0].mensagem").value("O nome do cliente deve ter no máximo 200 caracteres."));
	}

	@Test
	void loginSemCredenciaisPedeEmailESenha() throws Exception {
		mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[?(@.campo == 'email')].mensagem").value(contains("Informe o e-mail.")))
				.andExpect(jsonPath("$.erros[?(@.campo == 'senha')].mensagem").value(contains("Informe a senha.")));
	}

	@Test
	void valorForaDaListaInformaOsValoresAceitos() throws Exception {
		mvc.perform(json(post("/api/v1/usuarios"), tokenDono, """
				{"nome": "Carlos", "email": "carlos@empresa.com", "senha": "senha-forte-1", "perfil": "CHEFE"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("perfil"))
				.andExpect(jsonPath("$.erros[0].mensagem")
						.value("Valor inválido. Valores aceitos: DONO, ADMINISTRATIVO, TECNICO."));
	}

	@Test
	void tipoIncorretoEmItemDeListaIndicaOCaminhoDoCampo() throws Exception {
		mvc.perform(json(put("/api/v1/ordens-servico/1/itens"), tokenDono, """
				{"itens": [{"tipo": "SERVICO", "descricao": "Instalação", "quantidade": "dois", "precoUnitario": 10}]}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("itens[0].quantidade"))
				.andExpect(jsonPath("$.erros[0].mensagem")
						.value("Informe um número válido, sem aspas e com ponto como separador decimal."));
	}

	@Test
	void jsonMalFormadoRetornaMensagemEmPortugues() throws Exception {
		mvc.perform(json(post("/api/v1/clientes"), tokenDono, "{\"nome\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Requisição inválida"))
				.andExpect(jsonPath("$.detail").value("O corpo da requisição está ausente ou não é um JSON válido."))
				.andExpect(jsonPath("$.erros").doesNotExist());
	}

}
