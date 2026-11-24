package br.com.marwil.ordemservico.controller;

import java.net.URI;

import org.springframework.data.domain.Sort;
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

import br.com.marwil.ordemservico.dto.request.EnvioCobrancaRequest;
import br.com.marwil.ordemservico.dto.request.PagamentoRequest;
import br.com.marwil.ordemservico.dto.response.CobrancaResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.security.ApenasGestores;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.CobrancaService;
import br.com.marwil.ordemservico.util.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@ApenasGestores
@Tag(name = "Cobranças")
@RequiredArgsConstructor
public class CobrancaController {

	private static final Sort ORDENACAO = Sort.by(
			Sort.Order.desc("status"),
			Sort.Order.asc("dataVencimento").nullsLast(),
			Sort.Order.desc("enviadaEm"),
			Sort.Order.desc("id"));

	private final CobrancaService servico;

	@PostMapping("/ordens-servico/{id}/enviar-cobranca")
	@Operation(summary = "Envia a ordem concluída e precificada à cobrança como pendente")
	ResponseEntity<CobrancaResponse> enviar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
			@Valid @RequestBody(required = false) EnvioCobrancaRequest requisicao) {
		CobrancaResponse cobranca = servico.enviar(usuario, id, requisicao);
		return ResponseEntity.created(URI.create("/api/v1/cobrancas/" + cobranca.id())).body(cobranca);
	}

	@GetMapping("/cobrancas")
	@Operation(summary = "Painel de cobranças pendentes e pagas")
	PaginaResponse<CobrancaResponse> listar(@RequestParam(required = false) StatusCobranca status,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long ordemServicoId,
			@RequestParam(required = false) Boolean vencidas,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listar(status, clienteId, ordemServicoId, vencidas, Paginacao.de(pagina, tamanho, ORDENACAO));
	}

	@GetMapping("/cobrancas/{id}")
	@Operation(summary = "Consulta uma cobrança com o pagamento, quando houver")
	CobrancaResponse buscar(@PathVariable Long id) {
		return servico.buscar(id);
	}

	@PostMapping("/cobrancas/{id}/pagamentos")
	@Operation(summary = "Registra o pagamento integral e gera uma única entrada no caixa")
	ResponseEntity<CobrancaResponse> registrarPagamento(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable Long id, @Valid @RequestBody(required = false) PagamentoRequest requisicao) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servico.registrarPagamento(usuario, id, requisicao));
	}

}
