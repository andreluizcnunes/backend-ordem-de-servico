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

@Entity
@Table(name = "registros_atendimento")
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

	public Usuario getAutor() {
		return autor;
	}

	public void setAutor(Usuario autor) {
		this.autor = autor;
	}

	public TipoRegistro getTipo() {
		return tipo;
	}

	public void setTipo(TipoRegistro tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getMateriaisUtilizados() {
		return materiaisUtilizados;
	}

	public void setMateriaisUtilizados(String materiaisUtilizados) {
		this.materiaisUtilizados = materiaisUtilizados;
	}

	public OffsetDateTime getRegistradoEm() {
		return registradoEm;
	}

	public void setRegistradoEm(OffsetDateTime registradoEm) {
		this.registradoEm = registradoEm;
	}

}
