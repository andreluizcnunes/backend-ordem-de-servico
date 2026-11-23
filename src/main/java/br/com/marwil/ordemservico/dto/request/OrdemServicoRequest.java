package br.com.marwil.ordemservico.dto.request;

import java.time.OffsetDateTime;
import java.util.List;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record OrdemServicoRequest(
		@NotNull Long clienteId,
		@PastOrPresent OffsetDateTime dataAbertura,
		@NotBlank @Size(max = 10000) String descricao,
		@Size(max = 300) String enderecoAtendimento,
		@Size(max = 150) String contatoAtendimento,
		@Size(max = 30) String telefoneAtendimento,
		@Email @Size(max = 254) String emailAtendimento,
		List<@NotNull Long> tecnicosIds) {

	public OrdemServicoRequest {
		descricao = Textos.obrigatorio(descricao);
		enderecoAtendimento = Textos.opcional(enderecoAtendimento);
		contatoAtendimento = Textos.opcional(contatoAtendimento);
		telefoneAtendimento = Textos.opcional(telefoneAtendimento);
		emailAtendimento = Textos.opcional(emailAtendimento);
		tecnicosIds = tecnicosIds == null ? List.of() : tecnicosIds;
	}

}
