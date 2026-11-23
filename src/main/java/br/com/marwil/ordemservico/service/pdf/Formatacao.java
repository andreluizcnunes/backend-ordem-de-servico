package br.com.marwil.ordemservico.service.pdf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class Formatacao {

	private static final Locale BRASIL = Locale.of("pt", "BR");
	private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private Formatacao() {
	}

	static String moeda(BigDecimal valor) {
		NumberFormat formato = NumberFormat.getCurrencyInstance(BRASIL);
		return formato.format(valor).replace(' ', ' ');
	}

	static String quantidade(BigDecimal valor) {
		DecimalFormat formato = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(BRASIL));
		return formato.format(valor);
	}

	static String dataHora(OffsetDateTime momento, ZoneId fuso) {
		return momento == null ? "" : DATA_HORA.format(momento.atZoneSameInstant(fuso));
	}

	static String texto(String valor) {
		return valor == null ? "" : valor;
	}

}
