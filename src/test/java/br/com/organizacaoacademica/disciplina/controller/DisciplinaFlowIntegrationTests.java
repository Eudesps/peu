package br.com.organizacaoacademica.disciplina.controller;

import br.com.organizacaoacademica.disciplina.dto.DisciplinaRequest;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DisciplinaFlowIntegrationTests {

	private static final String SENHA = "senha-forte-para-disciplina";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void usuarioPodeCriarListarConsultarEditarEExcluirSuaDisciplina() throws Exception {
		SessaoUsuario usuario = criarUsuarioAutenticado();
		JsonNode criada = criarDisciplina(usuario, "  Matemática  ");
		long id = criada.get("id").asLong();
		String criadaEm = criada.get("criadaEm").asText();

		assertThat(criada.get("nome").asText()).isEqualTo("Matemática");
		mockMvc.perform(get("/api/v1/disciplinas").session(usuario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(id));

		mockMvc.perform(get("/api/v1/disciplinas/{id}", id).session(usuario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Matemática"));

		Csrf csrfEdicao = obterCsrf(usuario.sessao());
		mockMvc.perform(put("/api/v1/disciplinas/{id}", id)
					.session(csrfEdicao.sessao())
					.header(csrfEdicao.header(), csrfEdicao.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new DisciplinaRequest("Física"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Física"))
				.andExpect(jsonPath("$.criadaEm").value(criadaEm));

		Csrf csrfExclusao = obterCsrf(usuario.sessao());
		mockMvc.perform(delete("/api/v1/disciplinas/{id}", id)
					.session(csrfExclusao.sessao())
					.header(csrfExclusao.header(), csrfExclusao.token()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/disciplinas/{id}", id).session(usuario.sessao()))
				.andExpect(status().isNotFound());
	}

	@Test
	void usuarioNaoListaNemConsultaDisciplinaDeOutraPessoa() throws Exception {
		SessaoUsuario proprietario = criarUsuarioAutenticado();
		SessaoUsuario outraPessoa = criarUsuarioAutenticado();
		long id = criarDisciplina(proprietario, "Biologia").get("id").asLong();

		mockMvc.perform(get("/api/v1/disciplinas").session(outraPessoa.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/api/v1/disciplinas/{id}", id).session(outraPessoa.sessao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("DISCIPLINA_NOT_FOUND"));

		mockMvc.perform(get("/api/v1/disciplinas/{id}", id).session(proprietario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Biologia"));
	}

	@Test
	void usuarioNaoEditaNemExcluiDisciplinaDeOutraPessoa() throws Exception {
		SessaoUsuario proprietario = criarUsuarioAutenticado();
		SessaoUsuario outraPessoa = criarUsuarioAutenticado();
		long id = criarDisciplina(proprietario, "História").get("id").asLong();

		Csrf csrfEdicao = obterCsrf(outraPessoa.sessao());
		mockMvc.perform(put("/api/v1/disciplinas/{id}", id)
					.session(csrfEdicao.sessao())
					.header(csrfEdicao.header(), csrfEdicao.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new DisciplinaRequest("Geografia"))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("DISCIPLINA_NOT_FOUND"));

		Csrf csrfExclusao = obterCsrf(outraPessoa.sessao());
		mockMvc.perform(delete("/api/v1/disciplinas/{id}", id)
					.session(csrfExclusao.sessao())
					.header(csrfExclusao.header(), csrfExclusao.token()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("DISCIPLINA_NOT_FOUND"));

		mockMvc.perform(get("/api/v1/disciplinas/{id}", id).session(proprietario.sessao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("História"));
	}

	private JsonNode criarDisciplina(SessaoUsuario usuario, String nome) throws Exception {
		Csrf csrf = obterCsrf(usuario.sessao());
		MvcResult result = mockMvc.perform(post("/api/v1/disciplinas")
					.session(csrf.sessao())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new DisciplinaRequest(nome))))
				.andExpect(status().isCreated())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString());
	}

	private SessaoUsuario criarUsuarioAutenticado() throws Exception {
		String email = "disciplina-" + UUID.randomUUID() + "@example.edu";
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
