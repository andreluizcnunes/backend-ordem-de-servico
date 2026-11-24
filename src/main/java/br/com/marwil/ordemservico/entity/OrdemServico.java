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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ordens_servico")
@Getter
@Setter
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

	@Setter(AccessLevel.NONE)
	@Generated(event = EventType.INSERT)
	@Column(name = "criado_em", insertable = false, updatable = false)
	private OffsetDateTime criadoEm;

	@Setter(AccessLevel.NONE)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "atualizado_em", insertable = false, updatable = false)
	private OffsetDateTime atualizadoEm;

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("atribuidoEm ASC")
	private List<OrdemServicoTecnico> tecnicos = new ArrayList<>();

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "ordemServico")
	@OrderBy("registradoEm ASC, id ASC")
	private List<RegistroAtendimento> registros = new ArrayList<>();

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<ItemOrdemServico> itens = new ArrayList<>();

}
