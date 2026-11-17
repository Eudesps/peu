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
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTests {

	private final ApiExceptionHandler handler = new ApiExceptionHandler();

	@Test
	void mapeiaErrosDeTarefaParaStatusECodigosEstaveis() {
		assertErro(handler.tarefaNaoEncontrada(new TarefaNaoEncontradaException(), request("/api/v1/tarefas/8")),
				HttpStatus.NOT_FOUND, "TAREFA_NOT_FOUND", "/api/v1/tarefas/8");
		assertErro(handler.tarefaConcluida(new TarefaConcluidaException(), request("/api/v1/tarefas/8")),
				HttpStatus.CONFLICT, "TAREFA_CONCLUIDA", "/api/v1/tarefas/8");
		assertErro(handler.dadosDaTarefaInvalidos(new TarefaDadosInvalidosException(), request("/api/v1/tarefas")),
				HttpStatus.BAD_REQUEST, "INVALID_TASK_DATA", "/api/v1/tarefas");
		assertErro(handler.transicaoDeEstadoInvalida(
				new TarefaTransicaoEstadoInvalidaException(), request("/api/v1/tarefas/8/estado")),
				HttpStatus.CONFLICT, "INVALID_TASK_STATE_TRANSITION", "/api/v1/tarefas/8/estado");
		assertErro(handler.filtroTarefaInvalido(
				new FiltroTarefaInvalidoException(), request("/api/v1/tarefas")),
				HttpStatus.BAD_REQUEST, "INVALID_TASK_FILTER", "/api/v1/tarefas");
	}

	@Test
	void mapeiaErrosDeDisciplinaParaStatusECodigosEstaveis() {
		assertErro(handler.disciplinaNaoEncontrada(
				new DisciplinaNaoEncontradaException(), request("/api/v1/disciplinas/4")),
				HttpStatus.NOT_FOUND, "DISCIPLINA_NOT_FOUND", "/api/v1/disciplinas/4");
		assertErro(handler.disciplinaEmUso(
				new DisciplinaEmUsoException(), request("/api/v1/disciplinas/4")),
				HttpStatus.CONFLICT, "DISCIPLINA_IN_USE", "/api/v1/disciplinas/4");
	}

	@Test
	void mapeiaErrosDeAutenticacaoECadastroSemExporDadosDeCredencial() {
		ResponseEntity<ApiErrorResponse> credenciais = handler.credenciaisInvalidas(
				new CredenciaisInvalidasException(), request("/api/v1/auth/login"));
		assertErro(credenciais, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "/api/v1/auth/login");
		assertThat(credenciais.getBody().message()).doesNotContain("senha-forte", "aluno@example.edu");

		assertErro(handler.emailJaCadastrado(
				new EmailJaCadastradoException(), request("/api/v1/auth/register")),
				HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "/api/v1/auth/register");
		assertErro(handler.senhaForaDaPolitica(
				new SenhaForaDaPoliticaException(), request("/api/v1/auth/register")),
				HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "/api/v1/auth/register");
	}

	private static MockHttpServletRequest request(String path) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(path);
		return request;
	}

	private static void assertErro(
			ResponseEntity<ApiErrorResponse> response,
			HttpStatus status,
			String code,
			String path
	) {
		assertThat(response.getStatusCode()).isEqualTo(status);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(status.value());
		assertThat(response.getBody().code()).isEqualTo(code);
		assertThat(response.getBody().path()).isEqualTo(path);
	}
}
