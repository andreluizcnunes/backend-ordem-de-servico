package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SituacaoAtendimentoRequest(@NotNull StatusAtendimento status, @Size(max = 5000) String motivo) {

	public SituacaoAtendimentoRequest {
		motivo = Textos.opcional(motivo);
	}

}
