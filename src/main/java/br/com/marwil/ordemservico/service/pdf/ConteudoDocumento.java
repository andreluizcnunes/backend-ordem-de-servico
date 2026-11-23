package br.com.marwil.ordemservico.service.pdf;

import java.math.BigDecimal;
import java.util.List;

import br.com.marwil.ordemservico.dto.response.ItemOrdemServicoResponse;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResponse;
import br.com.marwil.ordemservico.enums.TipoItem;

public record ConteudoDocumento(
		String empresa,
		OrdemServicoResponse ordem,
		List<ItemOrdemServicoResponse> itens,
		boolean exibirValores,
		BigDecimal total) {

	public List<ItemOrdemServicoResponse> produtos() {
		return itens.stream().filter(item -> item.tipo() == TipoItem.PRODUTO).toList();
	}

	public List<ItemOrdemServicoResponse> servicos() {
		return itens.stream().filter(item -> item.tipo() != TipoItem.PRODUTO).toList();
	}

}
