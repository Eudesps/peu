package br.com.organizacaoacademica.disciplina.service;

import br.com.organizacaoacademica.disciplina.dto.DisciplinaRequest;
import br.com.organizacaoacademica.disciplina.dto.DisciplinaResponse;
import br.com.organizacaoacademica.disciplina.entity.Disciplina;
import br.com.organizacaoacademica.disciplina.repository.DisciplinaRepository;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import br.com.organizacaoacademica.usuario.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class DisciplinaService {

	private final DisciplinaRepository disciplinaRepository;
	private final UsuarioRepository usuarioRepository;

	public DisciplinaService(DisciplinaRepository disciplinaRepository, UsuarioRepository usuarioRepository) {
		this.disciplinaRepository = disciplinaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public List<DisciplinaResponse> listar(String emailAutenticado) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		return disciplinaRepository.findAllByUsuario_IdOrderByNomeAsc(usuarioId)
				.stream()
				.map(DisciplinaService::paraResposta)
				.toList();
	}

	@Transactional(readOnly = true)
	public DisciplinaResponse buscar(String emailAutenticado, Long disciplinaId) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		return buscarDisciplinaDoUsuario(disciplinaId, usuarioId)
				.map(DisciplinaService::paraResposta)
				.orElseThrow(DisciplinaNaoEncontradaException::new);
	}

	@Transactional
	public DisciplinaResponse criar(String emailAutenticado, DisciplinaRequest request) {
		Usuario usuario = buscarUsuario(emailAutenticado);
		Disciplina disciplina = disciplinaRepository.save(
				new Disciplina(usuario, request.nome()));
		return paraResposta(disciplina);
	}

	@Transactional
	public DisciplinaResponse editar(String emailAutenticado, Long disciplinaId, DisciplinaRequest request) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		Disciplina disciplina = buscarDisciplinaDoUsuario(disciplinaId, usuarioId)
				.orElseThrow(DisciplinaNaoEncontradaException::new);
		disciplina.alterarNome(request.nome());
		return paraResposta(disciplina);
	}

	@Transactional
	public void excluir(String emailAutenticado, Long disciplinaId) {
		Long usuarioId = buscarUsuario(emailAutenticado).getId();
		Disciplina disciplina = buscarDisciplinaDoUsuario(disciplinaId, usuarioId)
				.orElseThrow(DisciplinaNaoEncontradaException::new);
		try {
			disciplinaRepository.delete(disciplina);
			disciplinaRepository.flush();
		} catch (DataIntegrityViolationException exception) {
			// A FK para tarefa usa RESTRICT para exigir desassociação antes da exclusão.
			throw new DisciplinaEmUsoException();
		}
	}

	private Usuario buscarUsuario(String emailAutenticado) {
		String emailNormalizado = emailAutenticado.trim().toLowerCase(Locale.ROOT);
		return usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.orElseThrow(() -> new IllegalStateException("A conta autenticada não foi encontrada."));
	}

	private java.util.Optional<Disciplina> buscarDisciplinaDoUsuario(Long disciplinaId, Long usuarioId) {
		return disciplinaRepository.findByIdAndUsuario_Id(disciplinaId, usuarioId);
	}

	private static DisciplinaResponse paraResposta(Disciplina disciplina) {
		return new DisciplinaResponse(disciplina.getId(), disciplina.getNome(), disciplina.getCriadaEm());
	}
}
