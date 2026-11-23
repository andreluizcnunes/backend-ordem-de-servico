package br.com.marwil.ordemservico.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ConversorAutenticacaoJwt implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UsuarioRepository usuarios;

	public ConversorAutenticacaoJwt(UsuarioRepository usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt token) {
		long id = lerIdentificador(token);
		CredencialUsuario credencial = usuarios.buscarCredencialPorId(id)
				.filter(CredencialUsuario::ativo)
				.orElseThrow(() -> new BadCredentialsException("Usuário inexistente ou inativo."));
		return new AutenticacaoUsuario(credencial.comoAutenticado(), token);
	}

	private static long lerIdentificador(Jwt token) {
		try {
			return Long.parseLong(token.getSubject());
		} catch (NumberFormatException excecao) {
			throw new BadCredentialsException("Token com identificação inválida.");
		}
	}

}
