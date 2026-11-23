package br.com.organizacaoacademica.tarefa.entity;

import br.com.organizacaoacademica.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/** Vínculo ativo entre uma tarefa compartilhada e uma pessoa que a integra. */
@Entity
@Table(
		name = "participante_tarefa",
		indexes = @Index(name = "idx_participante_tarefa_usuario", columnList = "usuario_id")
)
public class ParticipanteTarefa {

	@EmbeddedId
	private ParticipanteTarefaId id;

	@MapsId("tarefaId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "tarefa_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_participante_tarefa_tarefa")
	)
	private Tarefa tarefa;

	@MapsId("usuarioId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "usuario_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_participante_tarefa_usuario")
	)
	private Usuario usuario;

	@Column(name = "adicionado_em", nullable = false, updatable = false)
	private Instant adicionadoEm;

	protected ParticipanteTarefa() {
		// Required by JPA.
	}

	public ParticipanteTarefa(Tarefa tarefa, Usuario usuario) {
		this.tarefa = Objects.requireNonNull(tarefa, "A tarefa e obrigatoria.");
		this.usuario = Objects.requireNonNull(usuario, "O participante e obrigatorio.");

		if (tarefa.getTipo() != TipoTarefa.COMPARTILHADA) {
			throw new IllegalArgumentException("Somente tarefas compartilhadas aceitam participantes.");
		}
		if (tarefa.getId() == null || usuario.getId() == null) {
			throw new IllegalArgumentException("Tarefa e participante devem estar persistidos.");
		}
		if (tarefa.getCriador() == null
				|| Objects.equals(tarefa.getCriador().getId(), usuario.getId())) {
			throw new IllegalArgumentException("O criador e responsavel principal, nao participante.");
		}

		this.id = new ParticipanteTarefaId(tarefa.getId(), usuario.getId());
	}

	public ParticipanteTarefaId getId() {
		return id;
	}

	public Tarefa getTarefa() {
		return tarefa;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Instant getAdicionadoEm() {
		return adicionadoEm;
	}

	@PrePersist
	void definirDataDeInclusao() {
		if (adicionadoEm == null) {
			adicionadoEm = Instant.now();
		}
	}
}
