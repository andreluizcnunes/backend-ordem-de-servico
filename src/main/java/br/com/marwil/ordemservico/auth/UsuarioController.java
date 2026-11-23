package br.com.marwil.ordemservico.auth;

import br.com.marwil.ordemservico.enums.Perfil;

import java.net.URI;

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

import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

	private final UsuarioService servico;

	public UsuarioController(UsuarioService servico) {
		this.servico = servico;
	}

	@GetMapping
	@ApenasGestores
	Pagina<Usuario> listar(@RequestParam(required = false) Perfil perfil,
			@RequestParam(required = false) Boolean ativo,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listar(perfil, ativo, Paginacao.de(pagina, tamanho));
	}

	@GetMapping("/{id}")
	@ApenasDono
	Usuario buscar(@PathVariable long id) {
		return servico.buscar(id);
	}

	@PostMapping
	@ApenasDono
	ResponseEntity<Usuario> cadastrar(@Valid @RequestBody RequisicaoCadastroUsuario requisicao) {
		Usuario usuario = servico.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha(),
				requisicao.perfil());
		return ResponseEntity.created(URI.create("/api/v1/usuarios/" + usuario.id())).body(usuario);
	}

	@PutMapping("/{id}")
	@ApenasDono
	Usuario atualizar(@AuthenticationPrincipal UsuarioAutenticado responsavel, @PathVariable long id,
			@Valid @RequestBody RequisicaoAtualizacaoUsuario requisicao) {
		return servico.atualizar(responsavel, id, requisicao.nome(), requisicao.email(), requisicao.perfil(),
				requisicao.ativo());
	}

	@PutMapping("/{id}/senha")
	@ApenasDono
	ResponseEntity<Void> redefinirSenha(@PathVariable long id,
			@Valid @RequestBody RequisicaoRedefinicaoSenha requisicao) {
		servico.redefinirSenha(id, requisicao.novaSenha());
		return ResponseEntity.noContent().build();
	}

	record RequisicaoCadastroUsuario(
			@NotBlank @Size(max = 150) String nome,
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(min = 8, max = 72) String senha,
			@NotNull Perfil perfil) {
	}

	record RequisicaoAtualizacaoUsuario(
			@NotBlank @Size(max = 150) String nome,
			@NotBlank @Email @Size(max = 254) String email,
			@NotNull Perfil perfil,
			@NotNull Boolean ativo) {
	}

	record RequisicaoRedefinicaoSenha(@NotBlank @Size(min = 8, max = 72) String novaSenha) {
	}

}
