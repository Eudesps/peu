package br.com.organizacaoacademica.config.security;

import br.com.organizacaoacademica.config.web.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/** Devolve erros de autenticação e autorização no mesmo formato JSON da API. */
public class ApiSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	public ApiSecurityExceptionHandler(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception
	) throws IOException {
		writeError(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Autenticação necessária.");
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException exception
	) throws IOException {
		writeError(request, response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Acesso negado.");
	}

	private void writeError(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpStatus status,
			String code,
			String message
	) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-store");
		jsonMapper.writeValue(response.getOutputStream(),
				ApiErrorResponse.of(status, code, message, request.getRequestURI()));
	}
}
