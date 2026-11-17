package br.com.organizacaoacademica.tarefa.controller;

import br.com.organizacaoacademica.disciplina.dto.DisciplinaRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaAtualizarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaCriarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaEstadoRequest;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;
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

import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
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
class TarefaFlowIntegrationTests {

	private static final String SENHA = "senha-forte-para-tarefa";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void usuarioCriaTarefaComDisciplinaEValoresPadrao() throws Exception {
		SessaoUsuario usuario = criarUsuarioAutenticado();
		long disciplinaId = criarDisciplina(usuario, "Cálculo");
		TarefaCriarRequest request = new TarefaCriarRequest(
				TipoTarefa.INDIVIDUAL,
				"  Prova de integrais  ",
				"  Rever os exercícios  ",
				disciplinaId,
				LocalDate.of(2026, 12, 10),
				null
		);
		Csrf csrf = obterCsrf(usuario.sessao());

		MvcResult result = mockMvc.perform(post("/api/v1/tarefas")
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("INDIVIDUAL"))
				.andExpect(jsonPath("$.titulo").value("Prova de integrais"))
				.andExpect(jsonPath("$.descricao").value("Rever os exercícios"))
				.andExpect(jsonPath("$.disciplinaId").value(disciplinaId))
				.andExpect(jsonPath("$.disciplinaNome").value("Cálculo"))
				.andExpect(jsonPath("$.prazo").value("2026-12-10"))
				.andExpect(jsonPath("$.prioridade").value("MEDIA"))
				.andExpect(jsonPath("$.estado").value("ABERTA"))
				.andExpect(jsonPath("$.criadaEm").exists())
				.andExpect(jsonPath("$.atualizadaEm").exists())
				.andReturn();

		JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
		assertThat(result.getResponse().getHeader("Location"))
				.isEqualTo("/api/v1/tarefas/" + body.get("id").asLong());
	}

