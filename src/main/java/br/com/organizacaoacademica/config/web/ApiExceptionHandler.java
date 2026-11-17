package br.com.organizacaoacademica.config.web;

import br.com.organizacaoacademica.disciplina.service.DisciplinaEmUsoException;
import br.com.organizacaoacademica.disciplina.service.DisciplinaNaoEncontradaException;
import br.com.organizacaoacademica.tarefa.service.FiltroTarefaInvalidoException;
import br.com.organizacaoacademica.tarefa.service.TarefaConcluidaException;
import br.com.organizacaoacademica.tarefa.service.TarefaDadosInvalidosException;
import br.com.organizacaoacademica.tarefa.service.TarefaNaoEncontradaException;
import br.com.organizacaoacademica.tarefa.service.TarefaTransicaoEstadoInvalidaException;
import br.com.organizacaoacademica.usuario.service.CredenciaisInvalidasException;
import br.com.organizacaoacademica.usuario.service.EmailJaCadastradoException;
import br.com.organizacaoacademica.usuario.service.SenhaForaDaPoliticaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte erros conhecidos de entrada e autenticação para respostas estáveis da API. */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(TarefaNaoEncontradaException.class)
	public ResponseEntity<ApiErrorResponse> tarefaNaoEncontrada(
			TarefaNaoEncontradaException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.NOT_FOUND, "TAREFA_NOT_FOUND", exception.getMessage(), request);
	}

	@ExceptionHandler(TarefaConcluidaException.class)
	public ResponseEntity<ApiErrorResponse> tarefaConcluida(
			TarefaConcluidaException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.CONFLICT, "TAREFA_CONCLUIDA", exception.getMessage(), request);
	}

	@ExceptionHandler(TarefaDadosInvalidosException.class)
	public ResponseEntity<ApiErrorResponse> dadosDaTarefaInvalidos(
			TarefaDadosInvalidosException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.BAD_REQUEST, "INVALID_TASK_DATA", exception.getMessage(), request);
	}

	@ExceptionHandler(TarefaTransicaoEstadoInvalidaException.class)
	public ResponseEntity<ApiErrorResponse> transicaoDeEstadoInvalida(
			TarefaTransicaoEstadoInvalidaException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.CONFLICT, "INVALID_TASK_STATE_TRANSITION", exception.getMessage(), request);
	}

	@ExceptionHandler(FiltroTarefaInvalidoException.class)
	public ResponseEntity<ApiErrorResponse> filtroTarefaInvalido(
			FiltroTarefaInvalidoException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.BAD_REQUEST, "INVALID_TASK_FILTER", exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> parametroInvalido(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.BAD_REQUEST, "INVALID_QUERY_PARAMETER", "Um parâmetro da consulta é inválido.", request);
	}

	@ExceptionHandler(DisciplinaNaoEncontradaException.class)
	public ResponseEntity<ApiErrorResponse> disciplinaNaoEncontrada(
			DisciplinaNaoEncontradaException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.NOT_FOUND, "DISCIPLINA_NOT_FOUND", exception.getMessage(), request);
	}

	@ExceptionHandler(DisciplinaEmUsoException.class)
	public ResponseEntity<ApiErrorResponse> disciplinaEmUso(
			DisciplinaEmUsoException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.CONFLICT, "DISCIPLINA_IN_USE", exception.getMessage(), request);
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	public ResponseEntity<ApiErrorResponse> credenciaisInvalidas(
			CredenciaisInvalidasException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "E-mail ou senha inválidos.", request);
	}

	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<ApiErrorResponse> emailJaCadastrado(
			EmailJaCadastradoException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "Não foi possível cadastrar esta conta.", request);
	}

	@ExceptionHandler(SenhaForaDaPoliticaException.class)
	public ResponseEntity<ApiErrorResponse> senhaForaDaPolitica(
			SenhaForaDaPoliticaException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> dadosInvalidos(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Os dados enviados são inválidos.", request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> jsonInvalido(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return erro(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "O corpo da requisição está ausente ou malformado.", request);
	}

	private ResponseEntity<ApiErrorResponse> erro(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request
	) {
		return ResponseEntity.status(status)
				.body(ApiErrorResponse.of(status, code, message, request.getRequestURI()));
	}
}
