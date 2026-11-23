package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrdemServicoAtualizacaoRequest(
		@NotBlank @Size(max = 10000) String descricao,
		@NotBlank @Size(max = 300) String enderecoAtendimento,
		@Size(max = 150) String contatoAtendimento,
		@Size(max = 30) String telefoneAtendimento,
		@Email @Size(max = 254) String emailAtendimento) {

	public OrdemServicoAtualizacaoRequest {
		descricao = Textos.obrigatorio(descricao);
		enderecoAtendimento = Textos.obrigatorio(enderecoAtendimento);
		contatoAtendimento = Textos.opcional(contatoAtendimento);
		telefoneAtendimento = Textos.opcional(telefoneAtendimento);
		emailAtendimento = Textos.opcional(emailAtendimento);
	}

}
