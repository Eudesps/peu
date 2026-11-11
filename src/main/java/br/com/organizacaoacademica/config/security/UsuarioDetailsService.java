package br.com.organizacaoacademica.config.security;

import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/** Adapta a conta persistida ao modelo de autenticação por usuário e senha do Spring Security. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada."));

		return User.withUsername(usuario.getEmail())
				.password(usuario.getSenhaHash())
				.authorities(List.of())
				.build();
	}
}
