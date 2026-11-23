package br.com.marwil.ordemservico.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;

class ActuatorTest extends TesteIntegracao {

	@Test
	void saudePublicaIndicaAplicacaoNoAr() throws Exception {
		mvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
		mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
	}

	@Test
	void demaisEndpointsDeGestaoNaoSaoPublicos() throws Exception {
		mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
	}

}
