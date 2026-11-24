package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroAtendimentoRequest(
		@NotBlank(message = "Informe a descrição do atendimento.")
		@Size(max = 10000, message = "A descrição do atendimento deve ter no máximo {max} caracteres.")
		String descricao,

		@Size(max = 10000, message = "Os materiais utilizados devem ter no máximo {max} caracteres.")
		String materiaisUtilizados) {

	public RegistroAtendimentoRequest {
		descricao = Textos.obrigatorio(descricao);
		materiaisUtilizados = Textos.opcional(materiaisUtilizados);
	}

}
