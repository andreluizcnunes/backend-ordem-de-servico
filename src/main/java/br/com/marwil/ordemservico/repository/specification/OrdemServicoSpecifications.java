package br.com.marwil.ordemservico.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.marwil.ordemservico.entity.Cobranca;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.entity.OrdemServicoTecnico;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class OrdemServicoSpecifications {

	private OrdemServicoSpecifications() {
	}

	public static Specification<OrdemServico> comId(Long id) {
		return (raiz, consulta, criterio) -> criterio.equal(raiz.get("id"), id);
	}

	public static Specification<OrdemServico> comStatus(StatusAtendimento status) {
		return (raiz, consulta, criterio) -> status == null ? criterio.conjunction()
				: criterio.equal(raiz.get("statusAtendimento"), status);
	}

	public static Specification<OrdemServico> doCliente(Long clienteId) {
		return (raiz, consulta, criterio) -> clienteId == null ? criterio.conjunction()
				: criterio.equal(raiz.get("cliente").get("id"), clienteId);
	}

	public static Specification<OrdemServico> comTecnico(Long tecnicoId) {
		return (raiz, consulta, criterio) -> tecnicoId == null ? criterio.conjunction()
				: criterio.exists(equipe(raiz, consulta, criterio, tecnicoId));
	}

	public static Specification<OrdemServico> comStatusCobranca(StatusCobranca status) {
		return (raiz, consulta, criterio) -> {
			if (status == null) {
				return criterio.conjunction();
			}
			Subquery<Long> cobranca = consulta.subquery(Long.class);
			Root<Cobranca> raizCobranca = cobranca.from(Cobranca.class);
			cobranca.select(raizCobranca.get("id"));
			Predicate daOrdem = criterio.equal(raizCobranca.get("ordemServico").get("id"), raiz.get("id"));
			if (status == StatusCobranca.NAO_ENVIADA) {
				return criterio.not(criterio.exists(cobranca.where(daOrdem)));
			}
			return criterio.exists(cobranca.where(daOrdem, criterio.equal(raizCobranca.get("status"), status)));
		};
	}

	public static Specification<OrdemServico> comTermo(String termo) {
		return (raiz, consulta, criterio) -> {
			if (termo == null) {
				return criterio.conjunction();
			}
			String padrao = Busca.contem(termo);
			Predicate porTexto = criterio.or(
					criterio.like(criterio.lower(raiz.get("descricao")), padrao, Busca.ESCAPE),
					criterio.like(criterio.lower(raiz.get("cliente").get("nome")), padrao, Busca.ESCAPE));
			Long numero = comoNumero(termo);
			return numero == null ? porTexto : criterio.or(porTexto, criterio.equal(raiz.get("id"), numero));
		};
	}

	public static Specification<OrdemServico> disponivel() {
		return (raiz, consulta, criterio) -> criterio.and(
				criterio.equal(raiz.get("statusAtendimento"), StatusAtendimento.ABERTA),
				criterio.not(criterio.exists(equipe(raiz, consulta, criterio, null))));
	}

	public static Specification<OrdemServico> doTecnico(Long tecnicoId) {
		return (raiz, consulta, criterio) -> criterio.and(
				criterio.notEqual(raiz.get("statusAtendimento"), StatusAtendimento.CONCLUIDA),
				criterio.exists(equipe(raiz, consulta, criterio, tecnicoId)));
	}

	public static Specification<OrdemServico> visivelAoTecnico(Long tecnicoId) {
		return disponivel().or(doTecnico(tecnicoId));
	}

	private static Subquery<Long> equipe(Root<OrdemServico> raiz, CriteriaQuery<?> consulta, CriteriaBuilder criterio,
			Long tecnicoId) {
		Subquery<Long> equipe = consulta.subquery(Long.class);
		Root<OrdemServicoTecnico> atribuicao = equipe.from(OrdemServicoTecnico.class);
		Predicate daOrdem = criterio.equal(atribuicao.get("id").get("ordemServicoId"), raiz.get("id"));
		equipe.select(atribuicao.get("id").get("tecnicoId"));
		return tecnicoId == null ? equipe.where(daOrdem)
				: equipe.where(daOrdem, criterio.equal(atribuicao.get("id").get("tecnicoId"), tecnicoId));
	}

	private static Long comoNumero(String termo) {
		try {
			return Long.valueOf(termo);
		} catch (NumberFormatException excecao) {
			return null;
		}
	}

}
