package br.com.marwil.ordemservico.documentos;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import br.com.marwil.ordemservico.ordensservico.ItemOrdemServico;
import br.com.marwil.ordemservico.ordensservico.OrdemServicoDetalhe;
import br.com.marwil.ordemservico.ordensservico.RegistroAtendimento;
import br.com.marwil.ordemservico.ordensservico.TecnicoAtribuido;
import br.com.marwil.ordemservico.ordensservico.TipoItem;
import br.com.marwil.ordemservico.ordensservico.TipoRegistro;

@Component
public class GeradorPdfOrdemServico {

	private static final int LINHAS_EM_BRANCO = 6;
	private static final Color CINZA = new Color(225, 225, 225);
	private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
	private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
	private static final Font ROTULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
	private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
	private static final Font PEQUENA = FontFactory.getFont(FontFactory.HELVETICA, 7);

	private final ZoneId fuso;

	public GeradorPdfOrdemServico(Clock relogio) {
		this.fuso = relogio.getZone();
	}

	public byte[] gerar(ConteudoDocumento conteudo) {
		ByteArrayOutputStream saida = new ByteArrayOutputStream();
		Document documento = new Document(PageSize.A4, 36, 36, 36, 48);
		PdfWriter escritor = PdfWriter.getInstance(documento, saida);
		escritor.setPageEvent(new RodapeNumerado(conteudo.ordem().id()));
		documento.addTitle("Ordem de Serviço " + conteudo.ordem().id());
		documento.open();
		documento.add(cabecalho(conteudo));
		documento.add(dadosCliente(conteudo.ordem()));
		documento.add(secaoTexto("Manutenção / serviço solicitado", conteudo.ordem().descricao()));
		documento.add(tabelaProdutos(conteudo));
		documento.add(tabelaServicos(conteudo));
		documento.add(tecnicosETotal(conteudo));
		if (!conteudo.ordem().registros().isEmpty()) {
			documento.add(registros(conteudo.ordem().registros()));
		}
		documento.add(recibos());
		documento.close();
		return saida.toByteArray();
	}

	private PdfPTable cabecalho(ConteudoDocumento conteudo) {
		PdfPTable tabela = tabela(new float[] { 3, 2 });
		PdfPCell empresa = semBorda(new Phrase(conteudo.empresa(), TITULO));
		empresa.setVerticalAlignment(Element.ALIGN_MIDDLE);
		tabela.addCell(empresa);
		Paragraph identificacao = new Paragraph();
		identificacao.add(new Phrase("ORDEM DE SERVIÇO Nº " + conteudo.ordem().id() + "\n", SUBTITULO));
		identificacao.add(new Phrase("Abertura: " + Formatacao.dataHora(conteudo.ordem().dataAbertura(), fuso)
				+ "\nSituação: " + rotuloSituacao(conteudo.ordem()), NORMAL));
		PdfPCell celula = semBorda(identificacao);
		celula.setHorizontalAlignment(Element.ALIGN_RIGHT);
		tabela.addCell(celula);
		tabela.setSpacingAfter(10);
		return tabela;
	}

	private PdfPTable dadosCliente(OrdemServicoDetalhe ordem) {
		PdfPTable tabela = tabela(new float[] { 1, 3, 1, 2 });
		titulo(tabela, "Cliente");
		campo(tabela, "Cliente", ordem.cliente().nome(), 3);
		campo(tabela, "Endereço", ordem.enderecoAtendimento(), 3);
		campo(tabela, "Contato", ordem.contatoAtendimento(), 1);
		campo(tabela, "Telefone", ordem.telefoneAtendimento(), 1);
		campo(tabela, "E-mail", ordem.emailAtendimento(), 3);
		tabela.setSpacingAfter(8);
		return tabela;
	}

	private PdfPTable secaoTexto(String rotulo, String texto) {
		PdfPTable tabela = tabela(new float[] { 1 });
		titulo(tabela, rotulo);
		PdfPCell celula = new PdfPCell(new Phrase(Formatacao.texto(texto), NORMAL));
		celula.setPadding(5);
		celula.setMinimumHeight(36);
		tabela.addCell(celula);
		tabela.setSpacingAfter(8);
		return tabela;
	}

