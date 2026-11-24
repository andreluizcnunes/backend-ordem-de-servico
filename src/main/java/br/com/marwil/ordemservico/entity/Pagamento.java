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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
public class Pagamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cobranca_id", nullable = false, updatable = false)
	private Cobranca cobranca;

	@Setter(AccessLevel.NONE)
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

}
