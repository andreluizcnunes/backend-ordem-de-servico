package br.com.marwil.ordemservico.documentos;

import java.math.BigDecimal;
import java.util.List;

import br.com.marwil.ordemservico.ordensservico.ItemOrdemServico;
import br.com.marwil.ordemservico.ordensservico.OrdemServicoDetalhe;
import br.com.marwil.ordemservico.enums.TipoItem;

public record ConteudoDocumento(
		String empresa,
		OrdemServicoDetalhe ordem,
		List<ItemOrdemServico> itens,
		boolean exibirValores,
		BigDecimal total) {

	public List<ItemOrdemServico> produtos() {
		return itens.stream().filter(item -> item.tipo() == TipoItem.PRODUTO).toList();
	}

	public List<ItemOrdemServico> servicos() {
		return itens.stream().filter(item -> item.tipo() != TipoItem.PRODUTO).toList();
	}

}
