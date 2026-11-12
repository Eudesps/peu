package br.com.organizacaoacademica.disciplina.controller;

import br.com.organizacaoacademica.disciplina.dto.DisciplinaRequest;
import br.com.organizacaoacademica.disciplina.dto.DisciplinaResponse;
import br.com.organizacaoacademica.disciplina.service.DisciplinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/disciplinas")
public class DisciplinaController {

	private final DisciplinaService disciplinaService;

	public DisciplinaController(DisciplinaService disciplinaService) {
		this.disciplinaService = disciplinaService;
	}

	@GetMapping
	public List<DisciplinaResponse> listar(Authentication authentication) {
		return disciplinaService.listar(authentication.getName());
	}

	@GetMapping("/{id}")
	public DisciplinaResponse buscar(@PathVariable Long id, Authentication authentication) {
		return disciplinaService.buscar(authentication.getName(), id);
	}

	@PostMapping
	public ResponseEntity<DisciplinaResponse> criar(
			@Valid @RequestBody DisciplinaRequest request,
			Authentication authentication
	) {
		DisciplinaResponse disciplina = disciplinaService.criar(authentication.getName(), request);
		return ResponseEntity.created(URI.create("/api/v1/disciplinas/" + disciplina.id())).body(disciplina);
	}

	@PutMapping("/{id}")
	public DisciplinaResponse editar(
			@PathVariable Long id,
			@Valid @RequestBody DisciplinaRequest request,
			Authentication authentication
	) {
		return disciplinaService.editar(authentication.getName(), id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
		disciplinaService.excluir(authentication.getName(), id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
