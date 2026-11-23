package br.com.marwil.ordemservico.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

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
@Table(name = "pagamentos")
public class Pagamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cobranca_id", nullable = false, updatable = false)
	private Cobranca cobranca;

	@Enumerated(EnumType.STRING)
	@Column(name = "cobranca_status", nullable = false, updatable = false, length = 20)
	private StatusCobranca cobrancaStatus = StatusCobranca.PAGA;

	@Column(name = "valor_recebido", nullable = false, updatable = false, precision = 14, scale = 2)
	private BigDecimal valorRecebido;

	@Column(name = "data_pagamento", nullable = false, updatable = false)
	private LocalDate dataPagamento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "registrado_por", nullable = false, updatable = false)
	private Usuario registradoPor;

	@Column(name = "registrado_em", nullable = false, updatable = false)
	private OffsetDateTime registradoEm;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Cobranca getCobranca() {
		return cobranca;
	}

	public void setCobranca(Cobranca cobranca) {
		this.cobranca = cobranca;
	}

	public StatusCobranca getCobrancaStatus() {
		return cobrancaStatus;
	}

	public BigDecimal getValorRecebido() {
		return valorRecebido;
	}

	public void setValorRecebido(BigDecimal valorRecebido) {
		this.valorRecebido = valorRecebido;
	}

	public LocalDate getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(LocalDate dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public Usuario getRegistradoPor() {
		return registradoPor;
	}

	public void setRegistradoPor(Usuario registradoPor) {
		this.registradoPor = registradoPor;
	}

	public OffsetDateTime getRegistradoEm() {
		return registradoEm;
	}

	public void setRegistradoEm(OffsetDateTime registradoEm) {
		this.registradoEm = registradoEm;
	}

}
