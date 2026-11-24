package br.com.marwil.ordemservico.entity;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.enums.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ordens_servico_tecnicos")
@Getter
@NoArgsConstructor
public class OrdemServicoTecnico {

	@EmbeddedId
	private OrdemServicoTecnicoId id = new OrdemServicoTecnicoId();

	@MapsId("ordemServicoId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ordem_servico_id")
	private OrdemServico ordemServico;

	@MapsId("tecnicoId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tecnico_id")
	private Usuario tecnico;

	@Enumerated(EnumType.STRING)
	@Column(name = "tecnico_perfil", nullable = false, updatable = false, length = 20)
	private Perfil tecnicoPerfil = Perfil.TECNICO;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "atribuido_por", nullable = false, updatable = false)
	private Usuario atribuidoPor;

	@Column(name = "atribuido_em", nullable = false, updatable = false)
	private OffsetDateTime atribuidoEm;

	public OrdemServicoTecnico(OrdemServico ordemServico, Usuario tecnico, Usuario atribuidoPor,
			OffsetDateTime atribuidoEm) {
		this.id = new OrdemServicoTecnicoId(ordemServico.getId(), tecnico.getId());
		this.ordemServico = ordemServico;
		this.tecnico = tecnico;
		this.atribuidoPor = atribuidoPor;
		this.atribuidoEm = atribuidoEm;
	}

}
