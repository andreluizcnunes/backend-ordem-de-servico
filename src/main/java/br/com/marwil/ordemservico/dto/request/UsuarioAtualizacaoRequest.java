package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoRequest(
		@NotBlank(message = "Informe o nome do usuário.")
		@Size(max = 150, message = "O nome do usuário deve ter no máximo {max} caracteres.")
		String nome,

		@NotBlank(message = "Informe o e-mail.")
		@Email(message = "Informe um e-mail válido.")
		@Size(max = 254, message = "O e-mail deve ter no máximo {max} caracteres.")
		String email,

		@NotNull(message = "Informe o perfil: DONO, ADMINISTRATIVO ou TECNICO.")
		Perfil perfil,

		@NotNull(message = "Informe se o usuário está ativo.")
		Boolean ativo) {

	public UsuarioAtualizacaoRequest {
		nome = Textos.obrigatorio(nome);
		email = Textos.obrigatorio(email);
	}

}
