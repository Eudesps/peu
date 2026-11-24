package br.com.organizacaoacademica.tarefa.controller;

import br.com.organizacaoacademica.tarefa.dto.ConviteTarefaRequest;
import br.com.organizacaoacademica.tarefa.dto.RespostaConviteRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaAtualizarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaCriarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaEstadoRequest;
import br.com.organizacaoacademica.tarefa.entity.EstadoConvite;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;
import br.com.organizacaoacademica.tarefa.repository.ParticipanteTarefaRepository;
import br.com.organizacaoacademica.usuario.dto.CadastroUsuarioRequest;
import br.com.organizacaoacademica.usuario.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConviteTarefaFlowIntegrationTests {

	private static final String SENHA = "senha-forte-para-convite";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private ParticipanteTarefaRepository participanteRepository;

	@Test
	void conviteFicaPendenteEContaConvidadaAindaNaoAcessaTarefa() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Trabalho compartilhado");
		long conviteId = convidar(criador, tarefaId, convidado.email());

		mockMvc.perform(get("/api/v1/tarefas/{id}/convites", tarefaId).session(criador.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(conviteId))
				.andExpect(jsonPath("$[0].emailConvidado").value(convidado.email()))
				.andExpect(jsonPath("$[0].estado").value("PENDENTE"));
		mockMvc.perform(get("/api/v1/tarefas/{tarefaId}/convites/{conviteId}", tarefaId, conviteId)
					.session(criador.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(conviteId))
				.andExpect(jsonPath("$.estado").value("PENDENTE"));
		mockMvc.perform(get("/api/v1/convites").session(convidado.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(conviteId))
				.andExpect(jsonPath("$[0].estado").value("PENDENTE"));
		mockMvc.perform(get("/api/v1/convites/{id}", conviteId).session(convidado.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tarefaTitulo").value("Trabalho compartilhado"));
		mockMvc.perform(get("/api/v1/tarefas").session(convidado.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(convidado.sessao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TAREFA_NOT_FOUND"));
		assertThat(participanteRepository.existsByTarefa_IdAndUsuario_Id(tarefaId, usuarioId(convidado)))
				.isFalse();
	}

	@Test
	void aceiteCriaParticipacaoEConcedeSomenteLeituraDaTarefa() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Entrega de grupo");
		long conviteId = convidar(criador, tarefaId, convidado.email());

		responder(convidado, conviteId, EstadoConvite.ACEITO)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("ACEITO"))
				.andExpect(jsonPath("$.respondidoEm").isNotEmpty());

		assertThat(participanteRepository.existsByTarefa_IdAndUsuario_Id(tarefaId, usuarioId(convidado)))
				.isTrue();
		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(convidado.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Entrega de grupo"));
		mockMvc.perform(get("/api/v1/tarefas").session(convidado.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(tarefaId));

		Csrf csrf = obterCsrf(convidado.sessao());
		mockMvc.perform(put("/api/v1/tarefas/{id}", tarefaId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaAtualizarRequest(
							"Alteracao proibida", null, null, LocalDate.of(2026, 12, 10), PrioridadeTarefa.ALTA))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TAREFA_NOT_FOUND"));
		mockMvc.perform(patch("/api/v1/tarefas/{id}/estado", tarefaId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaEstadoRequest(EstadoTarefa.CONCLUIDA))))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/v1/tarefas/{id}", tarefaId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token()))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/v1/tarefas/{id}/convites", tarefaId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new ConviteTarefaRequest(criador.email()))))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(criador.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Entrega de grupo"));
	}

	@Test
	void recusaNaoConcedeAcessoEMesmaContaPodeReceberNovoConvite() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Projeto de pesquisa");
		long conviteId = convidar(criador, tarefaId, convidado.email());

		responder(convidado, conviteId, EstadoConvite.RECUSADO)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("RECUSADO"))
				.andExpect(jsonPath("$.respondidoEm").isNotEmpty());
		assertThat(participanteRepository.existsByTarefa_IdAndUsuario_Id(tarefaId, usuarioId(convidado)))
				.isFalse();
		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(convidado.sessao()))
				.andExpect(status().isNotFound());

		long novoConviteId = convidar(criador, tarefaId, convidado.email());
		assertThat(novoConviteId).isNotEqualTo(conviteId);
		mockMvc.perform(get("/api/v1/tarefas/{id}/convites", tarefaId).session(criador.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].estado").value("PENDENTE"))
				.andExpect(jsonPath("$[1].estado").value("RECUSADO"));
	}

	@Test
	void criadorCancelaSomenteConvitePendenteEConviteCanceladoPodeSerReenviado() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Seminario");
		long conviteId = convidar(criador, tarefaId, convidado.email());
		Csrf csrf = obterCsrf(criador.sessao());

		mockMvc.perform(delete("/api/v1/tarefas/{tarefaId}/convites/{conviteId}", tarefaId, conviteId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CANCELADO"))
				.andExpect(jsonPath("$.respondidoEm").isNotEmpty());
		responder(convidado, conviteId, EstadoConvite.ACEITO)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_INVITATION_STATE"));

		long novoConviteId = convidar(criador, tarefaId, convidado.email());
		assertThat(novoConviteId).isNotEqualTo(conviteId);
	}

	@Test
	void rejeitaContaInexistenteAutoConviteDuplicidadeTarefaIndividualEConcluida() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaCompartilhada = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Compartilhada");

		convidar(criador, tarefaCompartilhada, "nao-cadastrado@example.edu", 404, "INVITEE_NOT_FOUND");
		convidar(criador, tarefaCompartilhada, criador.email(), 409, "INVITATION_CONFLICT");
		convidar(criador, tarefaCompartilhada, convidado.email());
		convidar(criador, tarefaCompartilhada, convidado.email(), 409, "INVITATION_CONFLICT");

		long individual = criarTarefa(criador, TipoTarefa.INDIVIDUAL, "Individual");
		convidar(criador, individual, convidado.email(), 409, "INVITATION_NOT_ALLOWED");

		long concluida = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Concluida");
		alterarEstado(criador, concluida, EstadoTarefa.CONCLUIDA);
		convidar(criador, concluida, convidado.email(), 409, "TAREFA_CONCLUIDA");
	}

	@Test
	void validaEmailRespostaDaPessoaConvidadaEImpedeRepetirRespostaOuConvidarParticipanteAtivo() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Validacao do convite");
		Csrf csrfCriador = obterCsrf(criador.sessao());

		mockMvc.perform(post("/api/v1/tarefas/{id}/convites", tarefaId)
					.session(csrfCriador.sessao()).header(csrfCriador.header(), csrfCriador.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"email-invalido\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		long conviteId = convidar(criador, tarefaId, convidado.email());
		responder(convidado, conviteId, EstadoConvite.PENDENTE)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INVITATION_DATA"));
		responder(convidado, conviteId, EstadoConvite.ACEITO)
				.andExpect(status().isOk());
		responder(convidado, conviteId, EstadoConvite.RECUSADO)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_INVITATION_STATE"));
		convidar(criador, tarefaId, convidado.email(), 409, "INVITATION_CONFLICT");
	}

	@Test
	void somenteDestinatarioAcessaConviteESomenteCriadorAdministraConvites() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		SessaoUsuario terceiraPessoa = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Privada");
		long conviteId = convidar(criador, tarefaId, convidado.email());
		Csrf csrfTerceiraPessoa = obterCsrf(terceiraPessoa.sessao());

		mockMvc.perform(get("/api/v1/convites/{id}", conviteId).session(terceiraPessoa.sessao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("INVITATION_NOT_FOUND"));
		mockMvc.perform(patch("/api/v1/convites/{id}/resposta", conviteId)
					.session(csrfTerceiraPessoa.sessao()).header(csrfTerceiraPessoa.header(), csrfTerceiraPessoa.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new RespostaConviteRequest(EstadoConvite.ACEITO))))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/tarefas/{id}/convites", tarefaId).session(terceiraPessoa.sessao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TAREFA_NOT_FOUND"));
		mockMvc.perform(get("/api/v1/tarefas/{tarefaId}/convites/{conviteId}", tarefaId, conviteId)
					.session(terceiraPessoa.sessao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TAREFA_NOT_FOUND"));
		mockMvc.perform(delete("/api/v1/tarefas/{tarefaId}/convites/{conviteId}", tarefaId, conviteId)
					.session(csrfTerceiraPessoa.sessao())
					.header(csrfTerceiraPessoa.header(), csrfTerceiraPessoa.token()))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/v1/tarefas/{id}/convites", tarefaId)
					.session(csrfTerceiraPessoa.sessao())
					.header(csrfTerceiraPessoa.header(), csrfTerceiraPessoa.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new ConviteTarefaRequest(criador.email()))))
				.andExpect(status().isNotFound());
		Csrf csrfConvidado = obterCsrf(convidado.sessao());
		mockMvc.perform(delete("/api/v1/tarefas/{tarefaId}/convites/{conviteId}", tarefaId, conviteId)
					.session(csrfConvidado.sessao())
					.header(csrfConvidado.header(), csrfConvidado.token()))
				.andExpect(status().isNotFound());
	}

	@Test
	void concluirTarefaCancelaConvitesPendentes() throws Exception {
		SessaoUsuario criador = criarUsuarioAutenticado();
		SessaoUsuario convidado = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "Prazo encerrado");
		long conviteId = convidar(criador, tarefaId, convidado.email());

		alterarEstado(criador, tarefaId, EstadoTarefa.CONCLUIDA);

		mockMvc.perform(get("/api/v1/convites/{id}", conviteId).session(convidado.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CANCELADO"))
				.andExpect(jsonPath("$.respondidoEm").isNotEmpty());
		responder(convidado, conviteId, EstadoConvite.ACEITO)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_INVITATION_STATE"));
	}

	@Test
	void rotasExigemAutenticacaoECsrfENaoPossuemAcessoPorLinkPublico() throws Exception {
		Csrf visitante = obterCsrf(null);
		mockMvc.perform(get("/api/v1/convites").session(visitante.sessao()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
		mockMvc.perform(post("/api/v1/tarefas/{id}/convites", 1L)
					.session(visitante.sessao()).header(visitante.header(), visitante.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"pessoa@example.edu\"}"))
				.andExpect(status().isUnauthorized());

		SessaoUsuario criador = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(criador, TipoTarefa.COMPARTILHADA, "CSRF");
		SessaoUsuario convidado = criarUsuarioAutenticado();
		mockMvc.perform(post("/api/v1/tarefas/{id}/convites", tarefaId)
					.session(criador.sessao())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"pessoa@example.edu\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		long conviteId = convidar(criador, tarefaId, convidado.email());
		mockMvc.perform(patch("/api/v1/convites/{id}/resposta", conviteId)
					.session(convidado.sessao())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new RespostaConviteRequest(EstadoConvite.ACEITO))))
				.andExpect(status().isForbidden());
	}

	private long convidar(SessaoUsuario criador, long tarefaId, String email) throws Exception {
		MvcResult result = convidar(criador, tarefaId, email, 201, null);
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private MvcResult convidar(
			SessaoUsuario criador,
			long tarefaId,
			String email,
			int statusEsperado,
			String codigoEsperado
	) throws Exception {
		Csrf csrf = obterCsrf(criador.sessao());
		var resultado = mockMvc.perform(post("/api/v1/tarefas/{id}/convites", tarefaId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new ConviteTarefaRequest(email))))
				.andExpect(status().is(statusEsperado));
		if (codigoEsperado != null) {
			resultado.andExpect(jsonPath("$.code").value(codigoEsperado));
		}
		return resultado.andReturn();
	}

	private org.springframework.test.web.servlet.ResultActions responder(
			SessaoUsuario usuario,
			long conviteId,
			EstadoConvite estado
	) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		return mockMvc.perform(patch("/api/v1/convites/{id}/resposta", conviteId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new RespostaConviteRequest(estado))));
	}

	private long criarTarefa(SessaoUsuario usuario, TipoTarefa tipo, String titulo) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		MvcResult result = mockMvc.perform(post("/api/v1/tarefas")
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaCriarRequest(
							tipo, titulo, null, null, LocalDate.of(2026, 12, 10), PrioridadeTarefa.MEDIA))))
				.andExpect(status().isCreated())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private void alterarEstado(SessaoUsuario usuario, long tarefaId, EstadoTarefa estado) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		mockMvc.perform(patch("/api/v1/tarefas/{id}/estado", tarefaId)
					.session(csrf.sessao()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaEstadoRequest(estado))))
				.andExpect(status().isOk());
	}

	private SessaoUsuario criarUsuarioAutenticado() throws Exception {
		String email = "convite-" + UUID.randomUUID() + "@example.edu";
		Csrf csrfCadastro = obterCsrf(null);
		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrfCadastro.sessao()).header(csrfCadastro.header(), csrfCadastro.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new CadastroUsuarioRequest("Estudante", email, SENHA))))
				.andExpect(status().isCreated());

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
					.session(csrfCadastro.sessao()).header(csrfCadastro.header(), csrfCadastro.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new LoginRequest(email, SENHA))))
				.andExpect(status().isOk())
				.andReturn();
		return new SessaoUsuario(email, (MockHttpSession) login.getRequest().getSession(false));
	}

	private long usuarioId(SessaoUsuario usuario) throws Exception {
		MvcResult perfil = mockMvc.perform(get("/api/v1/auth/me").session(usuario.sessao()))
				.andExpect(status().isOk())
				.andReturn();
		return jsonMapper.readTree(perfil.getResponse().getContentAsString()).get("id").asLong();
	}

	private Csrf obterCsrf(MockHttpSession sessao) throws Exception {
		var request = get("/api/v1/auth/csrf");
		if (sessao != null) {
			request.session(sessao);
		}
		MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
		JsonNode token = jsonMapper.readTree(result.getResponse().getContentAsString());
		return new Csrf(
				(MockHttpSession) result.getRequest().getSession(false),
				token.get("headerName").asText(),
				token.get("token").asText());
	}

	private record SessaoUsuario(String email, MockHttpSession sessao) {
	}

	private record Csrf(MockHttpSession sessao, String header, String token) {
	}
}
