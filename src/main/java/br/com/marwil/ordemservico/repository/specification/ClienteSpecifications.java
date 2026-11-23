package br.com.marwil.ordemservico.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.marwil.ordemservico.entity.Cliente;

public final class ClienteSpecifications {

	private ClienteSpecifications() {
	}

	public static Specification<Cliente> comTermo(String termo) {
		return (raiz, consulta, criterio) -> {
			if (termo == null) {
				return criterio.conjunction();
			}
			String padrao = Busca.contem(termo);
			return criterio.or(
					criterio.like(criterio.lower(raiz.get("nome")), padrao, Busca.ESCAPE),
					criterio.like(criterio.lower(raiz.get("contato")), padrao, Busca.ESCAPE),
					criterio.like(criterio.lower(raiz.get("telefone")), padrao, Busca.ESCAPE),
					criterio.like(criterio.lower(raiz.get("email")), padrao, Busca.ESCAPE));
		};
	}

}
