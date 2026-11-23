package br.com.marwil.ordemservico.clientes;

import br.com.marwil.ordemservico.comum.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCliente(
		@NotBlank @Size(max = 200) String nome,
		@Size(max = 150) String contato,
		@Size(max = 30) String telefone,
		@Email @Size(max = 254) String email,
		@Size(max = 300) String endereco) {

	public DadosCliente normalizado() {
		return new DadosCliente(
				Textos.obrigatorio(nome),
				Textos.opcional(contato),
				Textos.opcional(telefone),
				Textos.opcional(email),
				Textos.opcional(endereco));
	}

}
