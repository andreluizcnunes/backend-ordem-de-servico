package br.com.marwil.ordemservico.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PropriedadesJwt.class)
public class JwtConfig {

	@Bean
	JwtEncoder jwtEncoder(PropriedadesJwt propriedades) {
		return NimbusJwtEncoder.withSecretKey(chave(propriedades)).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(PropriedadesJwt propriedades) {
		return NimbusJwtDecoder.withSecretKey(chave(propriedades)).macAlgorithm(MacAlgorithm.HS256).build();
	}

	private static SecretKey chave(PropriedadesJwt propriedades) {
		return new SecretKeySpec(propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

}
