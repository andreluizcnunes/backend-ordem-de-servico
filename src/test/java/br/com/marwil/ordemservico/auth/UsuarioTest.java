package br.com.marwil.ordemservico.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.marwil.ordemservico.TesteIntegracao;

class UsuarioTest extends TesteIntegracao {

	private long donoId;
	private String tokenDono;

	@BeforeEach
	void prepararDono() throws Exception {
		donoId = criarUsuario("Dono", "dono@empresa.com", Perfil.DONO);
		tokenDono = token("dono@empresa.com");
	}

	@Test
	void donoCadastraUsuarioSemExporSenha() throws Exception {
		mvc.perform(json(post("/api/v1/usuarios"), tokenDono, """
				{"nome": "Carlos Técnico", "email": "carlos@empresa.com", "senha": "senha-forte-1", "perfil": "TECNICO"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.perfil").value("TECNICO"))
				.andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist());
	}

	@Test
	void emailDuplicadoIgnorandoMaiusculasRetornaConflito() throws Exception {
		criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);

		mvc.perform(json(post("/api/v1/usuarios"), tokenDono, """
				{"nome": "Outro", "email": "CARLOS@empresa.com", "senha": "senha-forte-1", "perfil": "TECNICO"}
				""")).andExpect(status().isConflict());
	}

	@Test
	void cadastroInvalidoRetornaErrosPorCampo() throws Exception {
		mvc.perform(json(post("/api/v1/usuarios"), tokenDono, """
				{"nome": "", "email": "invalido", "senha": "curta"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(4));
	}

	@Test
	void administrativoListaTecnicosMasNaoCadastraUsuarios() throws Exception {
		criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		criarUsuario("Bruno", "bruno@empresa.com", Perfil.TECNICO);
		String tokenAdmin = token("ana@empresa.com");

		mvc.perform(autenticado(get("/api/v1/usuarios?perfil=TECNICO"), tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2))
				.andExpect(jsonPath("$.itens[0].nome").value("Bruno"));

		mvc.perform(json(post("/api/v1/usuarios"), tokenAdmin, """
				{"nome": "Novo", "email": "novo@empresa.com", "senha": "senha-forte-1", "perfil": "DONO"}
				""")).andExpect(status().isForbidden());
	}

	@Test
	void tecnicoNaoListaUsuarios() throws Exception {
		criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);

		mvc.perform(autenticado(get("/api/v1/usuarios"), token("carlos@empresa.com")))
				.andExpect(status().isForbidden());
	}

	@Test
	void donoNaoPodeSeDesativar() throws Exception {
		mvc.perform(json(put("/api/v1/usuarios/" + donoId), tokenDono, """
				{"nome": "Dono", "email": "dono@empresa.com", "perfil": "DONO", "ativo": false}
				""")).andExpect(status().isConflict());
	}

	@Test
	void donoDesativaTecnico() throws Exception {
		long tecnicoId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);

		mvc.perform(json(put("/api/v1/usuarios/" + tecnicoId), tokenDono, """
				{"nome": "Carlos Silva", "email": "carlos@empresa.com", "perfil": "TECNICO", "ativo": false}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Carlos Silva"))
				.andExpect(jsonPath("$.ativo").value(false));
	}

	@Test
	void donoRedefineSenhaDeUsuario() throws Exception {
		long tecnicoId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);

		mvc.perform(json(put("/api/v1/usuarios/" + tecnicoId + "/senha"), tokenDono, """
				{"novaSenha": "outra-senha-99"}
				""")).andExpect(status().isNoContent());

		mvc.perform(json(post("/api/v1/auth/login"), tokenDono, """
				{"email": "carlos@empresa.com", "senha": "outra-senha-99"}
				""")).andExpect(status().isOk());
	}

}
