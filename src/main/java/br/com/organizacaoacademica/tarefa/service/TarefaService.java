package br.com.organizacaoacademica.tarefa.service;

import br.com.organizacaoacademica.disciplina.entity.Disciplina;
import br.com.organizacaoacademica.disciplina.repository.DisciplinaRepository;
import br.com.organizacaoacademica.disciplina.service.DisciplinaNaoEncontradaException;
import br.com.organizacaoacademica.tarefa.OrdenacaoTarefa;
import br.com.organizacaoacademica.tarefa.dto.TarefaAtualizarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaCriarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaResponse;
import br.com.organizacaoacademica.tarefa.entity.ConviteTarefa;
import br.com.organizacaoacademica.tarefa.entity.EstadoConvite;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import br.com.organizacaoacademica.tarefa.repository.TarefaRepository;
import br.com.organizacaoacademica.tarefa.repository.ConviteTarefaRepository;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class TarefaService {

	private final TarefaRepository tarefaRepository;
	private final DisciplinaRepository disciplinaRepository;
	private final UsuarioRepository usuarioRepository;
	private final ConviteTarefaRepository conviteTarefaRepository;

	public TarefaService(
			TarefaRepository tarefaRepository,
			DisciplinaRepository disciplinaRepository,
			UsuarioRepository usuarioRepository,
			ConviteTarefaRepository conviteTarefaRepository
	) {
		this.tarefaRepository = tarefaRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.usuarioRepository = usuarioRepository;
		this.conviteTarefaRepository = conviteTarefaRepository;
	}

	@Transactional(readOnly = true)
	public List<TarefaResponse> listar(
			String emailAutenticado,
			Long disciplinaId,
			LocalDate prazoInicio,
			LocalDate prazoFim,
			PrioridadeTarefa prioridade,
			EstadoTarefa estado,
			OrdenacaoTarefa ordenarPor,
			Sort.Direction direcao
	) {
		if (prazoInicio != null && prazoFim != null && prazoInicio.isAfter(prazoFim)
				|| ordenarPor == null || direcao == null) {
			throw new FiltroTarefaInvalidoException();
		}
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		return tarefaRepository.buscarComFiltrosAcessiveisAoUsuario(
				usuarioId, disciplinaId, prazoInicio, prazoFim, prioridade, estado, ordenarPor, direcao)
				.stream()
				.map(TarefaService::paraResposta)
				.toList();
	}

	@Transactional(readOnly = true)
	public TarefaResponse buscar(String emailAutenticado, Long tarefaId) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		Tarefa tarefa = tarefaRepository.findByIdAcessivelAoUsuario(tarefaId, usuarioId)
				.orElseThrow(TarefaNaoEncontradaException::new);
		return paraResposta(tarefa);
	}

	@Transactional
	public TarefaResponse criar(String emailAutenticado, TarefaCriarRequest request) {
		validarCriacao(request);
		Usuario criador = buscarUsuario(emailAutenticado);
		Disciplina disciplina = buscarDisciplinaDoCriador(request.disciplinaId(), criador.getId());
		Tarefa tarefa = new Tarefa(
				criador,
				request.tipo(),
				request.titulo(),
				request.descricao(),
				disciplina,
				request.prazo(),
				request.prioridade()
		);
		return paraResposta(tarefaRepository.saveAndFlush(tarefa));
	}

	@Transactional
	public TarefaResponse editar(String emailAutenticado, Long tarefaId, TarefaAtualizarRequest request) {
		Long criadorId = buscarUsuario(emailAutenticado).getId();
		Tarefa tarefa = buscarTarefaDoCriador(tarefaId, criadorId);
		if (tarefa.getEstado() == EstadoTarefa.CONCLUIDA) {
			throw new TarefaConcluidaException();
		}
		validarEdicao(request);
		Disciplina disciplina = buscarDisciplinaDoCriador(request.disciplinaId(), criadorId);
		tarefa.alterarDadosCentrais(
				request.titulo(),
				request.descricao(),
				disciplina,
				request.prazo(),
				request.prioridade()
		);
		tarefaRepository.flush();
		return paraResposta(tarefa);
	}

	@Transactional
	public TarefaResponse alterarEstado(String emailAutenticado, Long tarefaId, EstadoTarefa estado) {
		Long criadorId = buscarUsuario(emailAutenticado).getId();
		Tarefa tarefa = buscarTarefaDoCriador(tarefaId, criadorId);
		validarTransicao(tarefa.getEstado(), estado);
		if (tarefa.getEstado() == estado) {
			return paraResposta(tarefa);
		}
		tarefa.alterarEstado(estado);
		if (estado == EstadoTarefa.CONCLUIDA) {
			conviteTarefaRepository.findAllByTarefa_IdAndEstado(tarefaId, EstadoConvite.PENDENTE)
					.forEach(ConviteTarefa::cancelar);
		}
		tarefaRepository.flush();
		return paraResposta(tarefa);
	}

	@Transactional
	public void excluir(String emailAutenticado, Long tarefaId) {
		Long criadorId = buscarUsuario(emailAutenticado).getId();
		tarefaRepository.delete(buscarTarefaDoCriador(tarefaId, criadorId));
		tarefaRepository.flush();
	}

	private Usuario buscarUsuario(String emailAutenticado) {
		String emailNormalizado = emailAutenticado.trim().toLowerCase(Locale.ROOT);
		return usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.orElseThrow(() -> new IllegalStateException("A conta autenticada não foi encontrada."));
	}

	private static void validarCriacao(TarefaCriarRequest request) {
		if (request == null || request.tipo() == null) {
			throw new TarefaDadosInvalidosException();
		}
		validarTituloEPrazo(request.titulo(), request.prazo());
		// Prioridade nula na criação é válida: a entidade assume MEDIA como padrão.
	}

	private static void validarEdicao(TarefaAtualizarRequest request) {
		if (request == null || request.prioridade() == null) {
			throw new TarefaDadosInvalidosException();
		}
		validarTituloEPrazo(request.titulo(), request.prazo());
	}

	private static void validarTituloEPrazo(String titulo, LocalDate prazo) {
		if (titulo == null || titulo.isBlank() || titulo.length() > 160 || prazo == null) {
			throw new TarefaDadosInvalidosException();
		}
		// Prazos passados são aceitos para permitir registrar e acompanhar tarefas atrasadas.
	}

	private static void validarTransicao(EstadoTarefa estadoAtual, EstadoTarefa novoEstado) {
		if (estadoAtual == null || novoEstado == null) {
			throw new TarefaDadosInvalidosException();
		}
		if (estadoAtual == novoEstado) {
			return;
		}
		boolean permitida = estadoAtual == EstadoTarefa.ABERTA && novoEstado == EstadoTarefa.CONCLUIDA
				|| estadoAtual == EstadoTarefa.CONCLUIDA && novoEstado == EstadoTarefa.ABERTA;
		if (!permitida) {
			throw new TarefaTransicaoEstadoInvalidaException();
		}
	}

	private Disciplina buscarDisciplinaDoCriador(Long disciplinaId, Long criadorId) {
		if (disciplinaId == null) {
			return null;
		}
		return disciplinaRepository.findByIdAndUsuario_Id(disciplinaId, criadorId)
				.orElseThrow(DisciplinaNaoEncontradaException::new);
	}

	private Tarefa buscarTarefaDoCriador(Long tarefaId, Long criadorId) {
		return tarefaRepository.findByIdAndCriador_Id(tarefaId, criadorId)
				.orElseThrow(TarefaNaoEncontradaException::new);
	}

	private static TarefaResponse paraResposta(Tarefa tarefa) {
		Disciplina disciplina = tarefa.getDisciplina();
		return new TarefaResponse(
				tarefa.getId(),
				disciplina == null ? null : disciplina.getId(),
				disciplina == null ? null : disciplina.getNome(),
				tarefa.getTipo(),
				tarefa.getTitulo(),
				tarefa.getDescricao(),
				tarefa.getPrazo(),
				tarefa.getPrioridade(),
				tarefa.getEstado(),
				tarefa.getCriadaEm(),
				tarefa.getAtualizadaEm()
		);
	}
}
