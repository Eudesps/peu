package br.com.organizacaoacademica.tarefa.entity;

import br.com.organizacaoacademica.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/** Convite autenticado, vinculado a uma tarefa compartilhada e a uma conta existente. */
@Entity
@Table(
		name = "convite_tarefa",
		indexes = @Index(name = "idx_convite_tarefa_usuario_criado", columnList = "usuario_id, criado_em")
)
public class ConviteTarefa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "tarefa_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_convite_tarefa_tarefa")
	)
	private Tarefa tarefa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "usuario_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_convite_tarefa_usuario")
	)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 12)
	private EstadoConvite estado;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	@Column(name = "respondido_em")
	private Instant respondidoEm;

	protected ConviteTarefa() {
		// Required by JPA.
	}

	public ConviteTarefa(Tarefa tarefa, Usuario usuario) {
		this.tarefa = Objects.requireNonNull(tarefa, "A tarefa e obrigatoria.");
		this.usuario = Objects.requireNonNull(usuario, "O usuario convidado e obrigatorio.");
		if (tarefa.getTipo() != TipoTarefa.COMPARTILHADA) {
			throw new IllegalArgumentException("Somente tarefas compartilhadas aceitam convites.");
		}
		if (tarefa.getCriador() == null || tarefa.getCriador().getId() == null
				|| usuario.getId() == null || Objects.equals(tarefa.getCriador().getId(), usuario.getId())) {
			throw new IllegalArgumentException("O criador nao pode convidar a si mesmo.");
		}
		this.estado = EstadoConvite.PENDENTE;
	}

	public Long getId() {
		return id;
	}

	public Tarefa getTarefa() {
		return tarefa;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public EstadoConvite getEstado() {
		return estado;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public Instant getRespondidoEm() {
		return respondidoEm;
	}

	public void responder(EstadoConvite resposta) {
		if (estado != EstadoConvite.PENDENTE) {
			throw new IllegalStateException("Somente convites pendentes podem receber resposta.");
		}
		if (resposta != EstadoConvite.ACEITO && resposta != EstadoConvite.RECUSADO) {
			throw new IllegalArgumentException("A resposta deve aceitar ou recusar o convite.");
		}
		this.estado = resposta;
		this.respondidoEm = Instant.now();
	}

	public void cancelar() {
		if (estado != EstadoConvite.PENDENTE) {
			throw new IllegalStateException("Somente convites pendentes podem ser cancelados.");
		}
		this.estado = EstadoConvite.CANCELADO;
		this.respondidoEm = Instant.now();
	}

	@PrePersist
	void prepararParaPersistencia() {
		if (estado == null) {
			estado = EstadoConvite.PENDENTE;
		}
		if (criadoEm == null) {
			criadoEm = Instant.now();
		}
	}
}
