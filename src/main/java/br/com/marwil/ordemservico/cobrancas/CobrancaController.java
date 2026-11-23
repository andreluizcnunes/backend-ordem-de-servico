package br.com.marwil.ordemservico.cobrancas;

import br.com.marwil.ordemservico.enums.StatusCobranca;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.security.ApenasGestores;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

@RestController
@RequestMapping("/api/v1")
@ApenasGestores
public class CobrancaController {

	private final CobrancaService servico;

	public CobrancaController(CobrancaService servico) {
		this.servico = servico;
	}

	@PostMapping("/ordens-servico/{id}/enviar-cobranca")
	ResponseEntity<Cobranca> enviar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id,
			@Valid @RequestBody(required = false) RequisicaoEnvio requisicao) {
		LocalDate vencimento = requisicao == null ? null : requisicao.dataVencimento();
		Cobranca cobranca = servico.enviar(usuario, id, vencimento);
		return ResponseEntity.created(URI.create("/api/v1/cobrancas/" + cobranca.id())).body(cobranca);
	}

	@GetMapping("/cobrancas")
	Pagina<Cobranca> listar(@RequestParam(required = false) StatusCobranca status,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long ordemServicoId,
			@RequestParam(required = false) Boolean vencidas,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		if (status == StatusCobranca.NAO_ENVIADA) {
			return Pagina.de(List.of(), Paginacao.de(pagina, tamanho), 0);
		}
		return servico.listar(new FiltroCobrancas(status, clienteId, ordemServicoId, vencidas),
				Paginacao.de(pagina, tamanho));
	}

	@GetMapping("/cobrancas/{id}")
	Cobranca buscar(@PathVariable long id) {
		return servico.buscar(id);
	}

	@PostMapping("/cobrancas/{id}/pagamentos")
	ResponseEntity<Cobranca> registrarPagamento(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable long id, @Valid @RequestBody(required = false) RequisicaoPagamento requisicao) {
		LocalDate data = requisicao == null ? null : requisicao.dataPagamento();
		BigDecimal valor = requisicao == null ? null : requisicao.valorRecebido();
		return ResponseEntity.status(HttpStatus.CREATED).body(servico.registrarPagamento(usuario, id, data, valor));
	}

	record RequisicaoEnvio(LocalDate dataVencimento) {
	}

	record RequisicaoPagamento(
			LocalDate dataPagamento,
			@DecimalMin(value = "0", inclusive = false) @Digits(integer = 12, fraction = 2) BigDecimal valorRecebido) {
	}

}
