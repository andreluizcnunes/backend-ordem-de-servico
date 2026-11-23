package br.com.marwil.ordemservico.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ItensOrdemServicoRequest(@NotNull List<@NotNull @Valid ItemOrdemServicoRequest> itens) {
}
