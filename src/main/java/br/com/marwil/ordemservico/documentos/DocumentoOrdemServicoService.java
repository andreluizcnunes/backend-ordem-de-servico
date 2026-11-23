package br.com.marwil.ordemservico.documentos;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.ordensservico.OrdemServicoDetalhe;
import br.com.marwil.ordemservico.ordensservico.OrdemServicoService;
import br.com.marwil.ordemservico.ordensservico.Precificacao;
import br.com.marwil.ordemservico.ordensservico.PrecificacaoService;

@Service
public class DocumentoOrdemServicoService {

	private final OrdemServicoService ordens;
	private final PrecificacaoService precificacao;
	private final GeradorPdfOrdemServico gerador;
	private final String empresa;

	public DocumentoOrdemServicoService(OrdemServicoService ordens, PrecificacaoService precificacao,
			GeradorPdfOrdemServico gerador, @Value("${app.empresa.nome}") String empresa) {
		this.ordens = ordens;
		this.precificacao = precificacao;
		this.gerador = gerador;
		this.empresa = empresa;
	}

	@Transactional(readOnly = true)
	public byte[] gerarPdf(UsuarioAutenticado usuario, long ordemServicoId) {
		OrdemServicoDetalhe ordem = ordens.detalhar(usuario, ordemServicoId);
		if (!usuario.ehGestor()) {
			return gerador.gerar(new ConteudoDocumento(empresa, ordem, List.of(), false, null));
		}
		Precificacao precos = precificacao.consultar(ordemServicoId);
		boolean possuiItens = !precos.itens().isEmpty();
		return gerador.gerar(new ConteudoDocumento(empresa, ordem, precos.itens(), possuiItens,
				possuiItens ? precos.total() : null));
	}

}
