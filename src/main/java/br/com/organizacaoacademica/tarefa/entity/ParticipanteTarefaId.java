package br.com.organizacaoacademica.tarefa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/** Chave composta que identifica uma pessoa participante dentro de uma tarefa. */
@Embeddable
public class ParticipanteTarefaId implements Serializable {

	@Column(name = "tarefa_id", nullable = false)
	private Long tarefaId;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	protected ParticipanteTarefaId() {
		// Required by JPA.
	}

	public ParticipanteTarefaId(Long tarefaId, Long usuarioId) {
		this.tarefaId = Objects.requireNonNull(tarefaId, "tarefaId e obrigatorio.");
		this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId e obrigatorio.");
	}

	public Long getTarefaId() {
		return tarefaId;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ParticipanteTarefaId that)) {
			return false;
		}
		return Objects.equals(tarefaId, that.tarefaId)
				&& Objects.equals(usuarioId, that.usuarioId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tarefaId, usuarioId);
	}
}
