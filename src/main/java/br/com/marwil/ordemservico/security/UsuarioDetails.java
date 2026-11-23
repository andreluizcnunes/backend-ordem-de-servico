package br.com.marwil.ordemservico.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.marwil.ordemservico.entity.Usuario;

public class UsuarioDetails implements UserDetails {

	private final UsuarioAutenticado usuario;
	private final String email;
	private final String senhaHash;
	private final boolean ativo;

	public UsuarioDetails(Usuario usuario) {
		this.usuario = new UsuarioAutenticado(usuario.getId(), usuario.getNome(), usuario.getPerfil());
		this.email = usuario.getEmail();
		this.senhaHash = usuario.getSenhaHash();
		this.ativo = usuario.isAtivo();
	}

	public UsuarioAutenticado comoUsuarioAutenticado() {
		return usuario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(usuario.perfil().autoridade()));
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return ativo;
	}

}
