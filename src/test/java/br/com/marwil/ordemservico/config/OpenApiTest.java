package br.com.marwil.ordemservico.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;

class OpenApiTest extends TesteIntegracao {

	@Test
	void documentacaoPublicaDescreveRotasEAutenticacao() throws Exception {
		mvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/ordens-servico/{id}/assumir']").exists())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}

	@Test
	void interfaceSwaggerEstaDisponivelSemToken() throws Exception {
		mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

}
