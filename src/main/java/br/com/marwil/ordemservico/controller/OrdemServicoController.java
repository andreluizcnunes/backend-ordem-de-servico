package br.com.marwil.ordemservico.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Sort;
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

import br.com.marwil.ordemservico.dto.request.EquipeRequest;
import br.com.marwil.ordemservico.dto.request.ItensOrdemServicoRequest;
import br.com.marwil.ordemservico.dto.request.OrdemServicoAtualizacaoRequest;
import br.com.marwil.ordemservico.dto.request.OrdemServicoRequest;
import br.com.marwil.ordemservico.dto.request.RegistroAtendimentoRequest;
import br.com.marwil.ordemservico.dto.request.SituacaoAtendimentoRequest;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResponse;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResumoResponse;
import br.com.marwil.ordemservico.dto.response.PaginaResponse;
import br.com.marwil.ordemservico.dto.response.PrecificacaoResponse;
import br.com.marwil.ordemservico.dto.response.RegistroAtendimentoResponse;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.security.ApenasGestores;
import br.com.marwil.ordemservico.security.ApenasTecnicos;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.AtendimentoService;
import br.com.marwil.ordemservico.service.EquipeService;
import br.com.marwil.ordemservico.service.OrdemServicoService;
import br.com.marwil.ordemservico.service.PrecificacaoService;
import br.com.marwil.ordemservico.util.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ordens-servico")
@Tag(name = "Ordens de serviço")
@RequiredArgsConstructor
public class OrdemServicoController {

	private static final Sort ORDENACAO = Sort.by(Sort.Order.desc("dataAbertura"), Sort.Order.desc("id"));

	private final OrdemServicoService ordens;
	private final EquipeService equipes;
	private final AtendimentoService atendimento;
	private final PrecificacaoService precificacao;

	@PostMapping
	@ApenasGestores
	@Operation(summary = "Abre uma ordem de serviço, opcionalmente já com a equipe técnica")
	ResponseEntity<OrdemServicoResponse> abrir(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@Valid @RequestBody OrdemServicoRequest requisicao) {
		Long id = ordens.abrir(usuario, requisicao);
		return ResponseEntity.created(URI.create("/api/v1/ordens-servico/" + id)).body(ordens.detalhar(usuario, id));
	}

	@PutMapping("/{id}")
	@ApenasGestores
	@Operation(summary = "Edita descrição, endereço e contatos até o envio à cobrança")
	OrdemServicoResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
			@Valid @RequestBody OrdemServicoAtualizacaoRequest requisicao) {
		ordens.atualizar(id, requisicao);
		return ordens.detalhar(usuario, id);
	}

	@GetMapping
	@Operation(summary = "Lista ordens de serviço; o técnico vê apenas as disponíveis e as suas não concluídas")
	PaginaResponse<OrdemServicoResumoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@RequestParam(required = false) StatusAtendimento statusAtendimento,
			@RequestParam(required = false) StatusCobranca statusCobranca,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long tecnicoId,
			@RequestParam(required = false) String busca,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return ordens.listar(usuario, statusAtendimento, statusCobranca, clienteId, tecnicoId, busca,
				Paginacao.de(pagina, tamanho, ORDENACAO));
	}

	@GetMapping("/disponiveis")
	@Operation(summary = "Ordens abertas e sem equipe, que podem ser assumidas")
	PaginaResponse<OrdemServicoResumoResponse> listarDisponiveis(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return ordens.listarDisponiveis(usuario, Paginacao.de(pagina, tamanho, ORDENACAO));
	}

	@GetMapping("/minhas")
	@ApenasTecnicos
	@Operation(summary = "Ordens não concluídas da equipe do técnico")
	PaginaResponse<OrdemServicoResumoResponse> listarMinhas(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@RequestParam(required = false) StatusAtendimento statusAtendimento,
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamanho) {
		return ordens.listarDoTecnico(usuario, statusAtendimento, Paginacao.de(pagina, tamanho, ORDENACAO));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Detalhe da ordem com equipe e registros")
	OrdemServicoResponse detalhar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
		return ordens.detalhar(usuario, id);
	}

	@PostMapping("/{id}/assumir")
	@ApenasTecnicos
	@Operation(summary = "Técnico assume uma ordem aberta sem equipe")
	OrdemServicoResponse assumir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
		equipes.assumir(usuario, id);
		return ordens.detalhar(usuario, id);
	}

	@PutMapping("/{id}/tecnicos")
	@ApenasGestores
	@Operation(summary = "Define a equipe técnica da ordem")
	OrdemServicoResponse definirEquipe(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
			@Valid @RequestBody EquipeRequest requisicao) {
		equipes.definirEquipe(usuario, id, requisicao);
		return ordens.detalhar(usuario, id);
	}

	@PatchMapping("/{id}/atendimento")
	@Operation(summary = "Altera a situação do atendimento; retorna 204 quando a ordem sai do escopo do técnico")
	ResponseEntity<OrdemServicoResponse> alterarSituacao(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable Long id, @Valid @RequestBody SituacaoAtendimentoRequest requisicao) {
		atendimento.alterarSituacao(usuario, id, requisicao);
		return ordens.detalharSeVisivel(usuario, id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@GetMapping("/{id}/registros")
	@Operation(summary = "Registros de atividades e impedimentos")
	List<RegistroAtendimentoResponse> listarRegistros(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable Long id) {
		return atendimento.listarRegistros(usuario, id);
	}

	@PostMapping("/{id}/registros")
	@Operation(summary = "Registra atividade e materiais utilizados em texto livre")
	ResponseEntity<RegistroAtendimentoResponse> registrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
			@PathVariable Long id, @Valid @RequestBody RegistroAtendimentoRequest requisicao) {
		return ResponseEntity.status(HttpStatus.CREATED).body(atendimento.registrar(usuario, id, requisicao));
	}

	@GetMapping("/{id}/itens")
	@ApenasGestores
	@Operation(summary = "Itens precificados e totais da ordem")
	PrecificacaoResponse consultarItens(@PathVariable Long id) {
		return precificacao.consultar(id);
	}

	@PutMapping("/{id}/itens")
	@ApenasGestores
	@Operation(summary = "Substitui os itens de produtos, serviços e mão de obra da ordem concluída")
	PrecificacaoResponse definirItens(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
			@Valid @RequestBody ItensOrdemServicoRequest requisicao) {
		return precificacao.definirItens(usuario, id, requisicao);
	}

}
