package br.com.marwil.ordemservico.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class DonoInicialInicializador implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(DonoInicialInicializador.class);

	private final UsuarioRepository usuarios;
	private final PasswordEncoder codificador;
	private final String nome;
	private final String email;
	private final String senha;

	public DonoInicialInicializador(UsuarioRepository usuarios, PasswordEncoder codificador,
			@Value("${app.dono-inicial.nome:}") String nome,
			@Value("${app.dono-inicial.email:}") String email,
			@Value("${app.dono-inicial.senha:}") String senha) {
		this.usuarios = usuarios;
		this.codificador = codificador;
		this.nome = nome;
		this.email = email;
		this.senha = senha;
	}

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
		LOG.info("Usuário dono inicial criado para {}", dono.getEmail());
	}

	private boolean configuracaoCompleta() {
		return StringUtils.hasText(nome) && StringUtils.hasText(email) && StringUtils.hasText(senha);
	}

}
