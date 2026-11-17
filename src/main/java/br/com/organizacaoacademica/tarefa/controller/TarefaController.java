package br.com.organizacaoacademica.tarefa.controller;

import br.com.organizacaoacademica.tarefa.dto.TarefaAtualizarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaCriarRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaEstadoRequest;
import br.com.organizacaoacademica.tarefa.dto.TarefaResponse;
import br.com.organizacaoacademica.tarefa.OrdenacaoTarefa;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tarefas")
public class TarefaController {

	private final TarefaService tarefaService;

	public TarefaController(TarefaService tarefaService) {
		this.tarefaService = tarefaService;
	}

	@GetMapping
	public List<TarefaResponse> listar(
			@RequestParam(required = false) Long disciplinaId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prazoInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prazoFim,
			@RequestParam(required = false) PrioridadeTarefa prioridade,
			@RequestParam(required = false) EstadoTarefa estado,
			@RequestParam(defaultValue = "PRAZO") OrdenacaoTarefa ordenarPor,
			@RequestParam(defaultValue = "ASC") Sort.Direction direcao,
			Authentication authentication
	) {
		return tarefaService.listar(
				authentication.getName(), disciplinaId, prazoInicio, prazoFim,
				prioridade, estado, ordenarPor, direcao);
	}

	@GetMapping("/{id}")
	public TarefaResponse buscar(@PathVariable Long id, Authentication authentication) {
		return tarefaService.buscar(authentication.getName(), id);
	}

	@PostMapping
	public ResponseEntity<TarefaResponse> criar(
			@Valid @RequestBody TarefaCriarRequest request,
			Authentication authentication
	) {
		TarefaResponse tarefa = tarefaService.criar(authentication.getName(), request);
		return ResponseEntity.created(URI.create("/api/v1/tarefas/" + tarefa.id())).body(tarefa);
	}

	@PutMapping("/{id}")
	public TarefaResponse editar(
			@PathVariable Long id,
			@Valid @RequestBody TarefaAtualizarRequest request,
			Authentication authentication
	) {
		return tarefaService.editar(authentication.getName(), id, request);
	}

	@PatchMapping("/{id}/estado")
	public TarefaResponse alterarEstado(
			@PathVariable Long id,
			@Valid @RequestBody TarefaEstadoRequest request,
			Authentication authentication
	) {
		return tarefaService.alterarEstado(authentication.getName(), id, request.estado());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
		tarefaService.excluir(authentication.getName(), id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
