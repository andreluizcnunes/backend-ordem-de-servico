package br.com.marwil.ordemservico.dto.response;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.enums.TipoRegistro;

public record RegistroAtendimentoResponse(
		Long id,
		TipoRegistro tipo,
		String descricao,
		String materiaisUtilizados,
		ReferenciaResponse autor,
		OffsetDateTime registradoEm) {
}
