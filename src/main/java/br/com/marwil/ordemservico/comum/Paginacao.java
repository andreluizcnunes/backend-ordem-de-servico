package br.com.marwil.ordemservico.comum;

public record Paginacao(int pagina, int tamanho) {

	public static final int TAMANHO_PADRAO = 20;
	public static final int TAMANHO_MAXIMO = 100;

	public static Paginacao de(Integer pagina, Integer tamanho) {
		int paginaNormalizada = pagina == null ? 0 : Math.max(pagina, 0);
		int tamanhoNormalizado = tamanho == null ? TAMANHO_PADRAO : Math.clamp(tamanho, 1, TAMANHO_MAXIMO);
		return new Paginacao(paginaNormalizada, tamanhoNormalizado);
	}

	public long deslocamento() {
		return (long) pagina * tamanho;
	}

}
