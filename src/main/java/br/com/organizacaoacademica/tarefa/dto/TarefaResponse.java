package br.com.organizacaoacademica.tarefa.dto;

import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;

import java.time.Instant;
import java.time.LocalDate;

public record TarefaResponse(
		Long id,
		Long disciplinaId,
		String disciplinaNome,
		TipoTarefa tipo,
		String titulo,
		String descricao,
		LocalDate prazo,
		PrioridadeTarefa prioridade,
		EstadoTarefa estado,
		Instant criadaEm,
		Instant atualizadaEm
) {
}
