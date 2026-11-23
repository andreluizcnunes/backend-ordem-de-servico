package br.com.marwil.ordemservico.ordensservico;

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
