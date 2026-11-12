package br.com.organizacaoacademica.disciplina.service;

public class DisciplinaEmUsoException extends RuntimeException {
	public DisciplinaEmUsoException() {
		super("A disciplina está associada a tarefas e não pode ser removida.");
	}
}
