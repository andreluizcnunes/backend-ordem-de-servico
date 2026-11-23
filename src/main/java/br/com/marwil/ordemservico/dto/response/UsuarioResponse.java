package br.com.marwil.ordemservico.dto.response;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.enums.Perfil;

public record UsuarioResponse(
		Long id,
		String nome,
		String email,
		Perfil perfil,
		boolean ativo,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm) {
}