	private PdfPTable tabelaProdutos(ConteudoDocumento conteudo) {
		boolean valores = conteudo.exibirValores();
		PdfPTable tabela = tabela(valores ? new float[] { 1, 6, 2, 2 } : new float[] { 1, 8 });
		titulo(tabela, "Produtos / materiais");
		cabecalhoColunas(tabela, valores ? List.of("Qtd.", "Descrição", "Valor unit.", "Valor")
				: List.of("Qtd.", "Descrição"));
		tabela.setHeaderRows(2);
		List<ItemOrdemServico> produtos = conteudo.produtos();
		for (ItemOrdemServico item : produtos) {
			tabela.addCell(celula(Formatacao.quantidade(item.quantidade()), Element.ALIGN_CENTER));
			tabela.addCell(celula(item.descricao(), Element.ALIGN_LEFT));
			if (valores) {
				tabela.addCell(celula(Formatacao.moeda(item.precoUnitario()), Element.ALIGN_RIGHT));
				tabela.addCell(celula(Formatacao.moeda(item.valorTotal()), Element.ALIGN_RIGHT));
			}
		}
		linhasEmBranco(tabela, produtos.isEmpty() ? LINHAS_EM_BRANCO : 0);
		tabela.setSpacingAfter(8);
		return tabela;
	}

	private PdfPTable tabelaServicos(ConteudoDocumento conteudo) {
		boolean valores = conteudo.exibirValores();
		PdfPTable tabela = tabela(valores ? new float[] { 1, 5, 3, 2 } : new float[] { 1, 5, 3 });
		titulo(tabela, "Serviços e mão de obra");
		cabecalhoColunas(tabela, valores ? List.of("Qtd.", "Descrição", "Região / local", "Valor")
				: List.of("Qtd.", "Descrição", "Região / local"));
		tabela.setHeaderRows(2);
		List<ItemOrdemServico> servicos = conteudo.servicos();
		for (ItemOrdemServico item : servicos) {
			String descricao = item.tipo() == TipoItem.MAO_DE_OBRA ? "Mão de obra: " + item.descricao()
					: item.descricao();
			tabela.addCell(celula(Formatacao.quantidade(item.quantidade()), Element.ALIGN_CENTER));
			tabela.addCell(celula(descricao, Element.ALIGN_LEFT));
			tabela.addCell(celula(Formatacao.texto(item.localRealizacao()), Element.ALIGN_LEFT));
			if (valores) {
				tabela.addCell(celula(Formatacao.moeda(item.valorTotal()), Element.ALIGN_RIGHT));
			}
		}
		linhasEmBranco(tabela, servicos.isEmpty() ? LINHAS_EM_BRANCO : 0);
		tabela.setSpacingAfter(8);
		return tabela;
	}

	private PdfPTable tecnicosETotal(ConteudoDocumento conteudo) {
		boolean exibirTotal = conteudo.exibirValores() && conteudo.total() != null;
		PdfPTable tabela = tabela(exibirTotal ? new float[] { 3, 1 } : new float[] { 1 });
		String tecnicos = conteudo.ordem().tecnicos().stream()
				.map(TecnicoAtribuido::nome)
				.collect(Collectors.joining(", "));
		PdfPCell celulaTecnicos = new PdfPCell();
		celulaTecnicos.addElement(new Phrase("Técnico(s): ", ROTULO));
		celulaTecnicos.addElement(new Phrase(tecnicos.isEmpty() ? " " : tecnicos, NORMAL));
		celulaTecnicos.setPadding(5);
		tabela.addCell(celulaTecnicos);
		if (exibirTotal) {
			PdfPCell celulaTotal = new PdfPCell();
			celulaTotal.addElement(new Phrase("Valor total: ", ROTULO));
			celulaTotal.addElement(new Phrase(Formatacao.moeda(conteudo.total()), SUBTITULO));
			celulaTotal.setPadding(5);
			tabela.addCell(celulaTotal);
		}
		tabela.setSpacingAfter(8);
		return tabela;
	}

	private PdfPTable registros(List<RegistroAtendimento> registros) {
		PdfPTable tabela = tabela(new float[] { 2, 2, 6 });
		titulo(tabela, "Registros do atendimento");
		cabecalhoColunas(tabela, List.of("Data", "Autor", "Descrição / materiais"));
		tabela.setHeaderRows(2);
		for (RegistroAtendimento registro : registros) {
			String texto = registro.descricao();
			if (registro.materiaisUtilizados() != null) {
				texto += "\nMateriais: " + registro.materiaisUtilizados();
			}
			if (registro.tipo() == TipoRegistro.IMPEDIMENTO) {
				texto = "Impedimento: " + texto;
			}
			tabela.addCell(celula(Formatacao.dataHora(registro.registradoEm(), fuso), Element.ALIGN_LEFT));
			tabela.addCell(celula(registro.autor().nome(), Element.ALIGN_LEFT));
			tabela.addCell(celula(texto, Element.ALIGN_LEFT));
		}
		tabela.setSpacingAfter(8);
		return tabela;
	}

