package br.com.organizacaoacademica.usuario.controller;

import br.com.organizacaoacademica.usuario.dto.CadastroUsuarioRequest;
import br.com.organizacaoacademica.usuario.dto.LoginRequest;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void cadastraAutenticaConsultaSessaoEEncerraSessao() throws Exception {
		String email = emailUnico();
		String senha = "senha-forte-para-teste";
		Csrf csrf = obterCsrf();

		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrf.session())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new CadastroUsuarioRequest(" Estudante ", email, senha))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.senhaHash").doesNotExist());

		var usuarioSalvo = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
		assertThat(usuarioSalvo.getNome()).isEqualTo("Estudante");
		assertThat(usuarioSalvo.getSenhaHash()).isNotEqualTo(senha);
		assertThat(passwordEncoder.matches(senha, usuarioSalvo.getSenhaHash())).isTrue();

		String idSessaoAntesDoLogin = csrf.session().getId();
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
					.session(csrf.session())
					.header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new LoginRequest(email, senha))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andReturn();

		MockHttpSession sessaoAutenticada = (MockHttpSession) login.getRequest().getSession(false);
		assertThat(sessaoAutenticada).isNotNull();
		assertThat(sessaoAutenticada.getId()).isNotEqualTo(idSessaoAntesDoLogin);

		mockMvc.perform(get("/api/v1/auth/me").session(sessaoAutenticada))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));

		Csrf csrfLogout = obterCsrf(sessaoAutenticada);
		mockMvc.perform(post("/api/v1/auth/logout")
					.session(sessaoAutenticada)
					.header(csrfLogout.header(), csrfLogout.token()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/auth/me").session(sessaoAutenticada))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@Test
	void naoAceitaEmailDuplicadoMesmoComVariacaoDeCaixa() throws Exception {
		String email = emailUnico();
		Csrf csrf = obterCsrf();
		String conteudo = jsonMapper.writeValueAsString(new CadastroUsuarioRequest(
				"Primeira conta", email, "senha-forte-para-teste"));

		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON).content(conteudo))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new CadastroUsuarioRequest(
							"Segunda conta", email.toUpperCase(), "outra-senha-valida"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
	}

	@Test
	void rejeitaSenhaQueExcedeLimiteBcryptEmBytes() throws Exception {
		Csrf csrf = obterCsrf();
		CadastroUsuarioRequest request = new CadastroUsuarioRequest(
				"Estudante", emailUnico(), "á".repeat(37));

		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));

		CadastroUsuarioRequest curtaEmCaracteresUnicode = new CadastroUsuarioRequest(
				"Estudante", emailUnico(), "😀".repeat(6));
		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(curtaEmCaracteresUnicode)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
	}

	@Test
	void naoRevelaSeEmailExisteEExigeCsrfParaLogin() throws Exception {
		String email = emailUnico();
		Csrf csrf = obterCsrf();
		String senha = "senha-forte-para-teste";
		mockMvc.perform(post("/api/v1/auth/register")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new CadastroUsuarioRequest("Estudante", email, senha))))
				.andExpect(status().isCreated());

		String respostaSenhaErrada = mockMvc.perform(post("/api/v1/auth/login")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new LoginRequest(email, "senha-incorreta"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andReturn().getResponse().getContentAsString();

		String respostaContaInexistente = mockMvc.perform(post("/api/v1/auth/login")
					.session(csrf.session()).header(csrf.header(), csrf.token())
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new LoginRequest(emailUnico(), senha))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andReturn().getResponse().getContentAsString();

		JsonNode erroSenhaErrada = jsonMapper.readTree(respostaSenhaErrada);
		JsonNode erroContaInexistente = jsonMapper.readTree(respostaContaInexistente);
		assertThat(erroSenhaErrada.get("message").asText())
				.isEqualTo(erroContaInexistente.get("message").asText());

		mockMvc.perform(post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(new LoginRequest(email, senha))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	void protegeDadosDeSessaoParaVisitante() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	private Csrf obterCsrf() throws Exception {
		return obterCsrf(null);
	}

	private Csrf obterCsrf(MockHttpSession session) throws Exception {
		var request = get("/api/v1/auth/csrf");
		if (session != null) {
			request.session(session);
		}
		MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
		JsonNode json = jsonMapper.readTree(result.getResponse().getContentAsString());
		MockHttpSession csrfSession = (MockHttpSession) result.getRequest().getSession(false);
		return new Csrf(csrfSession, json.get("headerName").asText(), json.get("token").asText());
	}

	private static String emailUnico() {
		return "estudante-" + UUID.randomUUID() + "@example.edu";
	}

	private record Csrf(MockHttpSession session, String header, String token) {
	}
}
