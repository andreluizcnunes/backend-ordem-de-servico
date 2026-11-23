package br.com.marwil.ordemservico.security;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.marwil.ordemservico.config.PropriedadesJwt;

@Service
public class TokenService {

	private static final String EMISSOR = "backend-ordem-de-servico";

	private final JwtEncoder encoder;
	private final PropriedadesJwt propriedades;
	private final Clock relogio;

	public TokenService(JwtEncoder encoder, PropriedadesJwt propriedades, Clock relogio) {
		this.encoder = encoder;
		this.propriedades = propriedades;
		this.relogio = relogio;
	}

	public TokenEmitido emitir(UsuarioAutenticado usuario) {
		Instant agora = relogio.instant();
		Instant expiraEm = agora.plus(propriedades.expiracao());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(EMISSOR)
				.subject(String.valueOf(usuario.id()))
				.issuedAt(agora)
				.expiresAt(expiraEm)
				.claim("perfil", usuario.perfil().name())
				.claim("nome", usuario.nome())
				.build();
		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
		return new TokenEmitido(token, expiraEm);
	}

	public record TokenEmitido(String token, Instant expiraEm) {
	}

}
