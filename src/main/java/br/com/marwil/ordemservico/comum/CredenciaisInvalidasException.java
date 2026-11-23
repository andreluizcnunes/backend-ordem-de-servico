package br.com.marwil.ordemservico.comum;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException(String mensagem) {
		super(mensagem);
	}

}
