package br.com.organizacaoacademica.tarefa.dto;

import br.com.organizacaoacademica.tarefa.entity.EstadoConvite;
import jakarta.validation.constraints.NotNull;

/** A pessoa convidada pode aceitar ou recusar, mas nao escolher o estado pendente/cancelado. */
public record RespostaConviteRequest(@NotNull EstadoConvite estado) {
}
