package br.com.marwil.ordemservico.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.marwil.ordemservico.entity.LancamentoCaixa;
import br.com.marwil.ordemservico.repository.projection.RecebimentoPorCliente;
import br.com.marwil.ordemservico.repository.projection.Totalizador;

public interface LancamentoCaixaRepository extends JpaRepository<LancamentoCaixa, Long> {

	@EntityGraph(attributePaths = { "pagamento", "pagamento.cobranca", "pagamento.cobranca.ordemServico",
			"pagamento.cobranca.ordemServico.cliente" })
	Page<LancamentoCaixa> findByDataLancamentoBetween(LocalDate inicio, LocalDate fim, Pageable paginacao);

	@Query("""
			select new br.com.marwil.ordemservico.repository.projection.Totalizador(sum(l.valor), count(l))
			from LancamentoCaixa l
			where l.dataLancamento between :inicio and :fim
			""")
	Totalizador totalizarPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	@Query("""
			select new br.com.marwil.ordemservico.repository.projection.RecebimentoPorCliente(
			    cl.id, cl.nome, sum(l.valor))
			from LancamentoCaixa l
			join l.pagamento p
			join p.cobranca c
			join c.ordemServico o
			join o.cliente cl
			where l.dataLancamento between :inicio and :fim
			group by cl.id, cl.nome
			""")
	List<RecebimentoPorCliente> totalizarPorCliente(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

}
