package br.com.marwil.ordemservico.auth;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacaoController {

	private final AutenticacaoService autenticacao;

	public AutenticacaoController(AutenticacaoService autenticacao) {
		this.autenticacao = autenticacao;
	}

	@PostMapping("/login")
	RespostaLogin login(@Valid @RequestBody RequisicaoLogin requisicao) {
		AutenticacaoService.SessaoAutenticada sessao = autenticacao.autenticar(requisicao.email(), requisicao.senha());
		return new RespostaLogin(sessao.token().token(), "Bearer", sessao.token().expiraEm(), sessao.usuario());
	}

	@GetMapping("/eu")
	UsuarioAutenticado usuarioAtual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return usuario;
	}

	@PutMapping("/senha")
	ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@Valid @RequestBody RequisicaoAlteracaoSenha requisicao) {
		autenticacao.alterarPropriaSenha(usuario, requisicao.senhaAtual(), requisicao.novaSenha());
		return ResponseEntity.noContent().build();
	}

	record RequisicaoLogin(@NotBlank String email, @NotBlank String senha) {
	}

	record RespostaLogin(String token, String tipo, Instant expiraEm, UsuarioAutenticado usuario) {
	}

	record RequisicaoAlteracaoSenha(
			@NotBlank String senhaAtual,
			@NotBlank @Size(min = 8, max = 72) String novaSenha) {
	}

}
