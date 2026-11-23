package br.com.marwil.ordemservico.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.marwil.ordemservico.security.ConversorAutenticacaoJwt;
import br.com.marwil.ordemservico.security.UsuarioDetailsService;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] ROTAS_PUBLICAS_DOCUMENTACAO = {
			"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

	@Bean
	SecurityFilterChain cadeiaSeguranca(HttpSecurity http, ConversorAutenticacaoJwt conversor) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(rotas -> rotas
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers(ROTAS_PUBLICAS_DOCUMENTACAO).permitAll()
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(servidor -> servidor
						.jwt(jwt -> jwt.jwtAuthenticationConverter(conversor)));
		return http.build();
	}

	@Bean
	AuthenticationManager authenticationManager(UsuarioDetailsService usuarios, PasswordEncoder codificador) {
		DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(usuarios);
		provedor.setPasswordEncoder(codificador);
		return new ProviderManager(provedor);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origens}") List<String> origens) {
		CorsConfiguration configuracao = new CorsConfiguration();
		configuracao.setAllowedOrigins(origens);
		configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuracao.setExposedHeaders(List.of("Content-Disposition"));
		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/api/**", configuracao);
		return fonte;
	}

}
