package br.com.organizacaoacademica.tarefa.service;

public class ConviteNaoEncontradoException extends RuntimeException {
	public ConviteNaoEncontradoException() {
		super("Convite nao encontrado.");
	}
}
