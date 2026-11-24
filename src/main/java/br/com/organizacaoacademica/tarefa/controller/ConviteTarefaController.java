package br.com.organizacaoacademica.tarefa.controller;

import br.com.organizacaoacademica.tarefa.dto.ConviteTarefaRequest;
import br.com.organizacaoacademica.tarefa.dto.ConviteTarefaResponse;
import br.com.organizacaoacademica.tarefa.dto.RespostaConviteRequest;
import br.com.organizacaoacademica.tarefa.service.ConviteTarefaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class ConviteTarefaController {

	private final ConviteTarefaService conviteService;

	public ConviteTarefaController(ConviteTarefaService conviteService) {
		this.conviteService = conviteService;
	}

	@PostMapping("/api/v1/tarefas/{tarefaId}/convites")
	public ResponseEntity<ConviteTarefaResponse> convidar(
			@PathVariable Long tarefaId,
			@Valid @RequestBody ConviteTarefaRequest request,
			Authentication authentication
	) {
		ConviteTarefaResponse convite = conviteService.convidar(authentication.getName(), tarefaId, request);
		return ResponseEntity.created(URI.create(
				"/api/v1/tarefas/" + tarefaId + "/convites/" + convite.id())).body(convite);
	}

	@GetMapping("/api/v1/tarefas/{tarefaId}/convites")
	public List<ConviteTarefaResponse> listarDaTarefa(
			@PathVariable Long tarefaId,
			Authentication authentication
	) {
		return conviteService.listarDaTarefa(authentication.getName(), tarefaId);
	}

	@GetMapping("/api/v1/tarefas/{tarefaId}/convites/{conviteId}")
	public ConviteTarefaResponse buscarDaTarefa(
			@PathVariable Long tarefaId,
			@PathVariable Long conviteId,
			Authentication authentication
	) {
		return conviteService.buscarDaTarefa(authentication.getName(), tarefaId, conviteId);
	}

	@DeleteMapping("/api/v1/tarefas/{tarefaId}/convites/{conviteId}")
	public ConviteTarefaResponse cancelar(
			@PathVariable Long tarefaId,
			@PathVariable Long conviteId,
			Authentication authentication
	) {
		return conviteService.cancelar(authentication.getName(), tarefaId, conviteId);
	}

	@GetMapping("/api/v1/convites")
	public List<ConviteTarefaResponse> listarRecebidos(Authentication authentication) {
		return conviteService.listarRecebidos(authentication.getName());
	}

	@GetMapping("/api/v1/convites/{conviteId}")
	public ConviteTarefaResponse buscarRecebido(
			@PathVariable Long conviteId,
			Authentication authentication
	) {
		return conviteService.buscarRecebido(authentication.getName(), conviteId);
	}

	@PatchMapping("/api/v1/convites/{conviteId}/resposta")
	public ConviteTarefaResponse responder(
			@PathVariable Long conviteId,
			@Valid @RequestBody RespostaConviteRequest request,
			Authentication authentication
	) {
		return conviteService.responder(authentication.getName(), conviteId, request);
	}
}
