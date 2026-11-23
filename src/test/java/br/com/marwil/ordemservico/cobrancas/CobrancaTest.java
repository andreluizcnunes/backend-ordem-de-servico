package br.com.marwil.ordemservico.cobrancas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.exception.ConflitoException;

class CobrancaTest extends TesteIntegracao {

	@Autowired
	private CobrancaService cobrancas;

	private long adminId;
	private long clienteId;
	private long ordem;
	private String tokenAdmin;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		long carlosId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		clienteId = criarCliente("Condomínio Solar", "Rua A, 100", adminId);
		ordem = criarOrdem(clienteId, "Reparo elétrico", adminId);
		atribuir(ordem, carlosId, adminId);
		marcarConcluida(ordem, carlosId);
		tokenAdmin = token("ana@empresa.com");
	}

	@Test
	void fluxoDeCobrancaEPagamentoGeraUmaUnicaEntradaNoCaixa() throws Exception {
		precificar(ordem);

		String corpo = mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDENTE"))
				.andExpect(jsonPath("$.valorTotal").value(250.50))
				.andExpect(jsonPath("$.cliente.nome").value("Condomínio Solar"))
				.andExpect(jsonPath("$.pagamento").doesNotExist())
				.andReturn().getResponse().getContentAsString();
		long cobrancaId = lerId(corpo);

		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem), tokenAdmin))
				.andExpect(jsonPath("$.statusCobranca").value("PENDENTE"));
		mvc.perform(autenticado(get("/api/v1/cobrancas?status=PENDENTE"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].id").value(cobrancaId));

		mvc.perform(json(post(rotaPagamento(cobrancaId)), tokenAdmin, "{}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PAGA"))
				.andExpect(jsonPath("$.pagamento.valorRecebido").value(250.50))
				.andExpect(jsonPath("$.pagamento.registradoPor.nome").value("Ana"));

		mvc.perform(json(post(rotaPagamento(cobrancaId)), tokenAdmin, "{}"))
				.andExpect(status().isConflict());

		assertThat(contarLancamentos()).isEqualTo(1);
		mvc.perform(autenticado(get("/api/v1/cobrancas?status=PAGA"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1));
		mvc.perform(autenticado(get("/api/v1/ordens-servico?statusCobranca=PAGA"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1));
	}

	@Test
	void pagamentosSimultaneosGeramUmaUnicaEntrada() throws Exception {
		precificar(ordem);
		long cobrancaId = cobrancas.enviar(new UsuarioAutenticado(adminId, "Ana", Perfil.ADMINISTRATIVO), ordem,
				null).id();
		UsuarioAutenticado admin = new UsuarioAutenticado(adminId, "Ana", Perfil.ADMINISTRATIVO);
		int tentativas = 6;
		CountDownLatch largada = new CountDownLatch(1);
		List<Future<Boolean>> resultados = new ArrayList<>();
		try (ExecutorService executor = Executors.newFixedThreadPool(tentativas)) {
			for (int indice = 0; indice < tentativas; indice++) {
				Callable<Boolean> tentativa = () -> {
					largada.await();
					try {
						cobrancas.registrarPagamento(admin, cobrancaId, null, null);
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
		assertThat(contarLancamentos()).isEqualTo(1);
	}

	@Test
	void naoEnviaSemItensNemDuasVezes() throws Exception {
		mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin)).andExpect(status().isConflict());

		precificar(ordem);
		mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin)).andExpect(status().isCreated());
		mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin)).andExpect(status().isConflict());
	}

	@Test
	void naoEnviaOrdemNaoConcluida() throws Exception {
		long aberta = criarOrdem(clienteId, "Aberta", adminId);

		mvc.perform(autenticado(post(rotaEnvio(aberta)), tokenAdmin)).andExpect(status().isConflict());
	}

	@Test
	void naoEnviaOrdemComTotalZero() throws Exception {
		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/itens"), tokenAdmin, """
				{"itens": [{"tipo": "SERVICO", "descricao": "Garantia", "quantidade": 1, "precoUnitario": 0}]}
				""")).andExpect(status().isOk());

		mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin)).andExpect(status().isConflict());
	}

	@Test
	void cobrancaEnviadaFechaItensEImpedeReabertura() throws Exception {
		precificar(ordem);
		mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin)).andExpect(status().isCreated());

		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/itens"), tokenAdmin, """
				{"itens": []}
				""")).andExpect(status().isConflict());
		mvc.perform(json(patch("/api/v1/ordens-servico/" + ordem + "/atendimento"), tokenAdmin, """
				{"status": "EM_ATENDIMENTO"}
				""")).andExpect(status().isConflict());
		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem + "/itens"), tokenAdmin))
				.andExpect(jsonPath("$.editavel").value(false));
	}

	@Test
	void pagamentoParcialNaoEAceito() throws Exception {
		precificar(ordem);
		long cobrancaId = lerId(mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenAdmin))
				.andReturn().getResponse().getContentAsString());

		mvc.perform(json(post(rotaPagamento(cobrancaId)), tokenAdmin, """
				{"valorRecebido": 100.00}
				""")).andExpect(status().isBadRequest());
		assertThat(contarLancamentos()).isZero();
	}

	@Test
	void pendenteSoEVencidaComVencimentoUltrapassado() throws Exception {
		precificar(ordem);
		long cobrancaId = lerId(mvc.perform(json(post(rotaEnvio(ordem)), tokenAdmin, """
				{"dataVencimento": "2999-12-31"}
				""")).andReturn().getResponse().getContentAsString());

		mvc.perform(autenticado(get("/api/v1/cobrancas/" + cobrancaId), tokenAdmin))
				.andExpect(jsonPath("$.vencida").value(false));

		jdbc.sql("UPDATE cobrancas SET data_vencimento = DATE '2000-01-01' WHERE id = :id")
				.param("id", cobrancaId).update();

		mvc.perform(autenticado(get("/api/v1/cobrancas?vencidas=true"), tokenAdmin))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.itens[0].vencida").value(true));
	}

	@Test
	void tecnicoNaoAcessaCobrancas() throws Exception {
		String tokenCarlos = token("carlos@empresa.com");

		mvc.perform(autenticado(get("/api/v1/cobrancas"), tokenCarlos)).andExpect(status().isForbidden());
		mvc.perform(autenticado(post(rotaEnvio(ordem)), tokenCarlos)).andExpect(status().isForbidden());
		mvc.perform(json(post(rotaPagamento(1)), tokenCarlos, "{}")).andExpect(status().isForbidden());
	}

	private void precificar(long ordemId) throws Exception {
		mvc.perform(json(put("/api/v1/ordens-servico/" + ordemId + "/itens"), tokenAdmin, """
				{"itens": [
				  {"tipo": "PRODUTO", "descricao": "Disjuntor 40A", "quantidade": 1, "precoUnitario": 50.50},
				  {"tipo": "MAO_DE_OBRA", "descricao": "Eletricista", "quantidade": 2, "precoUnitario": 100}
				]}
				""")).andExpect(status().isOk());
	}

	private long contarLancamentos() {
		return jdbc.sql("SELECT count(*) FROM lancamentos_caixa").query(Long.class).single();
	}

	private static String rotaEnvio(long ordemId) {
		return "/api/v1/ordens-servico/" + ordemId + "/enviar-cobranca";
	}

	private static String rotaPagamento(long cobrancaId) {
		return "/api/v1/cobrancas/" + cobrancaId + "/pagamentos";
	}

}
