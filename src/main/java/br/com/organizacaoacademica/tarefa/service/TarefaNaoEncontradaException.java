package br.com.organizacaoacademica.tarefa.service;

public class TarefaNaoEncontradaException extends RuntimeException {
	public TarefaNaoEncontradaException() {
		super("Tarefa não encontrada.");
	}
}
