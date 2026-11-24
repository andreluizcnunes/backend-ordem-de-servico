package br.com.marwil.ordemservico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(
		@NotBlank(message = "Informe a senha atual.")
		String senhaAtual,

		@NotBlank(message = "Informe a nova senha.")
		@Size(min = 8, max = 72, message = "A nova senha deve ter entre {min} e {max} caracteres.")
		String novaSenha) {
}
