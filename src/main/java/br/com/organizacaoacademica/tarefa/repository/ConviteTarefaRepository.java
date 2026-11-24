package br.com.organizacaoacademica.tarefa.repository;

import br.com.organizacaoacademica.tarefa.entity.ConviteTarefa;
import br.com.organizacaoacademica.tarefa.entity.EstadoConvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConviteTarefaRepository extends JpaRepository<ConviteTarefa, Long> {

	List<ConviteTarefa> findAllByUsuario_IdOrderByCriadoEmDesc(Long usuarioId);

	List<ConviteTarefa> findAllByTarefa_IdAndTarefa_Criador_IdOrderByCriadoEmDesc(
			Long tarefaId,
			Long criadorId
	);

	List<ConviteTarefa> findAllByTarefa_IdAndEstado(Long tarefaId, EstadoConvite estado);

	Optional<ConviteTarefa> findByIdAndUsuario_Id(Long id, Long usuarioId);

	Optional<ConviteTarefa> findByIdAndTarefa_IdAndTarefa_Criador_Id(
			Long id,
			Long tarefaId,
			Long criadorId
	);

	boolean existsByTarefa_IdAndUsuario_IdAndEstado(Long tarefaId, Long usuarioId, EstadoConvite estado);
}
