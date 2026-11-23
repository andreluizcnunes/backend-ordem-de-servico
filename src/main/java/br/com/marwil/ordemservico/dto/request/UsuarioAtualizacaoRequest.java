package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoRequest(
		@NotBlank @Size(max = 150) String nome,
		@NotBlank @Email @Size(max = 254) String email,
		@NotNull Perfil perfil,
		@NotNull Boolean ativo) {

	public UsuarioAtualizacaoRequest {
		nome = Textos.obrigatorio(nome);
		email = Textos.obrigatorio(email);
	}

}
