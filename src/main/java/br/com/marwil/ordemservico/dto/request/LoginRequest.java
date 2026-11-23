package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String senha) {

	public LoginRequest {
		email = Textos.obrigatorio(email);
	}

}
