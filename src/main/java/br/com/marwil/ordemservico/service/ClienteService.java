package br.com.marwil.ordemservico.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.ClienteRequest;
import br.com.marwil.ordemservico.dto.response.ClienteResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.entity.Cliente;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.mapper.ClienteMapper;
import br.com.marwil.ordemservico.repository.ClienteRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.repository.specification.ClienteSpecifications;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.util.Textos;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository clientes;
	private final UsuarioRepository usuarios;
	private final ClienteMapper mapper;

	@Transactional
	public ClienteResponse cadastrar(UsuarioAutenticado responsavel, ClienteRequest requisicao) {
		Cliente cliente = mapper.paraEntidade(requisicao);
		cliente.setCriadoPor(usuarios.getReferenceById(responsavel.id()));
		return mapper.paraResponse(clientes.saveAndFlush(cliente));
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest requisicao) {
		Cliente cliente = buscarEntidade(id);
		mapper.atualizar(requisicao, cliente);
		return mapper.paraResponse(clientes.saveAndFlush(cliente));
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscar(Long id) {
		return mapper.paraResponse(buscarEntidade(id));
	}

	@Transactional(readOnly = true)
	public PaginaResponse<ClienteResponse> listar(String busca, Pageable paginacao) {
		return PaginaResponse.de(clientes.findAll(ClienteSpecifications.comTermo(Textos.opcional(busca)), paginacao)
				.map(mapper::paraResponse));
	}

	private Cliente buscarEntidade(Long id) {
		return clientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
	}

}
