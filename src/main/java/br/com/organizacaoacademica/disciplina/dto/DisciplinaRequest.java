package br.com.organizacaoacademica.disciplina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisciplinaRequest(@NotBlank @Size(max = 120) String nome) {
	public DisciplinaRequest {
		nome = nome == null ? null : nome.trim();
	}
}
