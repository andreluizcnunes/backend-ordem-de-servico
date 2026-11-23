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

@Entity
@Table(name = "cobrancas")
public class Cobranca {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ordem_servico_id", nullable = false, updatable = false)
	private OrdemServico ordemServico;

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

	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "atualizado_em", insertable = false, updatable = false)
	private OffsetDateTime atualizadoEm;

	@OneToOne(mappedBy = "cobranca", fetch = FetchType.LAZY)
	private Pagamento pagamento;

	public boolean estaVencida(LocalDate hoje) {
		return status == StatusCobranca.PENDENTE && dataVencimento != null && dataVencimento.isBefore(hoje);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrdemServico getOrdemServico() {
		return ordemServico;
	}

	public void setOrdemServico(OrdemServico ordemServico) {
		this.ordemServico = ordemServico;
	}

	public StatusAtendimento getOrdemServicoStatus() {
		return ordemServicoStatus;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public StatusCobranca getStatus() {
		return status;
	}

	public void setStatus(StatusCobranca status) {
		this.status = status;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public Usuario getEnviadaPor() {
		return enviadaPor;
	}

	public void setEnviadaPor(Usuario enviadaPor) {
		this.enviadaPor = enviadaPor;
	}

	public OffsetDateTime getEnviadaEm() {
		return enviadaEm;
	}

	public void setEnviadaEm(OffsetDateTime enviadaEm) {
		this.enviadaEm = enviadaEm;
	}

	public OffsetDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public Pagamento getPagamento() {
		return pagamento;
	}

	public void setPagamento(Pagamento pagamento) {
		this.pagamento = pagamento;
	}

}
