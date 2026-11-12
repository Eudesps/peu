package br.com.organizacaoacademica.disciplina.service;

public class DisciplinaNaoEncontradaException extends RuntimeException {
	public DisciplinaNaoEncontradaException() {
		super("Disciplina não encontrada.");
	}
}
