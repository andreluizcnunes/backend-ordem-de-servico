package br.com.marwil.ordemservico.ordensservico;

import java.time.OffsetDateTime;
import java.util.List;

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
			@Email @Size(max = 254) String emailAtendimento,
			List<@NotNull Long> tecnicosIds) {

		public List<Long> tecnicosOuVazio() {
			return tecnicosIds == null ? List.of() : tecnicosIds;
		}

	}

	public record Equipe(@NotNull List<@NotNull Long> tecnicosIds) {
	}

	public record Situacao(@NotNull StatusAtendimento status, @Size(max = 5000) String motivo) {
	}

	public record Registro(
			@NotBlank @Size(max = 10000) String descricao,
			@Size(max = 10000) String materiaisUtilizados) {
	}

	public record Atualizacao(
			@NotBlank @Size(max = 10000) String descricao,
			@NotBlank @Size(max = 300) String enderecoAtendimento,
			@Size(max = 150) String contatoAtendimento,
			@Size(max = 30) String telefoneAtendimento,
			@Email @Size(max = 254) String emailAtendimento) {
	}

}
