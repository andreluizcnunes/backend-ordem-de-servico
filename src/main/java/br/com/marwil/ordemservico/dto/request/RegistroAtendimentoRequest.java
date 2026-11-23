package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroAtendimentoRequest(
		@NotBlank @Size(max = 10000) String descricao,
		@Size(max = 10000) String materiaisUtilizados) {

	public RegistroAtendimentoRequest {
		descricao = Textos.obrigatorio(descricao);
		materiaisUtilizados = Textos.opcional(materiaisUtilizados);
	}

}
