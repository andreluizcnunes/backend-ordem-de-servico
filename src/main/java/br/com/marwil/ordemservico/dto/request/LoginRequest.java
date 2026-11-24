package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "Informe o e-mail.")
		String email,

		@NotBlank(message = "Informe a senha.")
		String senha) {

	public LoginRequest {
		email = Textos.obrigatorio(email);
	}

}
