package br.com.organizacaoacademica.tarefa.service;

public class TarefaDadosInvalidosException extends RuntimeException {
	public TarefaDadosInvalidosException() {
		super("Informe título, tipo e prazo válidos; a edição também exige prioridade.");
	}
}
