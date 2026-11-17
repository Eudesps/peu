package br.com.organizacaoacademica.tarefa.service;

public class TarefaTransicaoEstadoInvalidaException extends RuntimeException {
	public TarefaTransicaoEstadoInvalidaException() {
		super("A transição de estado solicitada não é permitida.");
	}
}
