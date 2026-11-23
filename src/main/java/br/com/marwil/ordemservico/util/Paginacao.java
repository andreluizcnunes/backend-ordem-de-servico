package br.com.marwil.ordemservico.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paginacao {

	public static final int TAMANHO_PADRAO = 20;
	public static final int TAMANHO_MAXIMO = 100;

	private Paginacao() {
	}

	public static Pageable de(Integer pagina, Integer tamanho, Sort ordenacao) {
		int paginaNormalizada = pagina == null ? 0 : Math.max(pagina, 0);
		int tamanhoNormalizado = tamanho == null ? TAMANHO_PADRAO : Math.clamp(tamanho, 1, TAMANHO_MAXIMO);
		return PageRequest.of(paginaNormalizada, tamanhoNormalizado, ordenacao);
	}

}
