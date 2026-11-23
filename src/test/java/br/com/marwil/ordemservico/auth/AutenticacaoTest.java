package br.com.marwil.ordemservico.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import br.com.marwil.ordemservico.TesteIntegracao;

class AutenticacaoTest extends TesteIntegracao {

	@Test
	void loginValidoRetornaTokenEUsuario() throws Exception {
		criarUsuario("Maria Dona", "maria@empresa.com", Perfil.DONO);

		mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "MARIA@empresa.com", "senha": "%s"}
						""".formatted(SENHA_PADRAO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.usuario.perfil").value("DONO"));
	}

	@Test
	void senhaIncorretaRetornaNaoAutenticado() throws Exception {
		criarUsuario("Maria Dona", "maria@empresa.com", Perfil.DONO);

		mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "maria@empresa.com", "senha": "errada"}
						"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void usuarioInativoNaoConsegueEntrar() throws Exception {
		long id = criarUsuario("Tecnico Inativo", "inativo@empresa.com", Perfil.TECNICO);
		jdbc.sql("UPDATE usuarios SET ativo = FALSE WHERE id = :id").param("id", id).update();

		mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "inativo@empresa.com", "senha": "%s"}
						""".formatted(SENHA_PADRAO)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rotaProtegidaSemTokenRetornaNaoAutenticado() throws Exception {
		mvc.perform(get("/api/v1/auth/eu")).andExpect(status().isUnauthorized());
	}

	@Test
	void tokenDeUsuarioDesativadoDeixaDeValer() throws Exception {
		long id = criarUsuario("Tecnico", "tecnico@empresa.com", Perfil.TECNICO);
		String token = token("tecnico@empresa.com");
		jdbc.sql("UPDATE usuarios SET ativo = FALSE WHERE id = :id").param("id", id).update();

		mvc.perform(autenticado(get("/api/v1/auth/eu"), token)).andExpect(status().isUnauthorized());
	}

	@Test
	void usuarioAtualRetornaPerfil() throws Exception {
		criarUsuario("Ana Administrativa", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		String token = token("ana@empresa.com");

		mvc.perform(autenticado(get("/api/v1/auth/eu"), token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Ana Administrativa"))
				.andExpect(jsonPath("$.perfil").value("ADMINISTRATIVO"));
	}

	@Test
	void alteraPropriaSenhaConferindoASenhaAtual() throws Exception {
		criarUsuario("Tecnico", "tecnico@empresa.com", Perfil.TECNICO);
		String token = token("tecnico@empresa.com");

		mvc.perform(json(put("/api/v1/auth/senha"), token, """
				{"senhaAtual": "incorreta", "novaSenha": "nova-senha-123"}
				""")).andExpect(status().isBadRequest());

		mvc.perform(json(put("/api/v1/auth/senha"), token, """
				{"senhaAtual": "%s", "novaSenha": "nova-senha-123"}
				""".formatted(SENHA_PADRAO))).andExpect(status().isNoContent());

		mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "tecnico@empresa.com", "senha": "nova-senha-123"}
						"""))
				.andExpect(status().isOk());
	}

}
