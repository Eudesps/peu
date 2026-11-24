package br.com.organizacaoacademica.tarefa.dto;

import br.com.organizacaoacademica.tarefa.entity.EstadoConvite;

import java.time.Instant;

public record ConviteTarefaResponse(
		Long id,
		Long tarefaId,
		String tarefaTitulo,
		String emailConvidado,
		EstadoConvite estado,
		Instant criadoEm,
		Instant respondidoEm
) {
}
