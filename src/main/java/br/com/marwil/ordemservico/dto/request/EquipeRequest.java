package br.com.marwil.ordemservico.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record EquipeRequest(@NotNull List<@NotNull Long> tecnicosIds) {
}
