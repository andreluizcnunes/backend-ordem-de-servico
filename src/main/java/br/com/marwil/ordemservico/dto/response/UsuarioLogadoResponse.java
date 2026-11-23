package br.com.marwil.ordemservico.dto.response;

import br.com.marwil.ordemservico.enums.Perfil;

public record UsuarioLogadoResponse(Long id, String nome, Perfil perfil) {
}
