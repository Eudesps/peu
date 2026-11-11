package br.com.organizacaoacademica.usuario.service;

public class SenhaForaDaPoliticaException extends RuntimeException {
	public SenhaForaDaPoliticaException() {
		super("A senha deve conter pelo menos 12 caracteres e no máximo 72 bytes em UTF-8.");
	}
}
