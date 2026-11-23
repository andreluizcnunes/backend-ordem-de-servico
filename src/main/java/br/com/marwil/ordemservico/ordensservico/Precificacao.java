package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.TipoItem;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.math.BigDecimal;
import java.util.List;

import br.com.marwil.ordemservico.enums.StatusCobranca;

public record Precificacao(
		long ordemServicoId,
		StatusAtendimento statusAtendimento,
		StatusCobranca statusCobranca,
		boolean editavel,
		List<ItemOrdemServico> itens,
		BigDecimal totalProdutos,
		BigDecimal totalServicos,
		BigDecimal totalMaoDeObra,
		BigDecimal total) {

	public static Precificacao de(EstadoOrdemServico estado, List<ItemOrdemServico> itens) {
		BigDecimal produtos = somar(itens, TipoItem.PRODUTO);
		BigDecimal servicos = somar(itens, TipoItem.SERVICO);
		BigDecimal maoDeObra = somar(itens, TipoItem.MAO_DE_OBRA);
		boolean editavel = estado.statusAtendimento() == StatusAtendimento.CONCLUIDA && !estado.cobrancaEnviada();
		return new Precificacao(estado.id(), estado.statusAtendimento(), estado.statusCobranca(), editavel, itens,
				produtos, servicos, maoDeObra, produtos.add(servicos).add(maoDeObra));
	}

	private static BigDecimal somar(List<ItemOrdemServico> itens, TipoItem tipo) {
		return itens.stream()
				.filter(item -> item.tipo() == tipo)
				.map(ItemOrdemServico::valorTotal)
				.reduce(new BigDecimal("0.00"), BigDecimal::add);
	}

}
