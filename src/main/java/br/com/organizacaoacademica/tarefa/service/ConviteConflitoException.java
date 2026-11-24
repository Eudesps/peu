package br.com.organizacaoacademica.tarefa.service;

public class ConviteConflitoException extends RuntimeException {
	public ConviteConflitoException() {
		super("Ja existe participacao ou convite pendente para essa pessoa e tarefa.");
	}
}
