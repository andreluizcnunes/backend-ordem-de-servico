package br.com.marwil.ordemservico.repository;

import java.time.LocalDate;
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

import br.com.marwil.ordemservico.entity.Cobranca;
import br.com.marwil.ordemservico.enums.StatusCobranca;
import br.com.marwil.ordemservico.repository.projection.PendenciaPorCliente;
import br.com.marwil.ordemservico.repository.projection.Totalizador;
import jakarta.persistence.LockModeType;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long>, JpaSpecificationExecutor<Cobranca> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Cobranca c where c.id = :id")
	Optional<Cobranca> findComBloqueioById(@Param("id") Long id);

	@Override
	@EntityGraph(attributePaths = { "ordemServico", "ordemServico.cliente", "enviadaPor", "pagamento",
			"pagamento.registradoPor" })
	Page<Cobranca> findAll(Specification<Cobranca> especificacao, Pageable paginacao);

	@Query("""
			select new br.com.marwil.ordemservico.repository.projection.Totalizador(sum(c.valorTotal), count(c))
			from Cobranca c
			where c.status = :status
			""")
	Totalizador totalizarPorStatus(@Param("status") StatusCobranca status);

	@Query("""
			select new br.com.marwil.ordemservico.repository.projection.Totalizador(sum(c.valorTotal), count(c))
			from Cobranca c
			where c.status = br.com.marwil.ordemservico.enums.StatusCobranca.PENDENTE
			  and c.dataVencimento < :hoje
			""")
	Totalizador totalizarVencidas(@Param("hoje") LocalDate hoje);

	@Query("""
			select new br.com.marwil.ordemservico.repository.projection.PendenciaPorCliente(
			    cl.id, cl.nome, sum(c.valorTotal), count(c),
			    max(case when c.dataVencimento < :hoje then 1 else 0 end))
			from Cobranca c
			join c.ordemServico o
			join o.cliente cl
			where c.status = br.com.marwil.ordemservico.enums.StatusCobranca.PENDENTE
			group by cl.id, cl.nome
			""")
	List<PendenciaPorCliente> totalizarPendenciasPorCliente(@Param("hoje") LocalDate hoje);

}