	private PdfPTable recibos() {
		PdfPTable tabela = tabela(new float[] { 1, 1 });
		tabela.setKeepTogether(true);
		tabela.addCell(recibo("Recibo de material"));
		tabela.addCell(recibo("Recibo de serviço"));
		return tabela;
	}

	private PdfPCell recibo(String titulo) {
		PdfPCell celula = new PdfPCell();
		celula.setPadding(6);
		celula.addElement(new Paragraph(titulo, ROTULO));
		celula.addElement(new Paragraph("\nRecebido por: _______________________________", NORMAL));
		celula.addElement(new Paragraph("\nData: ____/____/________", NORMAL));
		celula.addElement(new Paragraph("\nAssinatura: _________________________________", NORMAL));
		return celula;
	}

	private static String rotuloSituacao(OrdemServicoDetalhe ordem) {
		return switch (ordem.statusAtendimento()) {
			case ABERTA -> "Aberta";
			case EM_ATENDIMENTO -> "Em atendimento";
			case AGUARDANDO -> "Aguardando";
			case CONCLUIDA -> "Concluída";
		};
	}

	private static PdfPTable tabela(float[] larguras) {
		PdfPTable tabela = new PdfPTable(larguras);
		tabela.setWidthPercentage(100);
		return tabela;
	}

	private static void titulo(PdfPTable tabela, String texto) {
		PdfPCell celula = new PdfPCell(new Phrase(texto, SUBTITULO));
		celula.setColspan(tabela.getNumberOfColumns());
		celula.setBackgroundColor(CINZA);
		celula.setPadding(4);
		tabela.addCell(celula);
	}

	private static void cabecalhoColunas(PdfPTable tabela, List<String> colunas) {
		for (String coluna : colunas) {
			PdfPCell celula = new PdfPCell(new Phrase(coluna, ROTULO));
			celula.setHorizontalAlignment(Element.ALIGN_CENTER);
			celula.setPadding(3);
			tabela.addCell(celula);
		}
	}

	private static void campo(PdfPTable tabela, String rotulo, String valor, int colunas) {
		PdfPCell celulaRotulo = new PdfPCell(new Phrase(rotulo + ":", ROTULO));
		celulaRotulo.setPadding(4);
		celulaRotulo.setBorder(Rectangle.BOX);
		tabela.addCell(celulaRotulo);
		PdfPCell celulaValor = new PdfPCell(new Phrase(Formatacao.texto(valor), NORMAL));
		celulaValor.setColspan(colunas);
		celulaValor.setPadding(4);
		tabela.addCell(celulaValor);
	}

	private static PdfPCell celula(String texto, int alinhamento) {
		PdfPCell celula = new PdfPCell(new Phrase(texto, NORMAL));
		celula.setHorizontalAlignment(alinhamento);
		celula.setPadding(3);
		return celula;
	}

	private static void linhasEmBranco(PdfPTable tabela, int quantidade) {
		for (int linha = 0; linha < quantidade * tabela.getNumberOfColumns(); linha++) {
			PdfPCell celula = new PdfPCell(new Phrase(" ", NORMAL));
			celula.setMinimumHeight(16);
			tabela.addCell(celula);
		}
	}

	private static PdfPCell semBorda(Phrase conteudo) {
		PdfPCell celula = new PdfPCell(conteudo);
		celula.setBorder(Rectangle.NO_BORDER);
		return celula;
	}

	private static final class RodapeNumerado extends PdfPageEventHelper {

		private final long ordemId;

		private RodapeNumerado(long ordemId) {
			this.ordemId = ordemId;
		}

		@Override
		public void onEndPage(PdfWriter escritor, Document documento) {
			Phrase rodape = new Phrase("OS nº " + ordemId + " — página " + escritor.getPageNumber(), PEQUENA);
			ColumnText.showTextAligned(escritor.getDirectContent(), Element.ALIGN_RIGHT, rodape,
					documento.right(), documento.bottom() - 20, 0);
		}

	}

}
