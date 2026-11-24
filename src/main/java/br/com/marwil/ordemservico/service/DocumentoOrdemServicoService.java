package br.com.marwil.ordemservico.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.response.OrdemServicoResponse;
import br.com.marwil.ordemservico.dto.response.PrecificacaoResponse;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;
import br.com.marwil.ordemservico.service.pdf.ConteudoDocumento;
import br.com.marwil.ordemservico.service.pdf.GeradorPdfOrdemServico;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoOrdemServicoService {

	private final OrdemServicoService ordens;
	private final PrecificacaoService precificacao;
	private final GeradorPdfOrdemServico gerador;

	@Value("${app.empresa.nome}")
	private final String empresa;

	@Transactional(readOnly = true)
	public byte[] gerarPdf(UsuarioAutenticado usuario, Long ordemServicoId) {
		OrdemServicoResponse ordem = ordens.detalhar(usuario, ordemServicoId);
		if (!usuario.ehGestor()) {
			return gerador.gerar(new ConteudoDocumento(empresa, ordem, List.of(), false, null));
		}
		PrecificacaoResponse precos = precificacao.consultar(ordemServicoId);
		boolean possuiItens = !precos.itens().isEmpty();
		return gerador.gerar(new ConteudoDocumento(empresa, ordem, precos.itens(), possuiItens,
				possuiItens ? precos.total() : null));
	}

}
