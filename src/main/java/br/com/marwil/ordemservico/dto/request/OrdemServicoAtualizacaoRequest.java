package br.com.marwil.ordemservico.dto.request;

import br.com.marwil.ordemservico.util.Textos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrdemServicoAtualizacaoRequest(
		@NotBlank(message = "Informe a descrição da ordem de serviço.")
		@Size(max = 10000, message = "A descrição deve ter no máximo {max} caracteres.")
		String descricao,

		@NotBlank(message = "Informe o endereço de atendimento.")
		@Size(max = 300, message = "O endereço de atendimento deve ter no máximo {max} caracteres.")
		String enderecoAtendimento,

		@Size(max = 150, message = "O contato de atendimento deve ter no máximo {max} caracteres.")
		String contatoAtendimento,

		@Size(max = 30, message = "O telefone de atendimento deve ter no máximo {max} caracteres.")
		String telefoneAtendimento,

		@Email(message = "Informe um e-mail de atendimento válido.")
		@Size(max = 254, message = "O e-mail de atendimento deve ter no máximo {max} caracteres.")
		String emailAtendimento) {

	public OrdemServicoAtualizacaoRequest {
		descricao = Textos.obrigatorio(descricao);
		enderecoAtendimento = Textos.obrigatorio(enderecoAtendimento);
		contatoAtendimento = Textos.opcional(contatoAtendimento);
		telefoneAtendimento = Textos.opcional(telefoneAtendimento);
		emailAtendimento = Textos.opcional(emailAtendimento);
	}

}
