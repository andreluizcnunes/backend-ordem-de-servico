package br.com.marwil.ordemservico.dto.response;

import java.time.Instant;

public record LoginResponse(String token, String tipo, Instant expiraEm, UsuarioLogadoResponse usuario) {
}
