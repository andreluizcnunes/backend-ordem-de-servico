package br.com.marwil.ordemservico.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;

class CorsTest extends TesteIntegracao {

	@Test
	void liberaOrigemConfigurada() throws Exception {
		mvc.perform(options("/api/v1/ordens-servico")
				.header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "GET")
				.header("Access-Control-Request-Headers", "authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
	}

	@Test
	void recusaOrigemDesconhecida() throws Exception {
		mvc.perform(options("/api/v1/ordens-servico")
				.header("Origin", "http://desconhecida.example")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden());
	}

}
