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
		@NotNull TipoItem tipo,
		@NotBlank @Size(max = 500) String descricao,
		@Size(max = 300) String localRealizacao,
		@NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
		@NotNull(message = "informe o preço unitário; use 0 somente quando o item não for cobrado")
		@DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal precoUnitario) {

	public ItemOrdemServicoRequest {
		descricao = Textos.obrigatorio(descricao);
		localRealizacao = Textos.opcional(localRealizacao);
	}

}
