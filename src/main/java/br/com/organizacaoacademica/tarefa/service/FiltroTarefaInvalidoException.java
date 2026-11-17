package br.com.organizacaoacademica.tarefa.service;

public class FiltroTarefaInvalidoException extends RuntimeException {
	public FiltroTarefaInvalidoException() {
		super("O início do prazo não pode ser posterior ao fim do prazo.");
	}
}
