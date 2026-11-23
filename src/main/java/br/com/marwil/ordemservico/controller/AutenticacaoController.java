package br.com.marwil.ordemservico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.dto.request.AlterarSenhaRequest;
import br.com.marwil.ordemservico.dto.request.LoginRequest;
import br.com.marwil.ordemservico.dto.response.LoginResponse;
import br.com.marwil.ordemservico.dto.response.UsuarioLogadoResponse;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação")
public class AutenticacaoController {

	private final AutenticacaoService servico;

	public AutenticacaoController(AutenticacaoService servico) {
		this.servico = servico;
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Autentica por e-mail e senha e devolve o token de acesso")
	LoginResponse login(@Valid @RequestBody LoginRequest requisicao) {
		return servico.autenticar(requisicao);
	}

	@GetMapping("/eu")
	@Operation(summary = "Dados do usuário autenticado")
	UsuarioLogadoResponse usuarioAtual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return servico.usuarioAtual(usuario);
	}

	@PutMapping("/senha")
	@Operation(summary = "Altera a senha do próprio usuário")
	ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@Valid @RequestBody AlterarSenhaRequest requisicao) {
		servico.alterarPropriaSenha(usuario, requisicao);
		return ResponseEntity.noContent().build();
	}

}
