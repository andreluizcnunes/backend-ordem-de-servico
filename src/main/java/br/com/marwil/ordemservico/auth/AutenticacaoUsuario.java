package br.com.marwil.ordemservico.auth;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class AutenticacaoUsuario extends AbstractAuthenticationToken {

	private final UsuarioAutenticado usuario;
	private final Jwt token;

	public AutenticacaoUsuario(UsuarioAutenticado usuario, Jwt token) {
		super(List.of(new SimpleGrantedAuthority(usuario.perfil().autoridade())));
		this.usuario = usuario;
		this.token = token;
		setAuthenticated(true);
	}

	@Override
	public UsuarioAutenticado getPrincipal() {
		return usuario;
	}

	@Override
	public Jwt getCredentials() {
		return token;
	}

	@Override
	public String getName() {
		return String.valueOf(usuario.id());
	}

}
