package br.com.marwil.ordemservico.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.comum.CredenciaisInvalidasException;
import br.com.marwil.ordemservico.comum.RequisicaoInvalidaException;

@Service
public class AutenticacaoService {

	private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";

	private final UsuarioRepository usuarios;
	private final PasswordEncoder codificador;
	private final TokenService tokens;
	private final String hashParaUsuarioInexistente;

	public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder codificador, TokenService tokens) {
		this.usuarios = usuarios;
		this.codificador = codificador;
		this.tokens = tokens;
		this.hashParaUsuarioInexistente = codificador.encode("senha-inexistente-para-tempo-constante");
	}

	@Transactional(readOnly = true)
	public SessaoAutenticada autenticar(String email, String senha) {
		CredencialUsuario credencial = usuarios.buscarCredencialPorEmail(email.strip()).orElse(null);
		if (credencial == null) {
			codificador.matches(senha, hashParaUsuarioInexistente);
			throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
		}
		if (!codificador.matches(senha, credencial.senhaHash()) || !credencial.ativo()) {
			throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
		}
		UsuarioAutenticado usuario = credencial.comoAutenticado();
		return new SessaoAutenticada(usuario, tokens.emitir(usuario));
	}

	@Transactional
	public void alterarPropriaSenha(UsuarioAutenticado usuario, String senhaAtual, String novaSenha) {
		CredencialUsuario credencial = usuarios.buscarCredencialPorId(usuario.id())
				.orElseThrow(() -> new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS));
		if (!codificador.matches(senhaAtual, credencial.senhaHash())) {
			throw new RequisicaoInvalidaException("A senha atual não confere.");
		}
		usuarios.atualizarSenha(usuario.id(), codificador.encode(novaSenha));
	}

	public record SessaoAutenticada(UsuarioAutenticado usuario, TokenService.TokenEmitido token) {
	}

}
