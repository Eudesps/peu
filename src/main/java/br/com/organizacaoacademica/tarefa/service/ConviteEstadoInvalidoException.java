package br.com.organizacaoacademica.tarefa.service;

public class ConviteEstadoInvalidoException extends RuntimeException {
	public ConviteEstadoInvalidoException() {
		super("O convite nao permite a transicao de estado solicitada.");
	}
}
