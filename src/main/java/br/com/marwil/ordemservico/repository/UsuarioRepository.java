package br.com.marwil.ordemservico.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.enums.Perfil;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	List<Usuario> findByIdInAndPerfilAndAtivoTrue(Collection<Long> ids, Perfil perfil);

}
