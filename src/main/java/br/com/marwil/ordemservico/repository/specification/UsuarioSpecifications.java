package br.com.marwil.ordemservico.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.enums.Perfil;

public final class UsuarioSpecifications {

	private UsuarioSpecifications() {
	}

	public static Specification<Usuario> comPerfil(Perfil perfil) {
		return (raiz, consulta, criterio) -> perfil == null ? criterio.conjunction()
				: criterio.equal(raiz.get("perfil"), perfil);
	}

	public static Specification<Usuario> comSituacaoAtiva(Boolean ativo) {
		return (raiz, consulta, criterio) -> ativo == null ? criterio.conjunction()
				: criterio.equal(raiz.get("ativo"), ativo);
	}

}
