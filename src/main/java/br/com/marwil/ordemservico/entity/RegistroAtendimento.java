package br.com.marwil.ordemservico.entity;

import java.time.OffsetDateTime;

import br.com.marwil.ordemservico.enums.TipoRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registros_atendimento")
@Getter
@Setter
public class RegistroAtendimento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ordem_servico_id", nullable = false, updatable = false)
	private OrdemServico ordemServico;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "autor_id", nullable = false, updatable = false)
	private Usuario autor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoRegistro tipo = TipoRegistro.ATIVIDADE;

	@Column(nullable = false, columnDefinition = "text")
	private String descricao;

	@Column(name = "materiais_utilizados", columnDefinition = "text")
	private String materiaisUtilizados;

	@Column(name = "registrado_em", nullable = false, updatable = false)
	private OffsetDateTime registradoEm;

}
