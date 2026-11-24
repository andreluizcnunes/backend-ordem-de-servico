package br.com.marwil.ordemservico.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdemServicoTecnicoId implements Serializable {

	@Column(name = "ordem_servico_id")
	private Long ordemServicoId;

	@Column(name = "tecnico_id")
	private Long tecnicoId;

}
