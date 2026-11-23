package br.com.marwil.ordemservico.repository.specification;

import java.util.Locale;

final class Busca {

	static final char ESCAPE = '\\';

	private Busca() {
	}

	static String contem(String termo) {
		String escapado = termo.toLowerCase(Locale.ROOT)
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
		return "%" + escapado + "%";
	}

}
