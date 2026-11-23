package br.com.marwil.ordemservico.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import br.com.marwil.ordemservico.enums.StatusAtendimento;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false, updatable = false)
	private Cliente cliente;

	@Column(name = "data_abertura", nullable = false, updatable = false)
	private OffsetDateTime dataAbertura;

	@Column(nullable = false, columnDefinition = "text")
	private String descricao;

	@Column(name = "endereco_atendimento", nullable = false, length = 300)
	private String enderecoAtendimento;

	@Column(name = "contato_atendimento", length = 150)
	private String contatoAtendimento;

	@Column(name = "telefone_atendimento", length = 30)
	private String telefoneAtendimento;

	@Column(name = "email_atendimento", length = 254)
	private String emailAtendimento;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_atendimento", nullable = false, length = 20)
	private StatusAtendimento statusAtendimento = StatusAtendimento.ABERTA;

	@Column(name = "motivo_aguardando", columnDefinition = "text")
	private String motivoAguardando;

	@Column(name = "data_inicio_atendimento")
	private OffsetDateTime dataInicioAtendimento;

	@Column(name = "data_conclusao")
	private OffsetDateTime dataConclusao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concluida_por")
	private Usuario concluidaPor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "criado_por", nullable = false, updatable = false)
	private Usuario criadoPor;

	@Generated(event = EventType.INSERT)
	@Column(name = "criado_em", insertable = false, updatable = false)
	private OffsetDateTime criadoEm;

	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "atualizado_em", insertable = false, updatable = false)
	private OffsetDateTime atualizadoEm;

	@OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("atribuidoEm ASC")
	private List<OrdemServicoTecnico> tecnicos = new ArrayList<>();

	@OneToMany(mappedBy = "ordemServico")
	@OrderBy("registradoEm ASC, id ASC")
	private List<RegistroAtendimento> registros = new ArrayList<>();

	@OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<ItemOrdemServico> itens = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public OffsetDateTime getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(OffsetDateTime dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getEnderecoAtendimento() {
		return enderecoAtendimento;
	}

	public void setEnderecoAtendimento(String enderecoAtendimento) {
		this.enderecoAtendimento = enderecoAtendimento;
	}

	public String getContatoAtendimento() {
		return contatoAtendimento;
	}

	public void setContatoAtendimento(String contatoAtendimento) {
		this.contatoAtendimento = contatoAtendimento;
	}

	public String getTelefoneAtendimento() {
		return telefoneAtendimento;
	}

	public void setTelefoneAtendimento(String telefoneAtendimento) {
		this.telefoneAtendimento = telefoneAtendimento;
	}

	public String getEmailAtendimento() {
		return emailAtendimento;
	}

	public void setEmailAtendimento(String emailAtendimento) {
		this.emailAtendimento = emailAtendimento;
	}

	public StatusAtendimento getStatusAtendimento() {
		return statusAtendimento;
	}

	public void setStatusAtendimento(StatusAtendimento statusAtendimento) {
		this.statusAtendimento = statusAtendimento;
	}

	public String getMotivoAguardando() {
		return motivoAguardando;
	}

	public void setMotivoAguardando(String motivoAguardando) {
		this.motivoAguardando = motivoAguardando;
	}

	public OffsetDateTime getDataInicioAtendimento() {
		return dataInicioAtendimento;
	}

	public void setDataInicioAtendimento(OffsetDateTime dataInicioAtendimento) {
		this.dataInicioAtendimento = dataInicioAtendimento;
	}

	public OffsetDateTime getDataConclusao() {
		return dataConclusao;
	}

	public void setDataConclusao(OffsetDateTime dataConclusao) {
		this.dataConclusao = dataConclusao;
	}

	public Usuario getConcluidaPor() {
		return concluidaPor;
	}

	public void setConcluidaPor(Usuario concluidaPor) {
		this.concluidaPor = concluidaPor;
	}

	public Usuario getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(Usuario criadoPor) {
		this.criadoPor = criadoPor;
	}

	public OffsetDateTime getCriadoEm() {
		return criadoEm;
	}

	public OffsetDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public List<OrdemServicoTecnico> getTecnicos() {
		return tecnicos;
	}

	public List<RegistroAtendimento> getRegistros() {
		return registros;
	}

	public List<ItemOrdemServico> getItens() {
		return itens;
	}

}
