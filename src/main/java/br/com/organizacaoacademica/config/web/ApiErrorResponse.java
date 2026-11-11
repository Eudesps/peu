package br.com.organizacaoacademica.config.web;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/** Formato comum para erros devolvidos pela API. */
public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String code,
		String message,
		String path
) {
	public static ApiErrorResponse of(HttpStatus status, String code, String message, String path) {
		return new ApiErrorResponse(Instant.now(), status.value(), code, message, path);
	}
}
