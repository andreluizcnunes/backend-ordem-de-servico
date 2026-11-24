package br.com.marwil.ordemservico.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.RedefinirSenhaRequest;
import br.com.marwil.ordemservico.dto.request.UsuarioAtualizacaoRequest;
import br.com.marwil.ordemservico.dto.request.UsuarioRequest;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.dto.response.UsuarioResponse;
import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.mapper.UsuarioMapper;
import br.com.marwil.ordemservico.repository.OrdemServicoTecnicoRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.repository.specification.UsuarioSpecifications;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarios;
	private final OrdemServicoTecnicoRepository equipes;
	private final PasswordEncoder codificador;
	private final UsuarioMapper mapper;

	@Transactional
	public UsuarioResponse cadastrar(UsuarioRequest requisicao) {
		if (usuarios.existsByEmailIgnoreCase(requisicao.email())) {
			throw emailEmUso();
		}
		Usuario usuario = mapper.paraEntidade(requisicao);
		usuario.setSenhaHash(codificador.encode(requisicao.senha()));
		return mapper.paraResponse(usuarios.saveAndFlush(usuario));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscar(Long id) {
		return mapper.paraResponse(buscarEntidade(id));
	}

	@Transactional(readOnly = true)
	public PaginaResponse<UsuarioResponse> listar(Perfil perfil, Boolean ativo, Pageable paginacao) {
		Specification<Usuario> filtro = Specification.allOf(List.of(
				UsuarioSpecifications.comPerfil(perfil),
				UsuarioSpecifications.comSituacaoAtiva(ativo)));
		return PaginaResponse.de(usuarios.findAll(filtro, paginacao).map(mapper::paraResponse));
	}

	@Transactional
	public UsuarioResponse atualizar(UsuarioAutenticado responsavel, Long id, UsuarioAtualizacaoRequest requisicao) {
		Usuario usuario = buscarEntidade(id);
		if (usuarios.existsByEmailIgnoreCaseAndIdNot(requisicao.email(), id)) {
			throw emailEmUso();
		}
		boolean alteraPropriaConta = responsavel.id() == id;
		if (alteraPropriaConta && (requisicao.perfil() != usuario.getPerfil() || !requisicao.ativo())) {
			throw new ConflitoException("O dono não pode alterar o próprio perfil nem se desativar.");
		}
		boolean deixaDeSerTecnico = usuario.getPerfil() == Perfil.TECNICO && requisicao.perfil() != Perfil.TECNICO;
		if (deixaDeSerTecnico && equipes.existsByTecnicoId(id)) {
			throw new ConflitoException("O técnico possui ordens de serviço atribuídas e não pode mudar de perfil.");
		}
		mapper.atualizar(requisicao, usuario);
		return mapper.paraResponse(usuarios.saveAndFlush(usuario));
	}

	@Transactional
	public void redefinirSenha(Long id, RedefinirSenhaRequest requisicao) {
		buscarEntidade(id).setSenhaHash(codificador.encode(requisicao.novaSenha()));
	}

	private Usuario buscarEntidade(Long id) {
		return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
	}

	private static ConflitoException emailEmUso() {
		return new ConflitoException("Já existe um usuário com este e-mail.");
	}

}
