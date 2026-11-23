package br.com.marwil.ordemservico.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OrdemServicoTecnicoId implements Serializable {

	@Column(name = "ordem_servico_id")
	private Long ordemServicoId;

	@Column(name = "tecnico_id")
	private Long tecnicoId;

	public OrdemServicoTecnicoId() {
	}

	public OrdemServicoTecnicoId(Long ordemServicoId, Long tecnicoId) {
		this.ordemServicoId = ordemServicoId;
		this.tecnicoId = tecnicoId;
	}

	public Long getOrdemServicoId() {
		return ordemServicoId;
	}

	public Long getTecnicoId() {
		return tecnicoId;
	}

	@Override
	public boolean equals(Object outro) {
		return outro instanceof OrdemServicoTecnicoId chave
				&& Objects.equals(ordemServicoId, chave.ordemServicoId)
				&& Objects.equals(tecnicoId, chave.tecnicoId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ordemServicoId, tecnicoId);
	}

}
