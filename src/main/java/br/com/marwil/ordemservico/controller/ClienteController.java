package br.com.marwil.ordemservico.controller;

import java.net.URI;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.dto.request.ClienteRequest;
import br.com.marwil.ordemservico.dto.response.ClienteResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.security.ApenasGestores;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.ClienteService;
import br.com.marwil.ordemservico.util.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clientes")
@ApenasGestores
@Tag(name = "Clientes")
@RequiredArgsConstructor
public class ClienteController {

	private static final Sort ORDENACAO = Sort.by(Sort.Order.asc("nome").ignoreCase(), Sort.Order.asc("id"));

	private final ClienteService servico;

	@GetMapping
	@Operation(summary = "Lista clientes, com busca por nome, contato, telefone ou e-mail")
	PaginaResponse<ClienteResponse> listar(@RequestParam(required = false) String busca,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listar(busca, Paginacao.de(pagina, tamanho, ORDENACAO));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um cliente")
	ClienteResponse buscar(@PathVariable Long id) {
		return servico.buscar(id);
	}

	@PostMapping
	@Operation(summary = "Cadastra um cliente")
	ResponseEntity<ClienteResponse> cadastrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@Valid @RequestBody ClienteRequest requisicao) {
		ClienteResponse cliente = servico.cadastrar(usuario, requisicao);
		return ResponseEntity.created(URI.create("/api/v1/clientes/" + cliente.id())).body(cliente);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita um cliente")
	ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest requisicao) {
		return servico.atualizar(id, requisicao);
	}

}
