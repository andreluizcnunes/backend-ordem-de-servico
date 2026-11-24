package br.com.marwil.ordemservico.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.DocumentoOrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Documentos")
@RequiredArgsConstructor
public class DocumentoController {

	private final DocumentoOrdemServicoService documentos;

	@GetMapping(value = "/api/v1/ordens-servico/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	@Operation(summary = "PDF A4 da ordem de serviço; a versão do técnico não traz valores")
	ResponseEntity<byte[]> gerarPdf(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
		byte[] pdf = documentos.gerarPdf(usuario, id);
		ContentDisposition disposicao = ContentDisposition.inline().filename("ordem-servico-" + id + ".pdf").build();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
				.body(pdf);
	}

}
