package br.com.marwil.ordemservico.repository.projection;

import java.math.BigDecimal;

public record RecebimentoPorCliente(Long clienteId, String nome, BigDecimal total) {
}
