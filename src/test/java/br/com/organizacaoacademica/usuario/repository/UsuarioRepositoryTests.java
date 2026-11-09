package br.com.organizacaoacademica.usuario.repository;

import br.com.organizacaoacademica.usuario.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTests {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	void persisteUsuarioComEmailNormalizadoEPermiteBuscaSemDiferenciarCaixa() {
		Usuario usuario = new Usuario(
				"  Ana Silva  ",
				"  Ana.Silva@example.edu  ",
				"encoded-password-hash"
		);

		Usuario salvo = usuarioRepository.saveAndFlush(usuario);
		Usuario encontrado = usuarioRepository.findByEmailIgnoreCase("ANA.SILVA@EXAMPLE.EDU")
				.orElseThrow();

		assertThat(salvo.getId()).isNotNull();
		assertThat(salvo.getNome()).isEqualTo("Ana Silva");
		assertThat(salvo.getEmail()).isEqualTo("ana.silva@example.edu");
		assertThat(salvo.getCriadoEm()).isNotNull();
		assertThat(encontrado.getId()).isEqualTo(salvo.getId());
	}

	@Test
	void indiceDoBancoImpedeEmailDuplicadoIgnorandoCaixaEEspacos() {
		String email = "aluno-" + UUID.randomUUID() + "@example.edu";
		usuarioRepository.saveAndFlush(new Usuario("Primeiro", email, "encoded-hash"));

		Usuario duplicado = new Usuario(
				"Segundo",
				"  " + email.toUpperCase(Locale.ROOT) + "  ",
				"another-encoded-hash"
		);

		assertThatThrownBy(() -> usuarioRepository.saveAndFlush(duplicado))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
