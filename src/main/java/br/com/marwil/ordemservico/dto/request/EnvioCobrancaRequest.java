package br.com.marwil.ordemservico.dto.request;

import java.time.LocalDate;

public record EnvioCobrancaRequest(LocalDate dataVencimento) {
}
