package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
		@NotBlank @Size(max = 200) String nome,
		@Size(max = 150) String contato,
		@Size(max = 30) String telefone,
		@Email @Size(max = 254) String email,
		@Size(max = 300) String endereco) {

	public ClienteRequest {
		nome = Textos.obrigatorio(nome);
		contato = Textos.opcional(contato);
		telefone = Textos.opcional(telefone);
		email = Textos.opcional(email);
		endereco = Textos.opcional(endereco);
	}

}
