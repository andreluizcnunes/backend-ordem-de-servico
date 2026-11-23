package br.com.marwil.ordemservico.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.ItemOrdemServicoRequest;
import br.com.marwil.ordemservico.dto.request.ItensOrdemServicoRequest;
import br.com.marwil.ordemservico.dto.response.ItemOrdemServicoResponse;
import br.com.marwil.ordemservico.dto.response.PrecificacaoResponse;
import br.com.marwil.ordemservico.entity.ItemOrdemServico;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.enums.TipoItem;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.mapper.ItemOrdemServicoMapper;
import br.com.marwil.ordemservico.repository.OrdemServicoRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;

@Service
public class PrecificacaoService {

	private static final BigDecimal ZERO = new BigDecimal("0.00");

	private final OrdemServicoRepository ordens;
	private final UsuarioRepository usuarios;
	private final ItemOrdemServicoMapper mapper;

	public PrecificacaoService(OrdemServicoRepository ordens, UsuarioRepository usuarios,
			ItemOrdemServicoMapper mapper) {
		this.ordens = ordens;
		this.usuarios = usuarios;
		this.mapper = mapper;
	}

	@Transactional(readOnly = true)
	public PrecificacaoResponse consultar(Long ordemServicoId) {
		OrdemServico ordem = ordens.findById(ordemServicoId).orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		return resumir(ordem);
	}

	@Transactional
	public PrecificacaoResponse definirItens(UsuarioAutenticado responsavel, Long ordemServicoId,
			ItensOrdemServicoRequest requisicao) {
		OrdemServico ordem = ordens.findComBloqueioById(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (ordem.getStatusAtendimento() != StatusAtendimento.CONCLUIDA) {
			throw new ConflitoException("A precificação só é feita após a conclusão da ordem de serviço.");
		}
		if (ordens.findStatusCobranca(ordemServicoId).isPresent()) {
			throw new ConflitoException("A ordem de serviço já foi enviada à cobrança e os itens estão fechados.");
		}
		Map<Long, ItemOrdemServico> existentes = ordem.getItens().stream()
				.collect(Collectors.toMap(ItemOrdemServico::getId, Function.identity()));
		Set<Long> mantidos = idsMantidos(requisicao.itens(), existentes.keySet());
		ordem.getItens().removeIf(item -> !mantidos.contains(item.getId()));
		Usuario autor = usuarios.getReferenceById(responsavel.id());
		for (ItemOrdemServicoRequest item : requisicao.itens()) {
			if (item.id() == null) {
				ItemOrdemServico novo = mapper.paraEntidade(item);
				novo.setOrdemServico(ordem);
				novo.setCriadoPor(autor);
				ordem.getItens().add(novo);
			} else {
				mapper.atualizar(item, existentes.get(item.id()));
			}
		}
		ordens.flush();
		return resumir(ordem);
	}

	private PrecificacaoResponse resumir(OrdemServico ordem) {
		StatusCobranca statusCobranca = ordens.findStatusCobranca(ordem.getId()).orElse(StatusCobranca.NAO_ENVIADA);
		List<ItemOrdemServicoResponse> itens = ordem.getItens().stream().map(mapper::paraResponse).toList();
		BigDecimal produtos = somar(itens, TipoItem.PRODUTO);
		BigDecimal servicos = somar(itens, TipoItem.SERVICO);
		BigDecimal maoDeObra = somar(itens, TipoItem.MAO_DE_OBRA);
		boolean editavel = ordem.getStatusAtendimento() == StatusAtendimento.CONCLUIDA
				&& statusCobranca == StatusCobranca.NAO_ENVIADA;
		return new PrecificacaoResponse(ordem.getId(), ordem.getStatusAtendimento(), statusCobranca, editavel, itens,
				produtos, servicos, maoDeObra, produtos.add(servicos).add(maoDeObra));
	}

	private static BigDecimal somar(List<ItemOrdemServicoResponse> itens, TipoItem tipo) {
		return itens.stream()
				.filter(item -> item.tipo() == tipo)
				.map(ItemOrdemServicoResponse::valorTotal)
				.reduce(ZERO, BigDecimal::add);
	}

	private static Set<Long> idsMantidos(List<ItemOrdemServicoRequest> itens, Set<Long> existentes) {
		List<Long> ids = itens.stream().map(ItemOrdemServicoRequest::id).filter(Objects::nonNull).toList();
		Set<Long> mantidos = new HashSet<>(ids);
		if (mantidos.size() != ids.size()) {
			throw new RequisicaoInvalidaException("O mesmo item foi informado mais de uma vez.");
		}
		if (!existentes.containsAll(mantidos)) {
			throw new RequisicaoInvalidaException("Há itens que não pertencem a esta ordem de serviço.");
		}
		return mantidos;
	}

}
