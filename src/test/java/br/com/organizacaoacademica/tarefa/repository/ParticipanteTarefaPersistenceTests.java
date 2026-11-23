package br.com.organizacaoacademica.tarefa.repository;

import br.com.organizacaoacademica.tarefa.entity.ParticipanteTarefa;
import br.com.organizacaoacademica.tarefa.entity.ParticipanteTarefaId;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParticipanteTarefaPersistenceTests {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private TarefaRepository tarefaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void persisteVinculoAtivoComChaveCompostaERelacionamentos() {
		Usuario criador = criarUsuario("criador");
		Usuario participante = criarUsuario("participante");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.COMPARTILHADA);
		ParticipanteTarefa vinculo = new ParticipanteTarefa(tarefa, participante);

		entityManager.persist(vinculo);
		entityManager.flush();
		ParticipanteTarefaId id = vinculo.getId();
		entityManager.clear();

		ParticipanteTarefa carregado = entityManager.find(ParticipanteTarefa.class, id);
		assertThat(carregado).isNotNull();
		assertThat(carregado.getTarefa().getId()).isEqualTo(tarefa.getId());
		assertThat(carregado.getUsuario().getId()).isEqualTo(participante.getId());
		assertThat(carregado.getAdicionadoEm()).isNotNull();
	}

	@Test
	void chaveCompostaImpedeVincularMesmoUsuarioDuasVezesNaMesmaTarefa() {
		Usuario criador = criarUsuario("criador-duplicidade");
		Usuario participante = criarUsuario("participante-duplicidade");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.COMPARTILHADA);
		entityManager.persist(new ParticipanteTarefa(tarefa, participante));
		entityManager.flush();

		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO participante_tarefa (tarefa_id, usuario_id) VALUES (?, ?)",
				tarefa.getId(), participante.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void chavesEstrangeirasExigemTarefaExistente() {
		Usuario participante = criarUsuario("tarefa-inexistente");

		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO participante_tarefa (tarefa_id, usuario_id) VALUES (?, ?)",
				Long.MAX_VALUE, participante.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void chavesEstrangeirasExigemUsuarioExistente() {
		Usuario criador = criarUsuario("usuario-inexistente");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.COMPARTILHADA);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"INSERT INTO participante_tarefa (tarefa_id, usuario_id) VALUES (?, ?)",
				tarefa.getId(), Long.MAX_VALUE))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void excluirTarefaRemoveSeusVinculosAtivos() {
		Usuario criador = criarUsuario("exclusao-tarefa-criador");
		Usuario participante = criarUsuario("exclusao-tarefa-participante");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.COMPARTILHADA);
		entityManager.persist(new ParticipanteTarefa(tarefa, participante));
		entityManager.flush();

		jdbcTemplate.update("DELETE FROM tarefa WHERE id = ?", tarefa.getId());

		Integer quantidadeVinculos = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM participante_tarefa WHERE tarefa_id = ?", Integer.class, tarefa.getId());
		assertThat(quantidadeVinculos).isZero();
	}

	@Test
	void impedeExcluirUsuarioQueAindaTemParticipacaoAtiva() {
		Usuario criador = criarUsuario("exclusao-usuario-criador");
		Usuario participante = criarUsuario("exclusao-usuario-participante");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.COMPARTILHADA);
		entityManager.persist(new ParticipanteTarefa(tarefa, participante));
		entityManager.flush();

		assertThatThrownBy(() -> jdbcTemplate.update(
				"DELETE FROM usuario WHERE id = ?", participante.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejeitaParticipanteIgualAoCriadorDaTarefa() {
		Usuario criador = criarUsuario("criador-nao-participante");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.COMPARTILHADA);

		assertThatThrownBy(() -> new ParticipanteTarefa(tarefa, criador))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejeitaAssociarParticipanteATarefaIndividual() {
		Usuario criador = criarUsuario("individual-criador");
		Usuario participante = criarUsuario("individual-participante");
		Tarefa tarefa = criarTarefa(criador, TipoTarefa.INDIVIDUAL);

		assertThatThrownBy(() -> new ParticipanteTarefa(tarefa, participante))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private Usuario criarUsuario(String marcador) {
		return usuarioRepository.saveAndFlush(new Usuario(
				"Estudante " + marcador,
				"aluno-" + UUID.randomUUID() + "@example.edu",
				"encoded-hash"
		));
	}

	private Tarefa criarTarefa(Usuario criador, TipoTarefa tipo) {
		return tarefaRepository.saveAndFlush(new Tarefa(
				criador,
				tipo,
				"Trabalho em grupo",
				null,
				null,
				LocalDate.of(2026, 12, 10),
				PrioridadeTarefa.MEDIA
		));
	}
}
