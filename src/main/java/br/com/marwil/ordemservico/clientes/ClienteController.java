package br.com.marwil.ordemservico.clientes;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.auth.ApenasGestores;
import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clientes")
@ApenasGestores
public class ClienteController {

	private final ClienteService servico;

	public ClienteController(ClienteService servico) {
		this.servico = servico;
	}

	@GetMapping
	Pagina<Cliente> listar(@RequestParam(required = false) String busca,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listar(busca, Paginacao.de(pagina, tamanho));
	}

	@GetMapping("/{id}")
	Cliente buscar(@PathVariable long id) {
		return servico.buscar(id);
	}

	@PostMapping
	ResponseEntity<Cliente> cadastrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@Valid @RequestBody DadosCliente dados) {
		Cliente cliente = servico.cadastrar(usuario, dados);
		return ResponseEntity.created(URI.create("/api/v1/clientes/" + cliente.id())).body(cliente);
	}

	@PutMapping("/{id}")
	Cliente atualizar(@PathVariable long id, @Valid @RequestBody DadosCliente dados) {
		return servico.atualizar(id, dados);
	}

}
