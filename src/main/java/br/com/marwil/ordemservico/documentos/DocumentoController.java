package br.com.marwil.ordemservico.documentos;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.com.marwil.ordemservico.security.UsuarioAutenticado;

@RestController
public class DocumentoController {

	private final DocumentoOrdemServicoService documentos;

	public DocumentoController(DocumentoOrdemServicoService documentos) {
		this.documentos = documentos;
	}

	@GetMapping("/api/v1/ordens-servico/{id}/pdf")
	ResponseEntity<byte[]> gerarPdf(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
		byte[] pdf = documentos.gerarPdf(usuario, id);
		ContentDisposition disposicao = ContentDisposition.inline().filename("ordem-servico-" + id + ".pdf").build();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
				.body(pdf);
	}

}
