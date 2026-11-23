package br.com.marwil.ordemservico.financeiro;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.comum.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.financeiro.FinanceiroRepository.Totais;

@Service
public class FinanceiroService {

	private final FinanceiroRepository financeiro;
	private final Clock relogio;

	public FinanceiroService(FinanceiroRepository financeiro, Clock relogio) {
		this.financeiro = financeiro;
		this.relogio = relogio;
	}

	@Transactional(readOnly = true)
	public ResumoFinanceiro resumir(LocalDate inicio, LocalDate fim) {
		validarPeriodo(inicio, fim);
		LocalDate hoje = LocalDate.now(relogio);
		Totais recebido = financeiro.totalRecebido(inicio, fim);
		Totais pendente = financeiro.totalPendente(hoje, false);
		Totais vencido = financeiro.totalPendente(hoje, true);
		return new ResumoFinanceiro(inicio, fim,
				recebido.total(), recebido.quantidade(),
				pendente.total(), pendente.quantidade(),
				vencido.total(), vencido.quantidade(),
				financeiro.porCliente(inicio, fim, hoje));
	}

	@Transactional(readOnly = true)
	public Pagina<LancamentoCaixa> listarLancamentos(LocalDate inicio, LocalDate fim, Paginacao paginacao) {
		validarPeriodo(inicio, fim);
		return Pagina.de(financeiro.listarLancamentos(inicio, fim, paginacao), paginacao,
				financeiro.totalRecebido(inicio, fim).quantidade());
	}

	private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
		if (inicio != null && fim != null && inicio.isAfter(fim)) {
			throw new RequisicaoInvalidaException("A data inicial não pode ser posterior à data final.");
		}
	}

}
