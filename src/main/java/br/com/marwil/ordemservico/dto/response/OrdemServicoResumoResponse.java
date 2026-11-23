package br.com.marwil.ordemservico.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;

public record OrdemServicoResumoResponse(
		Long id,
		ReferenciaResponse cliente,
		OffsetDateTime dataAbertura,
		String descricao,
		String enderecoAtendimento,
		StatusAtendimento statusAtendimento,
		OffsetDateTime dataConclusao,
		@JsonInclude(JsonInclude.Include.NON_NULL) StatusCobranca statusCobranca,
		List<ReferenciaResponse> tecnicos) {
}
