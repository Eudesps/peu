package br.com.organizacaoacademica.tarefa.service;

public class ConviteNaoPermitidoException extends RuntimeException {
	public ConviteNaoPermitidoException() {
		super("Esta tarefa nao permite convites.");
	}
}
