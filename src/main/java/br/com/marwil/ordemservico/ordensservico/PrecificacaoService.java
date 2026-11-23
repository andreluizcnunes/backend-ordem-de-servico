package br.com.marwil.ordemservico.ordensservico;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.comum.ConflitoException;
import br.com.marwil.ordemservico.comum.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.comum.Textos;
import br.com.marwil.ordemservico.ordensservico.ItemOrdemServicoRepository.DadosItem;

@Service
public class PrecificacaoService {

	private final OrdemServicoRepository ordens;
	private final ItemOrdemServicoRepository itens;

	public PrecificacaoService(OrdemServicoRepository ordens, ItemOrdemServicoRepository itens) {
		this.ordens = ordens;
		this.itens = itens;
	}

	@Transactional(readOnly = true)
	public Precificacao consultar(long ordemServicoId) {
		EstadoOrdemServico estado = ordens.buscarEstado(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		return Precificacao.de(estado, itens.listar(ordemServicoId));
	}

	@Transactional
	public Precificacao definirItens(UsuarioAutenticado responsavel, long ordemServicoId,
			List<DadosOrdemServico.Item> itensInformados) {
		EstadoOrdemServico estado = ordens.bloquearParaAlteracao(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (estado.statusAtendimento() != StatusAtendimento.CONCLUIDA) {
			throw new ConflitoException("A precificação só é feita após a conclusão da ordem de serviço.");
		}
		if (estado.cobrancaEnviada()) {
			throw new ConflitoException("A ordem de serviço já foi enviada à cobrança e os itens estão fechados.");
		}
		Set<Long> existentes = itens.idsDaOrdem(ordemServicoId);
		Set<Long> mantidos = idsMantidos(itensInformados, existentes);
		Set<Long> removidos = new HashSet<>(existentes);
		removidos.removeAll(mantidos);
		itens.remover(ordemServicoId, removidos);
		for (DadosOrdemServico.Item item : itensInformados) {
			DadosItem dados = normalizar(item);
			if (item.id() == null) {
				itens.inserir(ordemServicoId, dados, responsavel.id());
			} else {
				itens.atualizar(ordemServicoId, item.id(), dados);
			}
		}
		return Precificacao.de(estado, itens.listar(ordemServicoId));
	}

	private static Set<Long> idsMantidos(List<DadosOrdemServico.Item> itensInformados, Set<Long> existentes) {
		List<Long> ids = itensInformados.stream().map(DadosOrdemServico.Item::id).filter(Objects::nonNull).toList();
		Set<Long> mantidos = new HashSet<>(ids);
		if (mantidos.size() != ids.size()) {
			throw new RequisicaoInvalidaException("O mesmo item foi informado mais de uma vez.");
		}
		if (!existentes.containsAll(mantidos)) {
			throw new RequisicaoInvalidaException("Há itens que não pertencem a esta ordem de serviço.");
		}
		return mantidos;
	}

	private static DadosItem normalizar(DadosOrdemServico.Item item) {
		return new DadosItem(
				item.tipo(),
				Textos.obrigatorio(item.descricao()),
				Textos.opcional(item.localRealizacao()),
				item.quantidade(),
				item.precoUnitario());
	}

}
