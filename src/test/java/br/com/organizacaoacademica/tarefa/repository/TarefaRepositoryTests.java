package br.com.organizacaoacademica.tarefa.repository;

import br.com.organizacaoacademica.disciplina.entity.Disciplina;
import br.com.organizacaoacademica.disciplina.repository.DisciplinaRepository;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
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
class TarefaRepositoryTests {

	@Autowired
	private TarefaRepository tarefaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void persisteTarefaIndividualSemDisciplinaComValoresPadrao() {
		Usuario criador = criarUsuario("individual");
		Tarefa tarefa = new Tarefa(
				criador,
				TipoTarefa.INDIVIDUAL,
				"  Estudar cálculo  ",
				null,
				null,
				LocalDate.of(2026, 12, 10),
				null
		);

		Tarefa salva = tarefaRepository.saveAndFlush(tarefa);
		Tarefa recarregada = tarefaRepository.findById(salva.getId()).orElseThrow();

		assertThat(recarregada.getTipo()).isEqualTo(TipoTarefa.INDIVIDUAL);
		assertThat(recarregada.getCriador().getId()).isEqualTo(criador.getId());
		assertThat(recarregada.getDisciplina()).isNull();
		assertThat(recarregada.getTitulo()).isEqualTo("Estudar cálculo");
		assertThat(recarregada.getDescricao()).isNull();
		assertThat(recarregada.getPrazo()).isEqualTo(LocalDate.of(2026, 12, 10));
		assertThat(recarregada.getPrioridade()).isEqualTo(PrioridadeTarefa.MEDIA);
		assertThat(recarregada.getEstado()).isEqualTo(EstadoTarefa.ABERTA);
		assertThat(recarregada.getCriadaEm()).isNotNull();
		assertThat(recarregada.getAtualizadaEm()).isNotNull();
	}

	@Test
	void persisteTarefaCompartilhadaComDisciplinaDoCriador() {
		Usuario criador = criarUsuario("compartilhada");
		Disciplina disciplina = disciplinaRepository.saveAndFlush(new Disciplina(criador, "Banco de Dados"));
		Tarefa tarefa = new Tarefa(
				criador,
				TipoTarefa.COMPARTILHADA,
				"Trabalho de modelagem",
				"  Revisar o diagrama ER  ",
				disciplina,
				LocalDate.of(2026, 12, 15),
				PrioridadeTarefa.ALTA
		);

		Tarefa salva = tarefaRepository.saveAndFlush(tarefa);
		Tarefa recarregada = tarefaRepository.findById(salva.getId()).orElseThrow();

		assertThat(recarregada.getTipo()).isEqualTo(TipoTarefa.COMPARTILHADA);
		assertThat(recarregada.getDisciplina().getId()).isEqualTo(disciplina.getId());
		assertThat(recarregada.getDescricao()).isEqualTo("Revisar o diagrama ER");
		assertThat(recarregada.getPrioridade()).isEqualTo(PrioridadeTarefa.ALTA);
	}

	@Test
	void impedeAssociarDisciplinaDeOutroUsuario() {
		Usuario criador = criarUsuario("owner");
		Usuario outroUsuario = criarUsuario("other");
		Disciplina disciplinaAlheia = disciplinaRepository.saveAndFlush(
				new Disciplina(outroUsuario, "Disciplina alheia")
		);
		Tarefa tarefa = new Tarefa(
				criador,
				TipoTarefa.INDIVIDUAL,
				"Tarefa inválida",
				null,
				disciplinaAlheia,
				LocalDate.of(2026, 12, 20),
				PrioridadeTarefa.BAIXA
		);

		assertThatThrownBy(() -> tarefaRepository.saveAndFlush(tarefa))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void listaEConsultaSomenteNoEscopoDoCriador() {
		Usuario criador = criarUsuario("scope");
		Usuario outraPessoa = criarUsuario("separate");
		Tarefa primeira = salvarTarefa(criador, "Mais tarde", LocalDate.of(2026, 12, 20));
		Tarefa segunda = salvarTarefa(criador, "Mais cedo", LocalDate.of(2026, 12, 5));
		Tarefa deOutraPessoa = salvarTarefa(outraPessoa, "Privada", LocalDate.of(2026, 12, 1));

		assertThat(tarefaRepository.findAllByCriador_IdOrderByPrazoAsc(criador.getId()))
				.extracting(Tarefa::getId)
				.containsExactly(segunda.getId(), primeira.getId());
		assertThat(tarefaRepository.findByIdAndCriador_Id(deOutraPessoa.getId(), criador.getId()))
				.isEmpty();
	}

	@Test
	void rejeitaTituloVazioDiretamenteNoBanco() {
		Usuario criador = criarUsuario("blank-title");

		assertThatThrownBy(() -> inserirDireto(criador.getId(), "   ", "INDIVIDUAL", "MEDIA", "ABERTA"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejeitaTituloMaiorQueLimiteDoModelo() {
		Usuario criador = criarUsuario("long-title");

		assertThatThrownBy(() -> inserirDireto(
				criador.getId(), "T".repeat(161), "INDIVIDUAL", "MEDIA", "ABERTA"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejeitaTipoInvalidoDiretamenteNoBanco() {
		Usuario criador = criarUsuario("invalid-type");

		assertThatThrownBy(() -> inserirDireto(criador.getId(), "Título válido", "GRUPO", "MEDIA", "ABERTA"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejeitaPrioridadeInvalidaDiretamenteNoBanco() {
		Usuario criador = criarUsuario("invalid-priority");

		assertThatThrownBy(() -> inserirDireto(criador.getId(), "Título válido", "INDIVIDUAL", "URGENTE", "ABERTA"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejeitaEstadoInvalidoDiretamenteNoBanco() {
		Usuario criador = criarUsuario("invalid-state");

		assertThatThrownBy(() -> inserirDireto(criador.getId(), "Título válido", "INDIVIDUAL", "MEDIA", "EM_ANDAMENTO"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Usuario criarUsuario(String marcador) {
		return usuarioRepository.saveAndFlush(new Usuario(
				"Estudante",
				"tarefa-" + marcador + "-" + UUID.randomUUID() + "@example.edu",
				"encoded-hash"
		));
	}

	private Tarefa salvarTarefa(Usuario criador, String titulo, LocalDate prazo) {
		return tarefaRepository.saveAndFlush(new Tarefa(
				criador,
				TipoTarefa.INDIVIDUAL,
				titulo,
				null,
				null,
				prazo,
				PrioridadeTarefa.MEDIA
		));
	}

	private void inserirDireto(
			Long criadorId,
			String titulo,
			String tipo,
			String prioridade,
			String estado
	) {
		jdbcTemplate.update(
				"INSERT INTO tarefa (criador_id, tipo, titulo, prazo, prioridade, estado) " +
						"VALUES (?, ?, ?, ?, ?, ?)",
				criadorId,
				tipo,
				titulo,
				LocalDate.of(2026, 12, 30),
				prioridade,
				estado
		);
	}
}
