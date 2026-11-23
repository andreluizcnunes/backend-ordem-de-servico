package br.com.marwil.ordemservico.comum;

public class RequisicaoInvalidaException extends RuntimeException {

	public RequisicaoInvalidaException(String mensagem) {
		super(mensagem);
	}

}
