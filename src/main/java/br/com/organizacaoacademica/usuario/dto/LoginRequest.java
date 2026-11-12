package br.com.organizacaoacademica.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 288) String senha
) {
	public LoginRequest {
		email = email == null ? null : email.trim();
	}

	@Override
	public String toString() {
		return "LoginRequest[credenciais=[REDACTED]]";
	}
}
