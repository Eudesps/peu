package br.com.organizacaoacademica.disciplina.dto;

import java.time.Instant;

/** Representação pública da disciplina, sem expor a entidade ou dados do usuário. */
public record DisciplinaResponse(Long id, String nome, Instant criadaEm) {
}
