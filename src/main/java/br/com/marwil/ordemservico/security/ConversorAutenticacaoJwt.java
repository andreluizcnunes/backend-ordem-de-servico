package br.com.marwil.ordemservico.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConversorAutenticacaoJwt implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UsuarioRepository usuarios;

	@Override
	@Transactional(readOnly = true)
	public AbstractAuthenticationToken convert(Jwt token) {
		Usuario usuario = usuarios.findById(lerIdentificador(token))
				.filter(Usuario::isAtivo)
				.orElseThrow(() -> new BadCredentialsException("Usuário inexistente ou inativo."));
		return new AutenticacaoUsuario(new UsuarioAutenticado(usuario.getId(), usuario.getNome(), usuario.getPerfil()),
				token);
	}

	private static long lerIdentificador(Jwt token) {
		try {
			return Long.parseLong(token.getSubject());
		} catch (NumberFormatException excecao) {
			throw new BadCredentialsException("Token com identificação inválida.");
		}
	}

}
