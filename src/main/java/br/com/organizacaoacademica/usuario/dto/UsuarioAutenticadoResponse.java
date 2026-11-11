package br.com.organizacaoacademica.usuario.dto;

import java.time.Instant;

/** Dados públicos da conta; deliberadamente não contém o hash da senha. */
public record UsuarioAutenticadoResponse(Long id, String nome, String email, Instant criadoEm) {
}
