package br.com.organizacaoacademica.tarefa.dto;

import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Campos editáveis da tarefa; criador e tipo permanecem imutáveis. */
public record TarefaAtualizarRequest(
		@NotBlank @Size(max = 160) String titulo,
		String descricao,
		Long disciplinaId,
		@NotNull LocalDate prazo,
		@NotNull PrioridadeTarefa prioridade
) {
	public TarefaAtualizarRequest {
		titulo = titulo == null ? null : titulo.trim();
		descricao = normalizarDescricao(descricao);
	}

	private static String normalizarDescricao(String descricao) {
		if (descricao == null) {
			return null;
		}
		String normalizada = descricao.trim();
		return normalizada.isEmpty() ? null : normalizada;
	}
}
