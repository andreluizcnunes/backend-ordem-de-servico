package br.com.marwil.ordemservico.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarios;

	@Override
	@Transactional(readOnly = true)
	public UsuarioDetails loadUserByUsername(String email) {
		return usuarios.findByEmailIgnoreCase(email.strip())
				.map(UsuarioDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
	}

}
