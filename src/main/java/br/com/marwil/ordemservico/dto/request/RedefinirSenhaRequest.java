package br.com.marwil.ordemservico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(@NotBlank @Size(min = 8, max = 72) String novaSenha) {
}
