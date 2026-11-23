package br.com.marwil.ordemservico.comum;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RelogioConfig {

	@Bean
	Clock relogio(@Value("${app.fuso-horario}") String fusoHorario) {
		return Clock.system(ZoneId.of(fusoHorario));
	}

}
