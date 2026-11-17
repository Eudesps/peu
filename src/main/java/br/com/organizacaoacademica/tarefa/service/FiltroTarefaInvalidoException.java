package br.com.organizacaoacademica.tarefa.service;

public class FiltroTarefaInvalidoException extends RuntimeException {
	public FiltroTarefaInvalidoException() {
		super("Os filtros de prazo ou de ordenação são inválidos.");
	}
}
