package br.com.organizacaoacademica.usuario.repository;

import br.com.organizacaoacademica.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistência e consulta de contas pelo identificador de autenticação (e-mail). */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmailIgnoreCase(String email);
}
