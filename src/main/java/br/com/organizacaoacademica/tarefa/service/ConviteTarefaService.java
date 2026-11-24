package br.com.organizacaoacademica.tarefa.service;

import br.com.organizacaoacademica.tarefa.dto.ConviteTarefaRequest;
import br.com.organizacaoacademica.tarefa.dto.ConviteTarefaResponse;
import br.com.organizacaoacademica.tarefa.dto.RespostaConviteRequest;
import br.com.organizacaoacademica.tarefa.entity.ConviteTarefa;
import br.com.organizacaoacademica.tarefa.entity.EstadoConvite;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.ParticipanteTarefa;
import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import br.com.organizacaoacademica.tarefa.entity.TipoTarefa;
import br.com.organizacaoacademica.tarefa.repository.ConviteTarefaRepository;
import br.com.organizacaoacademica.tarefa.repository.ParticipanteTarefaRepository;
import br.com.organizacaoacademica.tarefa.repository.TarefaRepository;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ConviteTarefaService {

	private final ConviteTarefaRepository conviteRepository;
	private final ParticipanteTarefaRepository participanteRepository;
	private final TarefaRepository tarefaRepository;
	private final UsuarioRepository usuarioRepository;

	public ConviteTarefaService(
			ConviteTarefaRepository conviteRepository,
			ParticipanteTarefaRepository participanteRepository,
			TarefaRepository tarefaRepository,
			UsuarioRepository usuarioRepository
	) {
		this.conviteRepository = conviteRepository;
		this.participanteRepository = participanteRepository;
		this.tarefaRepository = tarefaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public ConviteTarefaResponse convidar(
			String emailAutenticado,
			Long tarefaId,
			ConviteTarefaRequest request
	) {
		if (request == null || request.email() == null || request.email().isBlank()
				|| request.email().length() > 254) {
			throw new ConviteDadosInvalidosException();
		}
		Usuario criador = buscarUsuario(emailAutenticado);
		Tarefa tarefa = buscarTarefaDoCriador(tarefaId, criador.getId());
		validarTarefaAbertaECompartilhada(tarefa);

		String emailConvidado = request.email().trim().toLowerCase(Locale.ROOT);
		Usuario convidado = usuarioRepository.findByEmailIgnoreCase(emailConvidado)
				.orElseThrow(UsuarioConvidadoNaoEncontradoException::new);
		if (convidado.getId().equals(criador.getId())
				|| participanteRepository.existsByTarefa_IdAndUsuario_Id(tarefaId, convidado.getId())
				|| conviteRepository.existsByTarefa_IdAndUsuario_IdAndEstado(
					tarefaId, convidado.getId(), EstadoConvite.PENDENTE)) {
			throw new ConviteConflitoException();
		}

		ConviteTarefa convite = conviteRepository.saveAndFlush(new ConviteTarefa(tarefa, convidado));
		return paraResposta(convite);
	}

	@Transactional(readOnly = true)
	public List<ConviteTarefaResponse> listarRecebidos(String emailAutenticado) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		return conviteRepository.findAllByUsuario_IdOrderByCriadoEmDesc(usuarioId)
				.stream().map(ConviteTarefaService::paraResposta).toList();
	}

	@Transactional(readOnly = true)
	public ConviteTarefaResponse buscarRecebido(String emailAutenticado, Long conviteId) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		return conviteRepository.findByIdAndUsuario_Id(conviteId, usuarioId)
				.map(ConviteTarefaService::paraResposta)
				.orElseThrow(ConviteNaoEncontradoException::new);
	}

	@Transactional(readOnly = true)
	public List<ConviteTarefaResponse> listarDaTarefa(String emailAutenticado, Long tarefaId) {
		Long criadorId = buscarUsuario(emailAutenticado).getId();
		Tarefa tarefa = buscarTarefaDoCriador(tarefaId, criadorId);
		if (tarefa.getTipo() != TipoTarefa.COMPARTILHADA) {
			throw new ConviteNaoPermitidoException();
		}
		return conviteRepository.findAllByTarefa_IdAndTarefa_Criador_IdOrderByCriadoEmDesc(tarefaId, criadorId)
				.stream().map(ConviteTarefaService::paraResposta).toList();
	}

	@Transactional(readOnly = true)
	public ConviteTarefaResponse buscarDaTarefa(String emailAutenticado, Long tarefaId, Long conviteId) {
		Long criadorId = buscarUsuario(emailAutenticado).getId();
		Tarefa tarefa = buscarTarefaDoCriador(tarefaId, criadorId);
		if (tarefa.getTipo() != TipoTarefa.COMPARTILHADA) {
			throw new ConviteNaoPermitidoException();
		}
		return conviteRepository.findByIdAndTarefa_IdAndTarefa_Criador_Id(conviteId, tarefaId, criadorId)
				.map(ConviteTarefaService::paraResposta)
				.orElseThrow(ConviteNaoEncontradoException::new);
	}

	@Transactional
	public ConviteTarefaResponse responder(
			String emailAutenticado,
			Long conviteId,
			RespostaConviteRequest request
	) {
		if (request == null || request.estado() == null
				|| (request.estado() != EstadoConvite.ACEITO && request.estado() != EstadoConvite.RECUSADO)) {
			throw new ConviteDadosInvalidosException();
		}
		Usuario usuario = buscarUsuario(emailAutenticado);
		ConviteTarefa convite = conviteRepository.findByIdAndUsuario_Id(conviteId, usuario.getId())
				.orElseThrow(ConviteNaoEncontradoException::new);
		if (convite.getEstado() != EstadoConvite.PENDENTE) {
			throw new ConviteEstadoInvalidoException();
		}
		if (convite.getTarefa().getEstado() != EstadoTarefa.ABERTA) {
			throw new TarefaConcluidaException();
		}

		if (request.estado() == EstadoConvite.ACEITO) {
			if (participanteRepository.existsByTarefa_IdAndUsuario_Id(convite.getTarefa().getId(), usuario.getId())) {
				throw new ConviteConflitoException();
			}
			participanteRepository.save(new ParticipanteTarefa(convite.getTarefa(), usuario));
		}
		convite.responder(request.estado());
		return paraResposta(conviteRepository.saveAndFlush(convite));
	}

	@Transactional
	public ConviteTarefaResponse cancelar(String emailAutenticado, Long tarefaId, Long conviteId) {
		Long criadorId = buscarUsuario(emailAutenticado).getId();
		Tarefa tarefa = buscarTarefaDoCriador(tarefaId, criadorId);
		validarTarefaAbertaECompartilhada(tarefa);
		ConviteTarefa convite = conviteRepository
				.findByIdAndTarefa_IdAndTarefa_Criador_Id(conviteId, tarefaId, criadorId)
				.orElseThrow(ConviteNaoEncontradoException::new);
		if (convite.getEstado() != EstadoConvite.PENDENTE) {
			throw new ConviteEstadoInvalidoException();
		}
		convite.cancelar();
		return paraResposta(conviteRepository.saveAndFlush(convite));
	}

	private void validarTarefaAbertaECompartilhada(Tarefa tarefa) {
		if (tarefa.getEstado() == EstadoTarefa.CONCLUIDA) {
			throw new TarefaConcluidaException();
		}
		if (tarefa.getTipo() != TipoTarefa.COMPARTILHADA) {
			throw new ConviteNaoPermitidoException();
		}
	}

	private Usuario buscarUsuario(String emailAutenticado) {
		String emailNormalizado = emailAutenticado.trim().toLowerCase(Locale.ROOT);
		return usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.orElseThrow(() -> new IllegalStateException("A conta autenticada nao foi encontrada."));
	}

	private Tarefa buscarTarefaDoCriador(Long tarefaId, Long criadorId) {
		return tarefaRepository.findByIdAndCriador_Id(tarefaId, criadorId)
				.orElseThrow(TarefaNaoEncontradaException::new);
	}

	private static ConviteTarefaResponse paraResposta(ConviteTarefa convite) {
		return new ConviteTarefaResponse(
				convite.getId(),
				convite.getTarefa().getId(),
				convite.getTarefa().getTitulo(),
				convite.getUsuario().getEmail(),
				convite.getEstado(),
				convite.getCriadoEm(),
				convite.getRespondidoEm()
		);
	}
}
