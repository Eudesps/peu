package br.com.organizacaoacademica.usuario.service;

public class CredenciaisInvalidasException extends RuntimeException {
	public CredenciaisInvalidasException() {
		super("E-mail ou senha inválidos.");
	}
}
