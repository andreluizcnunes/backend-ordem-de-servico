package br.com.marwil.ordemservico.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record EquipeRequest(
		@NotNull(message = "Informe a lista de técnicos da equipe; envie uma lista vazia para remover todos.")
		List<@NotNull(message = "O identificador do técnico não pode ser nulo.") Long> tecnicosIds) {
}
