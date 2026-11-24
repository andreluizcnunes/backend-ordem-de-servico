package br.com.marwil.ordemservico.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import br.com.marwil.ordemservico.enums.TipoItem;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "itens_ordem_servico")
@Getter
@Setter
public class ItemOrdemServico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ordem_servico_id", nullable = false, updatable = false)
	private OrdemServico ordemServico;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoItem tipo;

	@Column(nullable = false, length = 500)
	private String descricao;

	@Column(name = "local_realizacao", length = 300)
	private String localRealizacao;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantidade;

	@Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
	private BigDecimal precoUnitario;

	@Setter(AccessLevel.NONE)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "valor_total", insertable = false, updatable = false, precision = 14, scale = 2)
	private BigDecimal valorTotal;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "criado_por", nullable = false, updatable = false)
	private Usuario criadoPor;

	@Setter(AccessLevel.NONE)
	@Generated(event = EventType.INSERT)
	@Column(name = "criado_em", insertable = false, updatable = false)
	private OffsetDateTime criadoEm;

	@Setter(AccessLevel.NONE)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "atualizado_em", insertable = false, updatable = false)
	private OffsetDateTime atualizadoEm;

}
