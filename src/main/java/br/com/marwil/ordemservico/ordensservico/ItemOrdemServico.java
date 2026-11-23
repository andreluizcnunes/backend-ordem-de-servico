package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ItemOrdemServico(
		long id,
		TipoItem tipo,
		String descricao,
		String localRealizacao,
		BigDecimal quantidade,
		BigDecimal precoUnitario,
		BigDecimal valorTotal,
		OffsetDateTime atualizadoEm) {
}
