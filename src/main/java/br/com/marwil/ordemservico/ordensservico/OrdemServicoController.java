package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ordens-servico")
public class OrdemServicoController {

	private final OrdemServicoService servico;
	private final EquipeService equipes;
	private final AtendimentoService atendimento;
	private final PrecificacaoService precificacao;

	public OrdemServicoController(OrdemServicoService servico, EquipeService equipes,
			AtendimentoService atendimento, PrecificacaoService precificacao) {
		this.servico = servico;
		this.equipes = equipes;
		this.atendimento = atendimento;
		this.precificacao = precificacao;
	}

	@GetMapping("/{id}/itens")
	@ApenasGestores
	Precificacao consultarItens(@PathVariable long id) {
		return precificacao.consultar(id);
	}

	@PutMapping("/{id}/itens")
	@ApenasGestores
	Precificacao definirItens(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id,
			@Valid @RequestBody DadosOrdemServico.Itens dados) {
		return precificacao.definirItens(usuario, id, dados.itens());
	}

	@PatchMapping("/{id}/atendimento")
	ResponseEntity<OrdemServicoDetalhe> alterarSituacao(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable long id, @Valid @RequestBody DadosOrdemServico.Situacao dados) {
		atendimento.alterarSituacao(usuario, id, dados.status(), dados.motivo());
		return servico.detalharSeVisivel(usuario, id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@GetMapping("/{id}/registros")
	List<RegistroAtendimento> listarRegistros(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable long id) {
		return atendimento.listarRegistros(usuario, id);
	}

	@PostMapping("/{id}/registros")
	ResponseEntity<RegistroAtendimento> registrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable long id, @Valid @RequestBody DadosOrdemServico.Registro dados) {
		RegistroAtendimento registro = atendimento.registrar(usuario, id, dados.descricao(),
				dados.materiaisUtilizados());
		return ResponseEntity.status(HttpStatus.CREATED).body(registro);
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
