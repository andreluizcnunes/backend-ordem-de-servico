package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.enums.TipoItem;

public record ItemOrdemServicoResponse(
		Long id,
		TipoItem tipo,
		String descricao,
		String localRealizacao,
		BigDecimal quantidade,
		BigDecimal precoUnitario,
		BigDecimal valorTotal,
		OffsetDateTime atualizadoEm) {
}
