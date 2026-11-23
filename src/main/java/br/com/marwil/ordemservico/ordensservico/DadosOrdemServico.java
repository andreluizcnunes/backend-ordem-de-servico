package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoItem;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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

	public record Itens(@NotNull List<@NotNull @Valid Item> itens) {
	}

	public record Item(
			Long id,
			@NotNull TipoItem tipo,
			@NotBlank @Size(max = 500) String descricao,
			@Size(max = 300) String localRealizacao,
			@NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
			@NotNull(message = "informe o preço unitário; use 0 somente quando o item não for cobrado")
			@DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal precoUnitario) {
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
