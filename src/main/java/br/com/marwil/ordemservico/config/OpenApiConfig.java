package br.com.marwil.ordemservico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

	private static final String ESQUEMA_TOKEN = "bearerAuth";

	@Bean
	OpenAPI documentacaoApi() {
		return new OpenAPI()
				.info(new Info()
						.title("API do Sistema de Gestão de Ordens de Serviço")
						.description("Ordens de serviço, equipe técnica, precificação, cobrança, pagamento e caixa. "
								+ "Autentique em POST /api/v1/auth/login e use o token no botão Authorize.")
						.version("v1"))
				.components(new Components().addSecuritySchemes(ESQUEMA_TOKEN, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_TOKEN));
	}

}
