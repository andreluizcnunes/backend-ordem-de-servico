package br.com.marwil.ordemservico.dto.response;

import java.time.OffsetDateTime;

public record ClienteResponse(
		Long id,
		String nome,
		String contato,
		String telefone,
		String email,
		String endereco,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm) {
}
