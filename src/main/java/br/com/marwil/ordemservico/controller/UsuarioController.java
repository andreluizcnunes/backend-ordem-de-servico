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

import br.com.marwil.ordemservico.dto.request.RedefinirSenhaRequest;
import br.com.marwil.ordemservico.dto.request.UsuarioAtualizacaoRequest;
import br.com.marwil.ordemservico.dto.request.UsuarioRequest;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.dto.response.UsuarioResponse;
import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.security.ApenasDono;
import br.com.marwil.ordemservico.security.ApenasGestores;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.UsuarioService;
import br.com.marwil.ordemservico.util.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

	private static final Sort ORDENACAO = Sort.by(Sort.Order.asc("nome").ignoreCase(), Sort.Order.asc("id"));

	private final UsuarioService servico;

	public UsuarioController(UsuarioService servico) {
		this.servico = servico;
	}

	@GetMapping
	@ApenasGestores
	@Operation(summary = "Lista usuários por perfil e situação, usado para escolher técnicos")
	PaginaResponse<UsuarioResponse> listar(@RequestParam(required = false) Perfil perfil,
			@RequestParam(required = false) Boolean ativo,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listar(perfil, ativo, Paginacao.de(pagina, tamanho, ORDENACAO));
	}

	@GetMapping("/{id}")
	@ApenasDono
	@Operation(summary = "Consulta um usuário")
	UsuarioResponse buscar(@PathVariable Long id) {
		return servico.buscar(id);
	}

	@PostMapping
	@ApenasDono
	@Operation(summary = "Cadastra um usuário")
	ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest requisicao) {
		UsuarioResponse usuario = servico.cadastrar(requisicao);
		return ResponseEntity.created(URI.create("/api/v1/usuarios/" + usuario.id())).body(usuario);
	}

	@PutMapping("/{id}")
	@ApenasDono
	@Operation(summary = "Edita nome, e-mail, perfil e situação de um usuário")
	UsuarioResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado responsavel, @PathVariable Long id,
			@Valid @RequestBody UsuarioAtualizacaoRequest requisicao) {
		return servico.atualizar(responsavel, id, requisicao);
	}

	@PutMapping("/{id}/senha")
	@ApenasDono
	@Operation(summary = "Redefine a senha de um usuário")
	ResponseEntity<Void> redefinirSenha(@PathVariable Long id, @Valid @RequestBody RedefinirSenhaRequest requisicao) {
		servico.redefinirSenha(id, requisicao);
		return ResponseEntity.noContent().build();
	}

}
