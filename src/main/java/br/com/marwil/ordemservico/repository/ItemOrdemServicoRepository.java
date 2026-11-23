package br.com.marwil.ordemservico.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.marwil.ordemservico.entity.ItemOrdemServico;

public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Long> {

	boolean existsByOrdemServicoId(Long ordemServicoId);

	@Query("select coalesce(sum(i.valorTotal), 0) from ItemOrdemServico i where i.ordemServico.id = :ordemServicoId")
	BigDecimal somarValorTotal(@Param("ordemServicoId") Long ordemServicoId);

}
