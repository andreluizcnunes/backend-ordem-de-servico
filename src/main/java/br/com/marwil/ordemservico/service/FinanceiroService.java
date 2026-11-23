package br.com.marwil.ordemservico.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.response.LancamentoCaixaResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.dto.response.ReferenciaResponse;
import br.com.marwil.ordemservico.dto.response.ResumoFinanceiroClienteResponse;
import br.com.marwil.ordemservico.dto.response.ResumoFinanceiroResponse;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.mapper.LancamentoCaixaMapper;
import br.com.marwil.ordemservico.repository.CobrancaRepository;
import br.com.marwil.ordemservico.repository.LancamentoCaixaRepository;
import br.com.marwil.ordemservico.repository.projection.PendenciaPorCliente;
import br.com.marwil.ordemservico.repository.projection.RecebimentoPorCliente;
import br.com.marwil.ordemservico.repository.projection.Totalizador;

@Service
public class FinanceiroService {

	private static final LocalDate INICIO_ABERTO = LocalDate.of(1900, 1, 1);
	private static final LocalDate FIM_ABERTO = LocalDate.of(9999, 12, 31);
	private static final BigDecimal ZERO = new BigDecimal("0.00");

	private final LancamentoCaixaRepository caixa;
	private final CobrancaRepository cobrancas;
	private final LancamentoCaixaMapper mapper;
	private final Clock relogio;

	public FinanceiroService(LancamentoCaixaRepository caixa, CobrancaRepository cobrancas,
			LancamentoCaixaMapper mapper, Clock relogio) {
		this.caixa = caixa;
		this.cobrancas = cobrancas;
		this.mapper = mapper;
		this.relogio = relogio;
	}

	@Transactional(readOnly = true)
	public ResumoFinanceiroResponse resumir(LocalDate inicio, LocalDate fim) {
		validarPeriodo(inicio, fim);
		LocalDate de = inicio == null ? INICIO_ABERTO : inicio;
		LocalDate ate = fim == null ? FIM_ABERTO : fim;
		LocalDate hoje = LocalDate.now(relogio);
		Totalizador recebido = caixa.totalizarPeriodo(de, ate);
		Totalizador pendente = cobrancas.totalizarPorStatus(StatusCobranca.PENDENTE);
		Totalizador vencido = cobrancas.totalizarVencidas(hoje);
		return new ResumoFinanceiroResponse(inicio, fim,
				recebido.total(), recebido.quantidade(),
				pendente.total(), pendente.quantidade(),
				vencido.total(), vencido.quantidade(),
				porCliente(caixa.totalizarPorCliente(de, ate), cobrancas.totalizarPendenciasPorCliente(hoje)));
	}

	@Transactional(readOnly = true)
	public PaginaResponse<LancamentoCaixaResponse> listarLancamentos(LocalDate inicio, LocalDate fim,
			Pageable paginacao) {
		validarPeriodo(inicio, fim);
		LocalDate de = inicio == null ? INICIO_ABERTO : inicio;
		LocalDate ate = fim == null ? FIM_ABERTO : fim;
		return PaginaResponse.de(caixa.findByDataLancamentoBetween(de, ate, paginacao).map(mapper::paraResponse));
	}

	private static List<ResumoFinanceiroClienteResponse> porCliente(List<RecebimentoPorCliente> recebimentos,
			List<PendenciaPorCliente> pendencias) {
		Map<Long, ResumoFinanceiroClienteResponse> resumo = new LinkedHashMap<>();
		for (RecebimentoPorCliente recebimento : recebimentos) {
			resumo.put(recebimento.clienteId(), new ResumoFinanceiroClienteResponse(
					new ReferenciaResponse(recebimento.clienteId(), recebimento.nome()),
					recebimento.total(), ZERO, 0, false));
		}
		for (PendenciaPorCliente pendencia : pendencias) {
			ResumoFinanceiroClienteResponse atual = resumo.get(pendencia.clienteId());
			BigDecimal recebido = atual == null ? ZERO : atual.totalRecebido();
			resumo.put(pendencia.clienteId(), new ResumoFinanceiroClienteResponse(
					new ReferenciaResponse(pendencia.clienteId(), pendencia.nome()),
					recebido, pendencia.total(), pendencia.quantidade(), pendencia.possuiVencidas()));
		}
		return resumo.values().stream()
				.sorted(Comparator.comparing(ResumoFinanceiroClienteResponse::totalPendente).reversed()
						.thenComparing(item -> item.cliente().nome().toLowerCase(Locale.ROOT))
						.thenComparing(item -> item.cliente().id()))
				.toList();
	}

	private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
		if (inicio != null && fim != null && inicio.isAfter(fim)) {
			throw new RequisicaoInvalidaException("A data inicial não pode ser posterior à data final.");
		}
	}

}
