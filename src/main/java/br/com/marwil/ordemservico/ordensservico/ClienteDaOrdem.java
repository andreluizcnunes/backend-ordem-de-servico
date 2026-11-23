package br.com.marwil.ordemservico.ordensservico;

public record ClienteDaOrdem(long id, String nome, String contato, String telefone, String email, String endereco) {
}
