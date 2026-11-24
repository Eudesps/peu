package br.com.organizacaoacademica.tarefa.service;

public class ConviteDadosInvalidosException extends RuntimeException {
	public ConviteDadosInvalidosException() {
		super("Informe um e-mail valido e uma resposta de convite permitida.");
	}
}
