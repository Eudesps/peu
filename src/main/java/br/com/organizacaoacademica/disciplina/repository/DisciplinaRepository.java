package br.com.organizacaoacademica.disciplina.repository;

import br.com.organizacaoacademica.disciplina.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Consultas de disciplinas sempre limitadas ao usuário proprietário. */
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

	List<Disciplina> findAllByUsuario_IdOrderByNomeAsc(Long usuarioId);

	Optional<Disciplina> findByIdAndUsuario_Id(Long id, Long usuarioId);
}
