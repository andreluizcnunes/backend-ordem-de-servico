package br.com.marwil.ordemservico.dto.response;

import java.math.BigDecimal;
import java.util.List;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;

public record PrecificacaoResponse(
		Long ordemServicoId,
		StatusAtendimento statusAtendimento,
		StatusCobranca statusCobranca,
		boolean editavel,
		List<ItemOrdemServicoResponse> itens,
		BigDecimal totalProdutos,
		BigDecimal totalServicos,
		BigDecimal totalMaoDeObra,
		BigDecimal total) {
}
