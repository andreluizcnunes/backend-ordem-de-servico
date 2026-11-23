package br.com.marwil.ordemservico.auth;

import java.time.OffsetDateTime;

public record Usuario(
		long id,
		String nome,
		String email,
		Perfil perfil,
		boolean ativo,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm) {
}
