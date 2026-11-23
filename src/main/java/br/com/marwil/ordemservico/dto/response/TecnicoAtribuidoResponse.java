package br.com.marwil.ordemservico.dto.response;

import java.time.OffsetDateTime;

public record TecnicoAtribuidoResponse(Long id, String nome, OffsetDateTime atribuidoEm,
		ReferenciaResponse atribuidoPor) {
}
