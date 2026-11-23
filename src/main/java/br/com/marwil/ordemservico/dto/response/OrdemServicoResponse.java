package br.com.marwil.ordemservico.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;

public record OrdemServicoResponse(
		Long id,
		ClienteResponse cliente,
		OffsetDateTime dataAbertura,
		String descricao,
		String enderecoAtendimento,
		String contatoAtendimento,
		String telefoneAtendimento,
		String emailAtendimento,
		StatusAtendimento statusAtendimento,
		String motivoAguardando,
		OffsetDateTime dataInicioAtendimento,
		OffsetDateTime dataConclusao,
		ReferenciaResponse concluidaPor,
		@JsonInclude(JsonInclude.Include.NON_NULL) StatusCobranca statusCobranca,
		ReferenciaResponse criadoPor,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm,
		List<TecnicoAtribuidoResponse> tecnicos,
		List<RegistroAtendimentoResponse> registros) {
}
