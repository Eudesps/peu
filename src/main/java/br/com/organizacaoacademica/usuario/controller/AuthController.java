package br.com.organizacaoacademica.usuario.controller;

import br.com.organizacaoacademica.usuario.dto.CadastroUsuarioRequest;
import br.com.organizacaoacademica.usuario.dto.LoginRequest;
import br.com.organizacaoacademica.usuario.dto.UsuarioAutenticadoResponse;
import br.com.organizacaoacademica.usuario.service.UsuarioAutenticacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UsuarioAutenticacaoService usuarioAutenticacaoService;
	private final SecurityContextRepository securityContextRepository;
	private final CsrfTokenRepository csrfTokenRepository;

	public AuthController(
			UsuarioAutenticacaoService usuarioAutenticacaoService,
			SecurityContextRepository securityContextRepository,
			CsrfTokenRepository csrfTokenRepository
	) {
		this.usuarioAutenticacaoService = usuarioAutenticacaoService;
		this.securityContextRepository = securityContextRepository;
		this.csrfTokenRepository = csrfTokenRepository;
	}

	@GetMapping("/csrf")
	public ResponseEntity<CsrfToken> csrfToken(CsrfToken csrfToken) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(csrfToken);
	}

	@PostMapping("/register")
	public ResponseEntity<UsuarioAutenticadoResponse> cadastrar(
			@Valid @RequestBody CadastroUsuarioRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(usuarioAutenticacaoService.cadastrar(request));
	}

	@PostMapping("/login")
	public UsuarioAutenticadoResponse login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		Authentication authentication = usuarioAutenticacaoService.autenticar(request.email(), request.senha());

		// O endpoint de login exige CSRF e, portanto, normalmente já tem uma sessão para rotacionar.
		if (httpRequest.getSession(false) != null) {
			httpRequest.changeSessionId();
		}

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);
		csrfTokenRepository.saveToken(null, httpRequest, httpResponse);

		return usuarioAutenticacaoService.buscarPerfil(authentication.getName());
	}

	@GetMapping("/me")
	public UsuarioAutenticadoResponse usuarioAtual(Authentication authentication) {
		return usuarioAutenticacaoService.buscarPerfil(authentication.getName());
	}
}
