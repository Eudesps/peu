package br.com.organizacaoacademica.tarefa.repository;

import br.com.organizacaoacademica.tarefa.entity.ParticipanteTarefa;
import br.com.organizacaoacademica.tarefa.entity.ParticipanteTarefaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipanteTarefaRepository extends JpaRepository<ParticipanteTarefa, ParticipanteTarefaId> {

	boolean existsByTarefa_IdAndUsuario_Id(Long tarefaId, Long usuarioId);
}
