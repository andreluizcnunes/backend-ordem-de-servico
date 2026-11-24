package br.com.marwil.ordemservico.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ItensOrdemServicoRequest(
		@NotNull(message = "Informe a lista de itens; envie uma lista vazia para remover todos.")
		List<@NotNull(message = "O item não pode ser nulo.") @Valid ItemOrdemServicoRequest> itens) {
}
