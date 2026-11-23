package br.com.marwil.ordemservico.auth;

import br.com.marwil.ordemservico.enums.Perfil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder codificador;

	public UsuarioService(UsuarioRepository usuarios, PasswordEncoder codificador) {
		this.usuarios = usuarios;
		this.codificador = codificador;
	}

	@Transactional
	public Usuario cadastrar(String nome, String email, String senha, Perfil perfil) {
		String emailNormalizado = email.strip();
		garantirEmailDisponivel(emailNormalizado, null);
		long id = usuarios.inserir(nome.strip(), emailNormalizado, codificador.encode(senha), perfil);
		return buscar(id);
	}

	@Transactional(readOnly = true)
	public Usuario buscar(long id) {
		return usuarios.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
	}

	@Transactional(readOnly = true)
	public Pagina<Usuario> listar(Perfil perfil, Boolean ativo, Paginacao paginacao) {
		return Pagina.de(usuarios.listar(perfil, ativo, paginacao), paginacao, usuarios.contar(perfil, ativo));
	}

	@Transactional
	public Usuario atualizar(UsuarioAutenticado responsavel, long id, String nome, String email, Perfil perfil,
			boolean ativo) {
		Usuario atual = buscar(id);
		String emailNormalizado = email.strip();
		garantirEmailDisponivel(emailNormalizado, id);
		if (responsavel.id() == id && (perfil != atual.perfil() || !ativo)) {
			throw new ConflitoException("O dono não pode alterar o próprio perfil nem se desativar.");
		}
		if (atual.perfil() == Perfil.TECNICO && perfil != Perfil.TECNICO && usuarios.possuiOrdensAtribuidas(id)) {
			throw new ConflitoException("O técnico possui ordens de serviço atribuídas e não pode mudar de perfil.");
		}
		usuarios.atualizar(id, nome.strip(), emailNormalizado, perfil, ativo);
		return buscar(id);
	}

	@Transactional
	public void redefinirSenha(long id, String novaSenha) {
		buscar(id);
		usuarios.atualizarSenha(id, codificador.encode(novaSenha));
	}

	private void garantirEmailDisponivel(String email, Long ignorarId) {
		if (usuarios.existeEmail(email, ignorarId)) {
			throw new ConflitoException("Já existe um usuário com este e-mail.");
		}
	}

}
