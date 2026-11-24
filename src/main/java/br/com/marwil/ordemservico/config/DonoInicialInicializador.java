package br.com.marwil.ordemservico.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class DonoInicialInicializador implements ApplicationRunner {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder codificador;

	@Value("${app.dono-inicial.nome:}")
	private final String nome;

	@Value("${app.dono-inicial.email:}")
	private final String email;

	@Value("${app.dono-inicial.senha:}")
	private final String senha;

	@Override
	@Transactional
	public void run(ApplicationArguments argumentos) {
		if (!configuracaoCompleta() || usuarios.count() > 0) {
			return;
		}
		Usuario dono = new Usuario();
		dono.setNome(nome.strip());
		dono.setEmail(email.strip());
		dono.setSenhaHash(codificador.encode(senha));
		dono.setPerfil(Perfil.DONO);
		usuarios.save(dono);
		log.info("Usuário dono inicial criado para {}", dono.getEmail());
	}

	private boolean configuracaoCompleta() {
		return StringUtils.hasText(nome) && StringUtils.hasText(email) && StringUtils.hasText(senha);
	}

}
