package br.com.marwil.ordemservico.clientes;

import java.time.OffsetDateTime;

public record Cliente(
		long id,
		String nome,
		String contato,
		String telefone,
		String email,
		String endereco,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm) {
}
