package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record OrdemServicoDetalhe(
		long id,
		ClienteDaOrdem cliente,
		OffsetDateTime dataAbertura,
		String descricao,
		String enderecoAtendimento,
		String contatoAtendimento,
		String telefoneAtendimento,
		String emailAtendimento,
		StatusAtendimento statusAtendimento,
		String motivoAguardando,
		OffsetDateTime dataInicioAtendimento,
		OffsetDateTime dataConclusao,
		UsuarioResumido concluidaPor,
		@JsonInclude(JsonInclude.Include.NON_NULL) StatusCobranca statusCobranca,
		UsuarioResumido criadoPor,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm,
		List<TecnicoAtribuido> tecnicos,
		List<RegistroAtendimento> registros) {

	public OrdemServicoDetalhe comEquipeERegistros(List<TecnicoAtribuido> equipe, List<RegistroAtendimento> historico) {
		return new OrdemServicoDetalhe(id, cliente, dataAbertura, descricao, enderecoAtendimento, contatoAtendimento,
				telefoneAtendimento, emailAtendimento, statusAtendimento, motivoAguardando, dataInicioAtendimento,
				dataConclusao, concluidaPor, statusCobranca, criadoPor, criadoEm, atualizadoEm, equipe, historico);
	}

	public OrdemServicoDetalhe semDadosFinanceiros() {
		return new OrdemServicoDetalhe(id, cliente, dataAbertura, descricao, enderecoAtendimento, contatoAtendimento,
				telefoneAtendimento, emailAtendimento, statusAtendimento, motivoAguardando, dataInicioAtendimento,
				dataConclusao, concluidaPor, null, criadoPor, criadoEm, atualizadoEm, tecnicos, registros);
	}

}
