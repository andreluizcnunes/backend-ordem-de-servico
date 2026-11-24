package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SituacaoAtendimentoRequest(
		@NotNull(message = "Informe a nova situação: ABERTA, EM_ATENDIMENTO, AGUARDANDO ou CONCLUIDA.")
		StatusAtendimento status,

		@Size(max = 5000, message = "O motivo deve ter no máximo {max} caracteres.")
		String motivo) {

	public SituacaoAtendimentoRequest {
		motivo = Textos.opcional(motivo);
	}

}
