package br.com.marwil.ordemservico.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.enums.StatusCobranca;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cobrancas")
@Getter
@Setter
public class Cobranca {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ordem_servico_id", nullable = false, updatable = false)
	private OrdemServico ordemServico;

	@Setter(AccessLevel.NONE)
	@Enumerated(EnumType.STRING)
	@Column(name = "ordem_servico_status", nullable = false, updatable = false, length = 20)
	private StatusAtendimento ordemServicoStatus = StatusAtendimento.CONCLUIDA;

	@Column(name = "valor_total", nullable = false, updatable = false, precision = 14, scale = 2)
	private BigDecimal valorTotal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusCobranca status = StatusCobranca.PENDENTE;

	@Column(name = "data_vencimento")
	private LocalDate dataVencimento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "enviada_por", nullable = false, updatable = false)
	private Usuario enviadaPor;

	@Column(name = "enviada_em", nullable = false, updatable = false)
	private OffsetDateTime enviadaEm;

	@Setter(AccessLevel.NONE)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "atualizado_em", insertable = false, updatable = false)
	private OffsetDateTime atualizadoEm;

	@OneToOne(mappedBy = "cobranca", fetch = FetchType.LAZY)
	private Pagamento pagamento;

	public boolean estaVencida(LocalDate hoje) {
		return status == StatusCobranca.PENDENTE && dataVencimento != null && dataVencimento.isBefore(hoje);
	}

}
