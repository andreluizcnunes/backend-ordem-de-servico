package br.com.marwil.ordemservico.exception;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException(String mensagem) {
		super(mensagem);
	}

}
