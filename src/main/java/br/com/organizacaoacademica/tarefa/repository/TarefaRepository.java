package br.com.organizacaoacademica.tarefa.repository;

import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Consultas de tarefas com opções de escopo pelo usuário criador. */
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

	List<Tarefa> findAllByCriador_IdOrderByPrazoAsc(Long criadorId);

	Optional<Tarefa> findByIdAndCriador_Id(Long id, Long criadorId);
}
