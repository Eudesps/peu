package br.com.organizacaoacademica.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
		@NotBlank @Size(max = 120) String nome,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 288) String senha
) {
	public CadastroUsuarioRequest {
		nome = nome == null ? null : nome.trim();
		email = email == null ? null : email.trim();
	}
}
