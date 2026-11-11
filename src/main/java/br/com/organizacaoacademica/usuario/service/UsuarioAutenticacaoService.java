package br.com.organizacaoacademica.usuario.service;

import br.com.organizacaoacademica.usuario.dto.CadastroUsuarioRequest;
import br.com.organizacaoacademica.usuario.dto.UsuarioAutenticadoResponse;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UsuarioAutenticacaoService {

	private static final int MAXIMO_BYTES_SENHA_BCRYPT = 72;

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

	public UsuarioAutenticacaoService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager
	) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public UsuarioAutenticadoResponse cadastrar(CadastroUsuarioRequest request) {
		String email = normalizarEmail(request.email());
		if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new EmailJaCadastradoException();
		}

		validarTamanhoEmBytes(request.senha());
		Usuario usuario = new Usuario(request.nome(), email, passwordEncoder.encode(request.senha()));
		try {
			return paraResposta(usuarioRepository.saveAndFlush(usuario));
		} catch (DataIntegrityViolationException exception) {
			// O índice único continua protegendo contra duas inscrições concorrentes.
			throw new EmailJaCadastradoException();
		}
	}

	public Authentication autenticar(String email, String senha) {
		try {
			return authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
					normalizarEmail(email), senha));
		} catch (AuthenticationException exception) {
			// A resposta não informa se a conta existe.
			throw new CredenciaisInvalidasException();
		}
	}

	@Transactional(readOnly = true)
	public UsuarioAutenticadoResponse buscarPerfil(String email) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email))
				.orElseThrow(() -> new IllegalStateException("A conta autenticada não foi encontrada."));
		return paraResposta(usuario);
	}

	private static void validarTamanhoEmBytes(String senha) {
		int quantidadeCaracteres = senha.codePointCount(0, senha.length());
		int quantidadeBytes = senha.getBytes(StandardCharsets.UTF_8).length;
		if (quantidadeCaracteres < 12 || quantidadeBytes > MAXIMO_BYTES_SENHA_BCRYPT) {
			throw new SenhaForaDaPoliticaException();
		}
	}

	private static String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static UsuarioAutenticadoResponse paraResposta(Usuario usuario) {
		return new UsuarioAutenticadoResponse(
				usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCriadoEm());
	}
}
