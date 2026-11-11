package br.com.organizacaoacademica.usuario.service;

public class EmailJaCadastradoException extends RuntimeException {
	public EmailJaCadastradoException() {
		super("O e-mail já está cadastrado.");
	}
}
