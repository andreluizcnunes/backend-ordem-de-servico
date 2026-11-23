package br.com.marwil.ordemservico.ordensservico;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public final class DadosOrdemServico {

	private DadosOrdemServico() {
	}

	public record Abertura(
			@NotNull Long clienteId,
			@PastOrPresent OffsetDateTime dataAbertura,
			@NotBlank @Size(max = 10000) String descricao,
			@Size(max = 300) String enderecoAtendimento,
			@Size(max = 150) String contatoAtendimento,
			@Size(max = 30) String telefoneAtendimento,
			@Email @Size(max = 254) String emailAtendimento) {
	}

	public record Atualizacao(
			@NotBlank @Size(max = 10000) String descricao,
			@NotBlank @Size(max = 300) String enderecoAtendimento,
			@Size(max = 150) String contatoAtendimento,
			@Size(max = 30) String telefoneAtendimento,
			@Email @Size(max = 254) String emailAtendimento) {
	}

}
