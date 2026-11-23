package br.com.marwil.ordemservico.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.marwil.ordemservico.entity.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
