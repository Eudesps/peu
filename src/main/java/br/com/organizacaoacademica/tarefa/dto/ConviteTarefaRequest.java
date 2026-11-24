package br.com.organizacaoacademica.tarefa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/** Identifica a conta existente que recebera o convite. */
public record ConviteTarefaRequest(
		@NotBlank @Email @Size(max = 254) String email
) {
	public ConviteTarefaRequest {
		email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}
