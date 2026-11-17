package br.com.organizacaoacademica.tarefa.dto;

import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import jakarta.validation.constraints.NotNull;

public record TarefaEstadoRequest(@NotNull EstadoTarefa estado) {
}
