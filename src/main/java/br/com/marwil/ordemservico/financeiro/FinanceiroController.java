package br.com.marwil.ordemservico.financeiro;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.auth.ApenasGestores;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;

@RestController
@RequestMapping("/api/v1/financeiro")
@ApenasGestores
public class FinanceiroController {

	private final FinanceiroService servico;

	public FinanceiroController(FinanceiroService servico) {
		this.servico = servico;
	}

	@GetMapping("/resumo")
	ResumoFinanceiro resumir(@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim) {
		return servico.resumir(inicio, fim);
	}

	@GetMapping("/lancamentos")
	Pagina<LancamentoCaixa> listarLancamentos(@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listarLancamentos(inicio, fim, Paginacao.de(pagina, tamanho));
	}

}
