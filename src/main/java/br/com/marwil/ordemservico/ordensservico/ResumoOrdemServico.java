package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record ResumoOrdemServico(
		long id,
		UsuarioResumido cliente,
		OffsetDateTime dataAbertura,
		String descricao,
		String enderecoAtendimento,
		StatusAtendimento statusAtendimento,
		OffsetDateTime dataConclusao,
		@JsonInclude(JsonInclude.Include.NON_NULL) StatusCobranca statusCobranca,
		List<UsuarioResumido> tecnicos) {

	public ResumoOrdemServico comTecnicos(List<UsuarioResumido> equipe) {
		return new ResumoOrdemServico(id, cliente, dataAbertura, descricao, enderecoAtendimento, statusAtendimento,
				dataConclusao, statusCobranca, equipe);
	}

	public ResumoOrdemServico semDadosFinanceiros() {
		return new ResumoOrdemServico(id, cliente, dataAbertura, descricao, enderecoAtendimento, statusAtendimento,
				dataConclusao, null, tecnicos);
	}

}
