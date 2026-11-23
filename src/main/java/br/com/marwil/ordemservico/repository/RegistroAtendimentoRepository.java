package br.com.marwil.ordemservico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.marwil.ordemservico.entity.RegistroAtendimento;

public interface RegistroAtendimentoRepository extends JpaRepository<RegistroAtendimento, Long> {

	@EntityGraph(attributePaths = "autor")
	List<RegistroAtendimento> findByOrdemServicoIdOrderByRegistradoEmAscIdAsc(Long ordemServicoId);

}
