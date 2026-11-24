package br.com.marwil.ordemservico.dto.request;

import java.math.BigDecimal;

import br.com.marwil.ordemservico.enums.TipoItem;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemOrdemServicoRequest(
		Long id,

		@NotNull(message = "Informe o tipo do item: PRODUTO ou SERVICO.")
		TipoItem tipo,

		@NotBlank(message = "Informe a descrição do item.")
		@Size(max = 500, message = "A descrição do item deve ter no máximo {max} caracteres.")
		String descricao,

		@Size(max = 300, message = "O local de realização deve ter no máximo {max} caracteres.")
		String localRealizacao,

		@NotNull(message = "Informe a quantidade.")
		@DecimalMin(value = "0", inclusive = false, message = "A quantidade deve ser maior que zero.")
		@Digits(integer = 9, fraction = 3,
				message = "A quantidade deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais.")
		BigDecimal quantidade,

		@NotNull(message = "Informe o preço unitário; use 0 somente quando o item não for cobrado.")
		@DecimalMin(value = "0", message = "O preço unitário não pode ser negativo.")
		@Digits(integer = 10, fraction = 2,
				message = "O preço unitário deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais.")
		BigDecimal precoUnitario) {

	public ItemOrdemServicoRequest {
		descricao = Textos.obrigatorio(descricao);
		localRealizacao = Textos.opcional(localRealizacao);
	}

}
