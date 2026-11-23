package br.com.marwil.ordemservico.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long total, int totalPaginas) {

	public static <T> PaginaResponse<T> de(Page<T> pagina) {
		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
				pagina.getTotalElements(), pagina.getTotalPages());
	}

}
