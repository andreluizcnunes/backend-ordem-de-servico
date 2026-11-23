package br.com.marwil.ordemservico.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.marwil.ordemservico.entity.OrdemServicoTecnico;
import br.com.marwil.ordemservico.entity.OrdemServicoTecnicoId;

public interface OrdemServicoTecnicoRepository extends JpaRepository<OrdemServicoTecnico, OrdemServicoTecnicoId> {

	boolean existsByTecnicoId(Long tecnicoId);

	boolean existsByOrdemServicoId(Long ordemServicoId);

}
