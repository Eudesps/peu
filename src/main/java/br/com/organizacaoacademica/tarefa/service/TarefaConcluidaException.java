package br.com.organizacaoacademica.tarefa.service;

public class TarefaConcluidaException extends RuntimeException {
	public TarefaConcluidaException() {
		super("Reabra a tarefa antes de editar seus dados.");
	}
}
