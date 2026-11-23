package br.com.marwil.ordemservico.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public record PropriedadesJwt(String segredo, Duration expiracao) {

	private static final int TAMANHO_MINIMO_SEGREDO = 32;

	public PropriedadesJwt {
		if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
			throw new IllegalStateException(
					"APP_JWT_SEGREDO deve ser definido com pelo menos " + TAMANHO_MINIMO_SEGREDO + " bytes.");
		}
		if (expiracao == null || expiracao.isNegative() || expiracao.isZero()) {
			throw new IllegalStateException("APP_JWT_EXPIRACAO deve ser uma duração positiva.");
		}
	}

}
