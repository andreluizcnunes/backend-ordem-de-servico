package br.com.marwil.ordemservico.repository.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import br.com.marwil.ordemservico.entity.Cobranca;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import jakarta.persistence.criteria.Predicate;

public final class CobrancaSpecifications {

	private CobrancaSpecifications() {
	}

	public static Specification<Cobranca> comStatus(StatusCobranca status) {
		return (raiz, consulta, criterio) -> status == null ? criterio.conjunction()
				: criterio.equal(raiz.get("status"), status);
	}

	public static Specification<Cobranca> doCliente(Long clienteId) {
		return (raiz, consulta, criterio) -> clienteId == null ? criterio.conjunction()
				: criterio.equal(raiz.get("ordemServico").get("cliente").get("id"), clienteId);
	}

	public static Specification<Cobranca> daOrdem(Long ordemServicoId) {
		return (raiz, consulta, criterio) -> ordemServicoId == null ? criterio.conjunction()
				: criterio.equal(raiz.get("ordemServico").get("id"), ordemServicoId);
	}

	public static Specification<Cobranca> vencidas(Boolean vencidas, LocalDate hoje) {
		return (raiz, consulta, criterio) -> {
			if (vencidas == null) {
				return criterio.conjunction();
			}
			Predicate vencida = criterio.and(
					criterio.equal(raiz.get("status"), StatusCobranca.PENDENTE),
					criterio.isNotNull(raiz.get("dataVencimento")),
					criterio.lessThan(raiz.get("dataVencimento"), hoje));
			return vencidas ? vencida : criterio.not(vencida);
		};
	}

}
