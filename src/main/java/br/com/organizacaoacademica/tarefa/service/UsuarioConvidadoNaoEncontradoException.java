package br.com.organizacaoacademica.tarefa.service;

public class UsuarioConvidadoNaoEncontradoException extends RuntimeException {
	public UsuarioConvidadoNaoEncontradoException() {
		super("Nao existe uma conta cadastrada com o e-mail informado.");
	}
}
