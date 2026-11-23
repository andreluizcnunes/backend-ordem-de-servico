package br.com.marwil.ordemservico.repository.projection;

import br.com.marwil.ordemservico.enums.StatusCobranca;

public record StatusCobrancaDaOrdem(Long ordemServicoId, StatusCobranca status) {
}
