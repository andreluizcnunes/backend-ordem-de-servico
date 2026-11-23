package br.com.marwil.ordemservico.ordensservico;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.comum.UsuarioResumido;

public record TecnicoAtribuido(long id, String nome, OffsetDateTime atribuidoEm, UsuarioResumido atribuidoPor) {
}
