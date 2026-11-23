package br.com.marwil.ordemservico.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.StringJoiner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.http.MediaType;

import br.com.marwil.ordemservico.TesteIntegracao;
import br.com.marwil.ordemservico.enums.Perfil;

class DocumentoTest extends TesteIntegracao {

	private long adminId;
	private long carlosId;
	private long clienteId;
	private String tokenAdmin;
	private String tokenCarlos;

	@BeforeEach
	void prepararCenario() throws Exception {
		adminId = criarUsuario("Ana", "ana@empresa.com", Perfil.ADMINISTRATIVO);
		carlosId = criarUsuario("Carlos", "carlos@empresa.com", Perfil.TECNICO);
		criarUsuario("Bruno", "bruno@empresa.com", Perfil.TECNICO);
		clienteId = criarCliente("Condomínio Solar", "Rua A, 100", adminId);
		tokenAdmin = token("ana@empresa.com");
		tokenCarlos = token("carlos@empresa.com");
	}

	@Test
	void gestorRecebePdfComProdutosServicosETotal() throws Exception {
		long ordem = criarOrdemConcluida();
		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/itens"), tokenAdmin, """
				{"itens": [
				  {"tipo": "PRODUTO", "descricao": "Disjuntor 40A", "quantidade": 2, "precoUnitario": 45.90},
				  {"tipo": "SERVICO", "descricao": "Troca do quadro", "localRealizacao": "Hall de entrada",
				   "quantidade": 1, "precoUnitario": 200},
				  {"tipo": "MAO_DE_OBRA", "descricao": "Eletricista", "quantidade": 3, "precoUnitario": 80}
				]}
				""")).andExpect(status().isOk());

		byte[] pdf = baixarPdf(ordem, tokenAdmin);
		String texto = extrairTexto(pdf);

		assertThat(texto).contains("ORDEM DE SERVIÇO Nº " + ordem, "Condomínio Solar", "Disjuntor 40A",
				"Troca do quadro", "Hall de entrada", "Mão de obra: Eletricista", "Carlos", "R$ 531,80",
				"Recibo de material", "Recibo de serviço");
	}

	@Test
	void tecnicoRecebePdfSemValores() throws Exception {
		long ordem = criarOrdem(clienteId, "Vistoria elétrica", adminId);
		atribuir(ordem, carlosId, adminId);

		String texto = extrairTexto(baixarPdf(ordem, tokenCarlos));

		assertThat(texto).contains("Vistoria elétrica", "Produtos / materiais", "Carlos").doesNotContain("R$", "Valor");
	}

	@Test
	void muitosItensContinuamNaPaginaSeguinte() throws Exception {
		long ordem = criarOrdemConcluida();
		StringJoiner itens = new StringJoiner(",");
		for (int indice = 1; indice <= 60; indice++) {
			itens.add("""
					{"tipo": "PRODUTO", "descricao": "Material %d", "quantidade": 1, "precoUnitario": 1}
					""".formatted(indice));
		}
		mvc.perform(json(put("/api/v1/ordens-servico/" + ordem + "/itens"), tokenAdmin,
				"{\"itens\": [" + itens + "]}")).andExpect(status().isOk());

		PdfReader leitor = new PdfReader(baixarPdf(ordem, tokenAdmin));
		assertThat(leitor.getNumberOfPages()).isGreaterThanOrEqualTo(2);
		assertThat(new PdfTextExtractor(leitor).getTextFromPage(2)).contains("Descrição");
	}

	@Test
	void tecnicoNaoBaixaPdfDeOrdemDeOutraEquipe() throws Exception {
		long ordem = criarOrdem(clienteId, "Outra equipe", adminId);
		atribuir(ordem, carlosId, adminId);

		mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem + "/pdf"), token("bruno@empresa.com")))
				.andExpect(status().isNotFound());
	}

	private long criarOrdemConcluida() {
		long ordem = criarOrdem(clienteId, "Reparo elétrico", adminId);
		atribuir(ordem, carlosId, adminId);
		marcarConcluida(ordem, carlosId);
		return ordem;
	}

	private byte[] baixarPdf(long ordem, String token) throws Exception {
		return mvc.perform(autenticado(get("/api/v1/ordens-servico/" + ordem + "/pdf"), token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andReturn().getResponse().getContentAsByteArray();
	}

	private static String extrairTexto(byte[] pdf) throws IOException {
		PdfReader leitor = new PdfReader(pdf);
		PdfTextExtractor extrator = new PdfTextExtractor(leitor);
		StringBuilder texto = new StringBuilder();
		for (int pagina = 1; pagina <= leitor.getNumberOfPages(); pagina++) {
			texto.append(extrator.getTextFromPage(pagina)).append('\n');
		}
		return texto.toString();
	}

}
