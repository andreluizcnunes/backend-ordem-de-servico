package br.com.marwil.ordemservico.comum;

import java.util.List;

public record Pagina<T>(List<T> itens, int pagina, int tamanho, long total, int totalPaginas) {

	public static <T> Pagina<T> de(List<T> itens, Paginacao paginacao, long total) {
		int totalPaginas = (int) ((total + paginacao.tamanho() - 1) / paginacao.tamanho());
		return new Pagina<>(itens, paginacao.pagina(), paginacao.tamanho(), total, totalPaginas);
	}

}
