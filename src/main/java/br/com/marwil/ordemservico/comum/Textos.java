package br.com.marwil.ordemservico.comum;

public final class Textos {

	private Textos() {
	}

	public static String opcional(String valor) {
		if (valor == null) {
			return null;
		}
		String limpo = valor.strip();
		return limpo.isEmpty() ? null : limpo;
	}

	public static String obrigatorio(String valor) {
		return valor.strip();
	}

	public static String primeiroPreenchido(String preferencial, String alternativo) {
		String valor = opcional(preferencial);
		return valor != null ? valor : opcional(alternativo);
	}

}
