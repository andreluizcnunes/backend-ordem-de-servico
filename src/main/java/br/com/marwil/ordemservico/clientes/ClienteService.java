package br.com.marwil.ordemservico.clientes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.exception.RecursoNaoEncontradoException;
import br.com.marwil.ordemservico.util.Textos;

@Service
public class ClienteService {

	private final ClienteRepository clientes;

	public ClienteService(ClienteRepository clientes) {
		this.clientes = clientes;
	}

	@Transactional
	public Cliente cadastrar(UsuarioAutenticado responsavel, DadosCliente dados) {
		long id = clientes.inserir(dados.normalizado(), responsavel.id());
		return buscar(id);
	}

	@Transactional
	public Cliente atualizar(long id, DadosCliente dados) {
		if (!clientes.atualizar(id, dados.normalizado())) {
			throw naoEncontrado();
		}
		return buscar(id);
	}

	@Transactional(readOnly = true)
	public Cliente buscar(long id) {
		return clientes.buscarPorId(id).orElseThrow(ClienteService::naoEncontrado);
	}

	@Transactional(readOnly = true)
	public Pagina<Cliente> listar(String busca, Paginacao paginacao) {
		String termo = Textos.opcional(busca);
		return Pagina.de(clientes.listar(termo, paginacao), paginacao, clientes.contar(termo));
	}

	private static RecursoNaoEncontradoException naoEncontrado() {
		return new RecursoNaoEncontradoException("Cliente não encontrado.");
	}

}
