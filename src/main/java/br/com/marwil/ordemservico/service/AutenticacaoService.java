package br.com.marwil.ordemservico.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.AlterarSenhaRequest;
import br.com.marwil.ordemservico.dto.request.LoginRequest;
import br.com.marwil.ordemservico.dto.response.LoginResponse;
import br.com.marwil.ordemservico.dto.response.UsuarioLogadoResponse;
import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.mapper.UsuarioMapper;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.security.TokenService;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.security.UsuarioDetails;

@Service
public class AutenticacaoService {

	private static final String TIPO_TOKEN = "Bearer";

	private final AuthenticationManager gerenciadorAutenticacao;
	private final TokenService tokens;
	private final UsuarioRepository usuarios;
	private final PasswordEncoder codificador;
	private final UsuarioMapper mapper;

	public AutenticacaoService(AuthenticationManager gerenciadorAutenticacao, TokenService tokens,
			UsuarioRepository usuarios, PasswordEncoder codificador, UsuarioMapper mapper) {
		this.gerenciadorAutenticacao = gerenciadorAutenticacao;
		this.tokens = tokens;
		this.usuarios = usuarios;
		this.codificador = codificador;
		this.mapper = mapper;
	}

	public LoginResponse autenticar(LoginRequest requisicao) {
		Authentication autenticacao = gerenciadorAutenticacao.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(requisicao.email(), requisicao.senha()));
		UsuarioAutenticado usuario = ((UsuarioDetails) autenticacao.getPrincipal()).comoUsuarioAutenticado();
		TokenService.TokenEmitido token = tokens.emitir(usuario);
		return new LoginResponse(token.token(), TIPO_TOKEN, token.expiraEm(), mapper.paraLogado(usuario));
	}

	public UsuarioLogadoResponse usuarioAtual(UsuarioAutenticado usuario) {
		return mapper.paraLogado(usuario);
	}

	@Transactional
	public void alterarPropriaSenha(UsuarioAutenticado usuarioAtual, AlterarSenhaRequest requisicao) {
		Usuario usuario = usuarios.findById(usuarioAtual.id())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
		if (!codificador.matches(requisicao.senhaAtual(), usuario.getSenhaHash())) {
			throw new RequisicaoInvalidaException("A senha atual não confere.");
		}
		usuario.setSenhaHash(codificador.encode(requisicao.novaSenha()));
	}

}
