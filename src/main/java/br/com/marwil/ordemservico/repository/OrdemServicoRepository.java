package br.com.marwil.ordemservico.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.repository.projection.StatusCobrancaDaOrdem;
import jakarta.persistence.LockModeType;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long>,
		JpaSpecificationExecutor<OrdemServico> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select o from OrdemServico o where o.id = :id")
	Optional<OrdemServico> findComBloqueioById(@Param("id") Long id);

	@Override
	@EntityGraph(attributePaths = "cliente")
	Page<OrdemServico> findAll(Specification<OrdemServico> especificacao, Pageable paginacao);

	@Query("select c.status from Cobranca c where c.ordemServico.id = :ordemServicoId")
	Optional<StatusCobranca> findStatusCobranca(@Param("ordemServicoId") Long ordemServicoId);

	@Query("""
			select new br.com.marwil.ordemservico.repository.projection.StatusCobrancaDaOrdem(c.ordemServico.id, c.status)
			from Cobranca c
			where c.ordemServico.id in :ordensIds
			""")
	List<StatusCobrancaDaOrdem> findStatusCobrancaPorOrdens(@Param("ordensIds") Collection<Long> ordensIds);

}
