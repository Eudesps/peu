package br.com.organizacaoacademica.tarefa.service;

import br.com.organizacaoacademica.disciplina.entity.Disciplina;
import br.com.organizacaoacademica.disciplina.repository.DisciplinaRepository;
import br.com.organizacaoacademica.tarefa.dto.TarefaAtualizarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaCriarRequest;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;
import br.com.organizacaoacademica.tarefa.repository.TarefaRepository;
import br.com.organizacaoacademica.tarefa.repository.ConviteTarefaRepository;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceValidacaoTests {

	@Mock
	private TarefaRepository tarefaRepository;

	@Mock
	private DisciplinaRepository disciplinaRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private ConviteTarefaRepository conviteTarefaRepository;

	@InjectMocks
	private TarefaService tarefaService;

	@Test
	void criarRejeitaTipoTituloOuPrazoAusentesEExcedeLimiteDoTitulo() {
		LocalDate prazo = LocalDate.of(2026, 12, 10);

		assertThatThrownBy(() -> tarefaService.criar("aluno@example.edu",
				new TarefaCriarRequest(null, "Título", null, null, prazo, PrioridadeTarefa.MEDIA)))
				.isInstanceOf(TarefaDadosInvalidosException.class);
		assertThatThrownBy(() -> tarefaService.criar("aluno@example.edu",
				new TarefaCriarRequest(TipoTarefa.INDIVIDUAL, "   ", null, null, prazo, PrioridadeTarefa.MEDIA)))
				.isInstanceOf(TarefaDadosInvalidosException.class);
		assertThatThrownBy(() -> tarefaService.criar("aluno@example.edu",
				new TarefaCriarRequest(TipoTarefa.INDIVIDUAL, "T".repeat(161), null, null, prazo,
						PrioridadeTarefa.MEDIA)))
				.isInstanceOf(TarefaDadosInvalidosException.class);
		assertThatThrownBy(() -> tarefaService.criar("aluno@example.edu",
				new TarefaCriarRequest(TipoTarefa.INDIVIDUAL, "Título", null, null, null,
						PrioridadeTarefa.MEDIA)))
				.isInstanceOf(TarefaDadosInvalidosException.class);

		verifyNoInteractions(usuarioRepository, disciplinaRepository, tarefaRepository);
	}

	@Test
	void editarRejeitaPrioridadeAusenteAntesDeAlterarDisciplinaOuTarefa() {
		Tarefa tarefa = prepararTarefa(EstadoTarefa.ABERTA);
		TarefaAtualizarRequest request = new TarefaAtualizarRequest(
				"Título", null, null, LocalDate.of(2026, 12, 10), null);

		assertThatThrownBy(() -> tarefaService.editar("aluno@example.edu", 25L, request))
				.isInstanceOf(TarefaDadosInvalidosException.class);

		verify(tarefa, never()).alterarDadosCentrais(any(), any(), any(), any(), any());
		verifyNoInteractions(disciplinaRepository);
	}

	@Test
	void rejeitaEstadoNuloETrataRepeticaoDoEstadoAtualComoOperacaoSemEfeito() {
		Tarefa tarefa = prepararTarefa(EstadoTarefa.ABERTA);

		assertThatThrownBy(() -> tarefaService.alterarEstado("aluno@example.edu", 25L, null))
				.isInstanceOf(TarefaDadosInvalidosException.class);
		tarefaService.alterarEstado("aluno@example.edu", 25L, EstadoTarefa.ABERTA);

		verify(tarefa, never()).alterarEstado(any());
		verify(tarefaRepository, never()).flush();
	}

	private Tarefa prepararTarefa(EstadoTarefa estado) {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(12L);
		when(usuarioRepository.findByEmailIgnoreCase("aluno@example.edu"))
				.thenReturn(Optional.of(usuario));

		Tarefa tarefa = mock(Tarefa.class);
		when(tarefa.getEstado()).thenReturn(estado);
		when(tarefaRepository.findByIdAndCriador_Id(25L, 12L)).thenReturn(Optional.of(tarefa));
		return tarefa;
	}
}
