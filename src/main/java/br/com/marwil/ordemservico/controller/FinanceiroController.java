package br.com.marwil.ordemservico.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.dto.response.LancamentoCaixaResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.dto.response.ResumoFinanceiroResponse;
import br.com.marwil.ordemservico.security.ApenasGestores;
import br.com.marwil.ordemservico.service.FinanceiroService;
import br.com.marwil.ordemservico.util.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/financeiro")
@ApenasGestores
@Tag(name = "Financeiro")
public class FinanceiroController {

	private static final Sort ORDENACAO = Sort.by(Sort.Order.desc("dataLancamento"), Sort.Order.desc("id"));

	private final FinanceiroService servico;

	public FinanceiroController(FinanceiroService servico) {
		this.servico = servico;
	}

	@GetMapping("/resumo")
	@Operation(summary = "Total recebido no período, saldo pendente, vencido e pendências por cliente")
	ResumoFinanceiroResponse resumir(@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim) {
		return servico.resumir(inicio, fim);
	}

	@GetMapping("/lancamentos")
	@Operation(summary = "Extrato de entradas no caixa originadas de ordens de serviço")
	PaginaResponse<LancamentoCaixaResponse> listarLancamentos(@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listarLancamentos(inicio, fim, Paginacao.de(pagina, tamanho, ORDENACAO));
	}

}
