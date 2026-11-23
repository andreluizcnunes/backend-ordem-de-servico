package br.com.marwil.ordemservico.ordensservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.auth.Perfil;
import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.comum.ConflitoException;

class EquipeTest extends TesteIntegracao {

	@Autowired
	private EquipeService equipes;

	private long adminId;
	private long carlosId;
	private long brunoId;
	private long clienteId;
	private String tokenAdmin;
	private String tokenCarlos;
	private String tokenBruno;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		carlosId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		brunoId = criarUsuario("Bruno", "bruno@empresa.com", Perfil.TECNICO);
		clienteId = criarCliente("Condomínio Solar", "Rua A, 100", adminId);
		tokenAdmin = token("ana@empresa.com");
		tokenCarlos = token("carlos@empresa.com");
		tokenBruno = token("bruno@empresa.com");
	}

	@Test
	void tecnicoAssumeOrdemDisponivelESegundaTentativaConflita() throws Exception {
		long ordem = criarOrdem(clienteId, "Disponível", adminId);

		mvc.perform(autenticado(post("/api/v1/ordens-servico/" + ordem + "/assumir"), tokenCarlos))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusAtendimento").value("ABERTA"))
				.andExpect(jsonPath("$.tecnicos[0].nome").value("Carlos"))
				.andExpect(jsonPath("$.tecnicos[0].atribuidoPor.nome").value("Carlos"));

		mvc.perform(autenticado(post("/api/v1/ordens-servico/" + ordem + "/assumir"), tokenBruno))
				.andExpect(status().isConflict());
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), tokenBruno))
				.andExpect(status().isNotFound());
	}

	@Test
	void capturasSimultaneasResultamEmUmUnicoResponsavel() throws Exception {
		long ordem = criarOrdem(clienteId, "Disputada", adminId);
		int quantidade = 8;
		List<UsuarioAutenticado> tecnicos = new ArrayList<>();
		for (int indice = 0; indice < quantidade; indice++) {
			long id = criarUsuario("Técnico " + indice, "tecnico" + indice + "@empresa.com", Perfil.TECNICO);
			tecnicos.add(new UsuarioAutenticado(id, "Técnico " + indice, Perfil.TECNICO));
		}
		CountDownLatch largada = new CountDownLatch(1);
		List<Future<Boolean>> resultados = new ArrayList<>();
		try (ExecutorService executor = Executors.newFixedThreadPool(quantidade)) {
			for (UsuarioAutenticado tecnico : tecnicos) {
				Callable<Boolean> tentativa = () -> {
					largada.await();
					try {
						equipes.assumir(tecnico, ordem);
						return true;
					} catch (ConflitoException conflito) {
						return false;
					}
				};
				resultados.add(executor.submit(tentativa));
			}
			largada.countDown();
			long sucessos = 0;
			for (Future<Boolean> resultado : resultados) {
				if (resultado.get()) {
					sucessos++;
				}
			}
			assertThat(sucessos).isEqualTo(1);
		}
		long atribuicoes = jdbc.sql("SELECT count(*) FROM ordens_servico_tecnicos WHERE ordem_servico_id = :id")
				.param("id", ordem).query(Long.class).single();
		assertThat(atribuicoes).isEqualTo(1);
	}

	@Test
	void gestorDefineEquipeComVariosTecnicosSemLimite() throws Exception {
		long ordem = criarOrdem(clienteId, "Grande obra", adminId);
		long diegoId = criarUsuario("Diego", "diego@empresa.com", Perfil.TECNICO);
		long eduardoId = criarUsuario("Eduardo", "eduardo@empresa.com", Perfil.TECNICO);
		criarUsuario("Fora da equipe", "fora@empresa.com", Perfil.TECNICO);
		atribuir(ordem, carlosId, carlosId);

		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/tecnicos"), tokenAdmin, """
				{"tecnicosIds": [%d, %d, %d, %d]}
				""".formatted(carlosId, brunoId, diegoId, eduardoId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tecnicos.length()").value(4));

		for (String email : List.of("carlos@empresa.com", "bruno@empresa.com", "diego@empresa.com",
				"eduardo@empresa.com")) {
			mvc.perform(autenticado(get("/api/v1/ordens-servico/minhas"), token(email)))
					.andExpect(jsonPath("$.total").value(1))
					.andExpect(jsonPath("$.itens[0].id").value(ordem))
					.andExpect(jsonPath("$.itens[0].tecnicos.length()").value(4));
		}
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), token("fora@empresa.com")))
				.andExpect(status().isNotFound());
		mvc.perform(autenticado(get("/api/v1/ordens-servico/disponiveis"), token("fora@empresa.com")))
				.andExpect(jsonPath("$.total").value(0));
	}

	@Test
	void gestorRemoveTecnicoDaEquipe() throws Exception {
		long ordem = criarOrdem(clienteId, "Manutenção", adminId);
		atribuir(ordem, carlosId, adminId);
		atribuir(ordem, brunoId, adminId);

		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/tecnicos"), tokenAdmin, """
				{"tecnicosIds": [%d]}
				""".formatted(brunoId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tecnicos.length()").value(1))
				.andExpect(jsonPath("$.tecnicos[0].nome").value("Bruno"));

		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), tokenCarlos))
				.andExpect(status().isNotFound());
	}

	@Test
	void equipeNaoAceitaUsuarioQueNaoETecnicoAtivo() throws Exception {
		long ordem = criarOrdem(clienteId, "Manutenção", adminId);
		long inativoId = criarUsuario("Inativo", "inativo@empresa.com", Perfil.TECNICO);
		jdbc.sql("UPDATE usuarios SET ativo = FALSE WHERE id = :id").param("id", inativoId).update();

		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/tecnicos"), tokenAdmin, """
				{"tecnicosIds": [%d]}
				""".formatted(adminId))).andExpect(status().isBadRequest());
		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/tecnicos"), tokenAdmin, """
				{"tecnicosIds": [%d]}
				""".formatted(inativoId))).andExpect(status().isBadRequest());
	}

	@Test
	void equipeNaoMudaAposConclusao() throws Exception {
		long ordem = criarOrdem(clienteId, "Encerrada", adminId);
		marcarConcluida(ordem, adminId);

		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/tecnicos"), tokenAdmin, """
				{"tecnicosIds": [%d]}
				""".formatted(carlosId))).andExpect(status().isConflict());
	}

	@Test
	void aberturaJaComEquipe() throws Exception {
		mvc.perform(json(post("/api/v1/ordens-servico"), tokenAdmin, """
				{"clienteId": %d, "descricao": "Com equipe", "tecnicosIds": [%d, %d]}
				""".formatted(clienteId, carlosId, brunoId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tecnicos.length()").value(2))
				.andExpect(jsonPath("$.tecnicos[0].atribuidoPor.nome").value("Ana"));

		mvc.perform(autenticado(get("/api/v1/ordens-servico/disponiveis"), tokenCarlos))
				.andExpect(jsonPath("$.total").value(0));
	}

	@Test
	void gestorNaoUsaRotaDeCaptura() throws Exception {
		long ordem = criarOrdem(clienteId, "Disponível", adminId);

		mvc.perform(autenticado(post("/api/v1/ordens-servico/" + ordem + "/assumir"), tokenAdmin))
				.andExpect(status().isForbidden());
	}

}
