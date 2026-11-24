package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
		@NotBlank(message = "Informe o nome do usuário.")
		@Size(max = 150, message = "O nome do usuário deve ter no máximo {max} caracteres.")
		String nome,

		@NotBlank(message = "Informe o e-mail.")
		@Email(message = "Informe um e-mail válido.")
		@Size(max = 254, message = "O e-mail deve ter no máximo {max} caracteres.")
		String email,

		@NotBlank(message = "Informe a senha.")
		@Size(min = 8, max = 72, message = "A senha deve ter entre {min} e {max} caracteres.")
		String senha,

		@NotNull(message = "Informe o perfil: DONO, ADMINISTRATIVO ou TECNICO.")
		Perfil perfil) {

	public UsuarioRequest {
		nome = Textos.obrigatorio(nome);
		email = Textos.obrigatorio(email);
	}

}
