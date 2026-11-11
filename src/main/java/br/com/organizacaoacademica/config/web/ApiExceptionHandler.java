package br.com.organizacaoacademica.config.web;

import br.com.organizacaoacademica.usuario.service.CredenciaisInvalidasException;
import br.com.organizacaoacademica.usuario.service.EmailJaCadastradoException;
import br.com.organizacaoacademica.usuario.service.SenhaForaDaPoliticaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte erros conhecidos de entrada e autenticação para respostas estáveis da API. */
@RestControllerAdvice
public class ApiExceptionHandler {

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
