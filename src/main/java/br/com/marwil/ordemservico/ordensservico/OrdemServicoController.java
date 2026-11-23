package br.com.marwil.ordemservico.ordensservico;

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
import br.com.marwil.ordemservico.auth.ApenasTecnicos;
import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.cobrancas.StatusCobranca;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.comum.Textos;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ordens-servico")
public class OrdemServicoController {

	private final OrdemServicoService servico;
	private final EquipeService equipes;

	public OrdemServicoController(OrdemServicoService servico, EquipeService equipes) {
		this.servico = servico;
		this.equipes = equipes;
	}

	@PostMapping("/{id}/assumir")
	@ApenasTecnicos
	OrdemServicoDetalhe assumir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
		equipes.assumir(usuario, id);
		return servico.detalhar(usuario, id);
	}

	@PutMapping("/{id}/tecnicos")
	@ApenasGestores
	OrdemServicoDetalhe definirEquipe(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id,
			@Valid @RequestBody DadosOrdemServico.Equipe dados) {
		equipes.definirEquipe(usuario, id, dados.tecnicosIds());
		return servico.detalhar(usuario, id);
	}

	@PostMapping
	@ApenasGestores
	ResponseEntity<OrdemServicoDetalhe> abrir(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@Valid @RequestBody DadosOrdemServico.Abertura dados) {
		long id = servico.abrir(usuario, dados);
		return ResponseEntity.created(URI.create("/api/v1/ordens-servico/" + id))
				.body(servico.detalhar(usuario, id));
	}

	@PutMapping("/{id}")
	@ApenasGestores
	OrdemServicoDetalhe atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id,
			@Valid @RequestBody DadosOrdemServico.Atualizacao dados) {
		servico.atualizar(id, dados);
		return servico.detalhar(usuario, id);
	}

	@GetMapping
	Pagina<ResumoOrdemServico> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@RequestParam(required = false) StatusAtendimento statusAtendimento,
			@RequestParam(required = false) StatusCobranca statusCobranca,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long tecnicoId,
			@RequestParam(required = false) String busca,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		FiltroOrdensServico filtro = new FiltroOrdensServico(statusAtendimento, statusCobranca, clienteId, tecnicoId,
				Textos.opcional(busca));
		return servico.listar(usuario, filtro, Paginacao.de(pagina, tamanho));
	}

	@GetMapping("/disponiveis")
	Pagina<ResumoOrdemServico> listarDisponiveis(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listarDisponiveis(usuario, Paginacao.de(pagina, tamanho));
	}

	@GetMapping("/minhas")
	@ApenasTecnicos
	Pagina<ResumoOrdemServico> listarMinhas(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@RequestParam(required = false) StatusAtendimento statusAtendimento,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return servico.listarDoTecnico(usuario, statusAtendimento, Paginacao.de(pagina, tamanho));
	}

	@GetMapping("/{id}")
	OrdemServicoDetalhe detalhar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
		return servico.detalhar(usuario, id);
	}

}
