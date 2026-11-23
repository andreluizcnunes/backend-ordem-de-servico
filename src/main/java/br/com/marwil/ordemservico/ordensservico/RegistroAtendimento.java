package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoRegistro;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record RegistroAtendimento(
		long id,
		TipoRegistro tipo,
		String descricao,
		String materiaisUtilizados,
		UsuarioResumido autor,
		OffsetDateTime registradoEm) {
}
