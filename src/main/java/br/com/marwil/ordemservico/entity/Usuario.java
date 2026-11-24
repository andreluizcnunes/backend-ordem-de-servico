package br.com.marwil.ordemservico.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import br.com.marwil.ordemservico.enums.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Perfil perfil;

	@Column(nullable = false)
	private boolean ativo = true;

	@Setter(AccessLevel.NONE)
	@Generated(event = EventType.INSERT)
	@Column(name = "criado_em", insertable = false, updatable = false)
	private OffsetDateTime criadoEm;

	@Setter(AccessLevel.NONE)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "atualizado_em", insertable = false, updatable = false)
	private OffsetDateTime atualizadoEm;

}