	@Test
	void listaFiltraPorDisciplinaPrazoPrioridadeEstadoEIsolaCadaUsuario() throws Exception {
		SessaoUsuario proprietario = criarUsuarioAutenticado();
		SessaoUsuario outraPessoa = criarUsuarioAutenticado();
		long disciplinaFiltrada = criarDisciplina(proprietario, "Zoologia");
		long outraDisciplina = criarDisciplina(proprietario, "Álgebra");

		long tarefaEsperada = criarTarefa(proprietario, new TarefaCriarRequest(
				TipoTarefa.INDIVIDUAL, "Prova", null, disciplinaFiltrada,
				LocalDate.of(2026, 12, 10), PrioridadeTarefa.ALTA));
		long tarefaAnterior = criarTarefa(proprietario, new TarefaCriarRequest(
				TipoTarefa.INDIVIDUAL, "Leitura", null, outraDisciplina,
				LocalDate.of(2026, 12, 20), PrioridadeTarefa.BAIXA));
		alterarEstado(proprietario, tarefaAnterior, EstadoTarefa.CONCLUIDA);
		criarTarefa(outraPessoa, new TarefaCriarRequest(
				TipoTarefa.COMPARTILHADA, "Tarefa de outra pessoa", null, null,
				LocalDate.of(2026, 12, 5), PrioridadeTarefa.ALTA));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("disciplinaId", String.valueOf(disciplinaFiltrada))
					.param("prazoInicio", "2026-12-01")
					.param("prazoFim", "2026-12-31")
					.param("prioridade", "ALTA")
					.param("estado", "ABERTA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(tarefaEsperada));

		mockMvc.perform(get("/api/v1/tarefas").session(proprietario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(tarefaEsperada))
				.andExpect(jsonPath("$[1].id").value(tarefaAnterior));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("ordenarPor", "PRIORIDADE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(tarefaAnterior))
				.andExpect(jsonPath("$[1].id").value(tarefaEsperada));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("ordenarPor", "DISCIPLINA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(tarefaAnterior))
				.andExpect(jsonPath("$[1].id").value(tarefaEsperada));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("ordenarPor", "ESTADO")
					.param("direcao", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(tarefaAnterior))
				.andExpect(jsonPath("$[1].id").value(tarefaEsperada));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("ordenarPor", "PRAZO")
					.param("direcao", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(tarefaAnterior))
				.andExpect(jsonPath("$[1].id").value(tarefaEsperada));

		mockMvc.perform(get("/api/v1/tarefas").session(outraPessoa.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].titulo").value("Tarefa de outra pessoa"));
	}

	@Test
	void usuarioVisualizaSuaTarefaMasRecebe404ParaIdDeOutraPessoa() throws Exception {
		SessaoUsuario proprietario = criarUsuarioAutenticado();
		SessaoUsuario outraPessoa = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(proprietario, tarefaBasica("Minha tarefa"));

		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(proprietario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Minha tarefa"));

		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(outraPessoa.sessao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TAREFA_NOT_FOUND"));
	}

	@Test
	void usuarioEditaDadosCentraisSemAlterarTipoOuCriador() throws Exception {
		SessaoUsuario usuario = criarUsuarioAutenticado();
		long disciplinaId = criarDisciplina(usuario, "Pesquisa");
		long tarefaId = criarTarefa(usuario, new TarefaCriarRequest(
				TipoTarefa.COMPARTILHADA, "Trabalho", null, null,
				LocalDate.of(2026, 12, 10), PrioridadeTarefa.BAIXA));
		TarefaAtualizarRequest request = new TarefaAtualizarRequest(
				"Trabalho final",
				"Dividir a pesquisa",
				disciplinaId,
				LocalDate.of(2026, 12, 20),
				PrioridadeTarefa.ALTA
		);
		Csrf csrf = obterCsrf(usuario.sessao());

		mockMvc.perform(put("/api/v1/tarefas/{id}", tarefaId)
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("COMPARTILHADA"))
				.andExpect(jsonPath("$.titulo").value("Trabalho final"))
				.andExpect(jsonPath("$.descricao").value("Dividir a pesquisa"))
				.andExpect(jsonPath("$.disciplinaId").value(disciplinaId))
				.andExpect(jsonPath("$.disciplinaNome").value("Pesquisa"))
				.andExpect(jsonPath("$.prazo").value("2026-12-20"))
				.andExpect(jsonPath("$.prioridade").value("ALTA"));
	}

	@Test
	void somenteCriadorPodeEditarOuAlterarEstadoEConclusaoBloqueiaEdicao() throws Exception {
		SessaoUsuario proprietario = criarUsuarioAutenticado();
		SessaoUsuario outraPessoa = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(proprietario, tarefaBasica("Tarefa protegida"));
		TarefaAtualizarRequest alteracao = new TarefaAtualizarRequest(
				"Tentativa indevida", null, null,
				LocalDate.of(2026, 12, 12), PrioridadeTarefa.ALTA);
		Csrf csrfOutraPessoa = obterCsrf(outraPessoa.sessao());

		mockMvc.perform(put("/api/v1/tarefas/{id}", tarefaId)
					.session(csrfOutraPessoa.sessao())
					.header(csrfOutraPessoa.header(), csrfOutraPessoa.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(alteracao)))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/api/v1/tarefas/{id}", tarefaId)
					.session(csrfOutraPessoa.sessao())
					.header(csrfOutraPessoa.header(), csrfOutraPessoa.token()))
				.andExpect(status().isNotFound());
		mockMvc.perform(patch("/api/v1/tarefas/{id}/estado", tarefaId)
					.session(csrfOutraPessoa.sessao())
					.header(csrfOutraPessoa.header(), csrfOutraPessoa.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaEstadoRequest(EstadoTarefa.CONCLUIDA))))
				.andExpect(status().isNotFound());

		alterarEstado(proprietario, tarefaId, EstadoTarefa.CONCLUIDA);
		Csrf csrfProprietario = obterCsrf(proprietario.sessao());
		mockMvc.perform(put("/api/v1/tarefas/{id}", tarefaId)
					.session(csrfProprietario.sessao())
					.header(csrfProprietario.header(), csrfProprietario.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(alteracao)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("TAREFA_CONCLUIDA"));

		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(proprietario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Tarefa protegida"))
				.andExpect(jsonPath("$.estado").value("CONCLUIDA"));
	}

	@Test
	void criadorPodeReabrirTarefaEExcluirSuaTarefa() throws Exception {
		SessaoUsuario usuario = criarUsuarioAutenticado();
		long tarefaId = criarTarefa(usuario, tarefaBasica("Excluir depois"));
		alterarEstado(usuario, tarefaId, EstadoTarefa.ABERTA);
		alterarEstado(usuario, tarefaId, EstadoTarefa.CONCLUIDA);
		alterarEstado(usuario, tarefaId, EstadoTarefa.ABERTA);
		alterarEstado(usuario, tarefaId, EstadoTarefa.CONCLUIDA);

		Csrf csrf = obterCsrf(usuario.sessao());
		mockMvc.perform(delete("/api/v1/tarefas/{id}", tarefaId)
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/tarefas/{id}", tarefaId).session(usuario.sessao()))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejeitaDadosInvalidosDisciplinasAlheiasEFiltrosIncoerentes() throws Exception {
		SessaoUsuario proprietario = criarUsuarioAutenticado();
		SessaoUsuario outraPessoa = criarUsuarioAutenticado();
		long disciplinaAlheia = criarDisciplina(proprietario, "Disciplina privada");
		long tarefaDaOutraPessoa = criarTarefa(outraPessoa, tarefaBasica("Sem disciplina"));
		long tarefaPropria = criarTarefa(proprietario, tarefaBasica("Validar atualização"));
		Csrf csrf = obterCsrf(outraPessoa.sessao());

		mockMvc.perform(post("/api/v1/tarefas")
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaCriarRequest(
							TipoTarefa.INDIVIDUAL, "Inválida", null, disciplinaAlheia,
							LocalDate.of(2026, 12, 10), PrioridadeTarefa.MEDIA))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("DISCIPLINA_NOT_FOUND"));

		Csrf csrfEdicao = obterCsrf(outraPessoa.sessao());
		mockMvc.perform(put("/api/v1/tarefas/{id}", tarefaDaOutraPessoa)
					.session(csrfEdicao.sessao())
					.header(csrfEdicao.header(), csrfEdicao.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaAtualizarRequest(
							"Sem disciplina", null, disciplinaAlheia,
							LocalDate.of(2026, 12, 10), PrioridadeTarefa.MEDIA))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("DISCIPLINA_NOT_FOUND"));

		Csrf csrfProprietario = obterCsrf(proprietario.sessao());
		mockMvc.perform(post("/api/v1/tarefas")
					.session(csrfProprietario.sessao())
					.header(csrfProprietario.header(), csrfProprietario.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaCriarRequest(
							TipoTarefa.INDIVIDUAL, "   ", null, null,
							LocalDate.of(2026, 12, 10), PrioridadeTarefa.MEDIA))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		Csrf csrfCamposObrigatorios = obterCsrf(proprietario.sessao());
		mockMvc.perform(post("/api/v1/tarefas")
					.session(csrfCamposObrigatorios.sessao())
					.header(csrfCamposObrigatorios.header(), csrfCamposObrigatorios.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"tipo\":\"INDIVIDUAL\",\"titulo\":\"Sem prazo\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		Csrf csrfTipoObrigatorio = obterCsrf(proprietario.sessao());
		mockMvc.perform(post("/api/v1/tarefas")
					.session(csrfTipoObrigatorio.sessao())
					.header(csrfTipoObrigatorio.header(), csrfTipoObrigatorio.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"titulo\":\"Sem tipo\",\"prazo\":\"2026-12-10\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		Csrf csrfPrioridadeInvalida = obterCsrf(proprietario.sessao());
		mockMvc.perform(post("/api/v1/tarefas")
					.session(csrfPrioridadeInvalida.sessao())
					.header(csrfPrioridadeInvalida.header(), csrfPrioridadeInvalida.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"tipo\":\"INDIVIDUAL\",\"titulo\":\"Prioridade inválida\","
							+ "\"prazo\":\"2026-12-10\",\"prioridade\":\"URGENTE\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

		Csrf csrfDataInvalida = obterCsrf(proprietario.sessao());
		mockMvc.perform(post("/api/v1/tarefas")
					.session(csrfDataInvalida.sessao())
					.header(csrfDataInvalida.header(), csrfDataInvalida.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"tipo\":\"INDIVIDUAL\",\"titulo\":\"Data inválida\","
							+ "\"prazo\":\"2026-02-30\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

		Csrf csrfPrioridadeEdicao = obterCsrf(proprietario.sessao());
		mockMvc.perform(put("/api/v1/tarefas/{id}", tarefaPropria)
					.session(csrfPrioridadeEdicao.sessao())
					.header(csrfPrioridadeEdicao.header(), csrfPrioridadeEdicao.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaAtualizarRequest(
							"Sem prioridade", null, null, LocalDate.of(2026, 12, 10), null))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		Csrf csrfEstadoObrigatorio = obterCsrf(proprietario.sessao());
		mockMvc.perform(patch("/api/v1/tarefas/{id}/estado", tarefaPropria)
					.session(csrfEstadoObrigatorio.sessao())
					.header(csrfEstadoObrigatorio.header(), csrfEstadoObrigatorio.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"estado\":null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		criarTarefa(proprietario, new TarefaCriarRequest(
				TipoTarefa.INDIVIDUAL, "Atividade atrasada", null, null,
				LocalDate.of(2020, 1, 1), PrioridadeTarefa.BAIXA));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("prazoInicio", "2026-12-20")
					.param("prazoFim", "2026-12-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_TASK_FILTER"));

		mockMvc.perform(get("/api/v1/tarefas")
					.session(proprietario.sessao())
					.param("prioridade", "URGENTE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_QUERY_PARAMETER"));
	}

	@Test
	void endpointsDeTarefasExigemAutenticacao() throws Exception {
		mockMvc.perform(get("/api/v1/tarefas"))
				.andExpect(status().isUnauthorized());
	}

	private long criarDisciplina(SessaoUsuario usuario, String nome) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		MvcResult result = mockMvc.perform(post("/api/v1/disciplinas")
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new DisciplinaRequest(nome))))
				.andExpect(status().isCreated())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private long criarTarefa(SessaoUsuario usuario, TarefaCriarRequest request) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		MvcResult result = mockMvc.perform(post("/api/v1/tarefas")
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private void alterarEstado(SessaoUsuario usuario, long tarefaId, EstadoTarefa estado) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		mockMvc.perform(patch("/api/v1/tarefas/{id}/estado", tarefaId)
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new TarefaEstadoRequest(estado))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value(estado.name()));
	}

	private TarefaCriarRequest tarefaBasica(String titulo) {
		return new TarefaCriarRequest(
				TipoTarefa.INDIVIDUAL, titulo, null, null,
				LocalDate.of(2026, 12, 10), PrioridadeTarefa.MEDIA);
	}

	private SessaoUsuario criarUsuarioAutenticado() throws Exception {
		String email = "tarefa-" + UUID.randomUUID() + "@example.edu";
		Csrf csrfCadastro = obterCsrf(null);
		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrfCadastro.sessao())
					.header(csrfCadastro.header(), csrfCadastro.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(
							new CadastroUsuarioRequest("Estudante", email, SENHA))))
				.andExpect(status().isCreated());

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
					.session(csrfCadastro.sessao())
					.header(csrfCadastro.header(), csrfCadastro.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new LoginRequest(email, SENHA))))
				.andExpect(status().isOk())
				.andReturn();
		return new SessaoUsuario((MockHttpSession) login.getRequest().getSession(false));
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

	private record SessaoUsuario(MockHttpSession sessao) {
	}

	private record Csrf(MockHttpSession sessao, String header, String token) {
	}
}
