package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
		@NotBlank(message = "Informe o nome do cliente.")
		@Size(max = 200, message = "O nome do cliente deve ter no máximo {max} caracteres.")
		String nome,

		@Size(max = 150, message = "O contato deve ter no máximo {max} caracteres.")
		String contato,

		@Size(max = 30, message = "O telefone deve ter no máximo {max} caracteres.")
		String telefone,

		@Email(message = "Informe um e-mail válido.")
		@Size(max = 254, message = "O e-mail deve ter no máximo {max} caracteres.")
		String email,

		@Size(max = 300, message = "O endereço deve ter no máximo {max} caracteres.")
		String endereco) {

	public ClienteRequest {
		nome = Textos.obrigatorio(nome);
		contato = Textos.opcional(contato);
		telefone = Textos.opcional(telefone);
		email = Textos.opcional(email);
		endereco = Textos.opcional(endereco);
	}

}
