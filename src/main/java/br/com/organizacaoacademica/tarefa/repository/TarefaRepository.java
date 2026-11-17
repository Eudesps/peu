package br.com.organizacaoacademica.tarefa.repository;

import br.com.organizacaoacademica.tarefa.OrdenacaoTarefa;
import br.com.organizacaoacademica.tarefa.entity.EstadoTarefa;
import br.com.organizacaoacademica.tarefa.entity.PrioridadeTarefa;
import br.com.organizacaoacademica.tarefa.entity.Tarefa;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consultas de tarefas com opções de escopo pelo usuário criador. */
public interface TarefaRepository extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

	default List<Tarefa> buscarComFiltrosDoCriador(
			Long criadorId,
			Long disciplinaId,
			LocalDate prazoInicio,
			LocalDate prazoFim,
			PrioridadeTarefa prioridade,
			EstadoTarefa estado,
			OrdenacaoTarefa ordenarPor,
			Sort.Direction direcao
	) {
		Specification<Tarefa> filtros = (root, query, builder) -> {
			List<Predicate> condicoes = new ArrayList<>();
			condicoes.add(builder.equal(root.get("criador").get("id"), criadorId));
			if (disciplinaId != null) {
				condicoes.add(builder.equal(root.get("disciplinaId"), disciplinaId));
			}
			if (prazoInicio != null) {
				condicoes.add(builder.greaterThanOrEqualTo(root.<LocalDate>get("prazo"), prazoInicio));
			}
			if (prazoFim != null) {
				condicoes.add(builder.lessThanOrEqualTo(root.<LocalDate>get("prazo"), prazoFim));
			}
			if (prioridade != null) {
				condicoes.add(builder.equal(root.get("prioridade"), prioridade));
			}
			if (estado != null) {
				condicoes.add(builder.equal(root.get("estado"), estado));
			}
			var ordem = switch (ordenarPor) {
				case DISCIPLINA -> {
					var nomeDisciplina = root.join("disciplina", JoinType.LEFT).<String>get("nome");
					yield direcao.isAscending() ? builder.asc(nomeDisciplina) : builder.desc(nomeDisciplina);
				}
				case PRAZO -> direcao.isAscending()
						? builder.asc(root.<LocalDate>get("prazo"))
						: builder.desc(root.<LocalDate>get("prazo"));
				case PRIORIDADE -> {
					var prioridadeOrdenada = builder.selectCase(root.<PrioridadeTarefa>get("prioridade"))
						.when(PrioridadeTarefa.BAIXA, 1)
						.when(PrioridadeTarefa.MEDIA, 2)
						.when(PrioridadeTarefa.ALTA, 3);
					yield direcao.isAscending() ? builder.asc(prioridadeOrdenada) : builder.desc(prioridadeOrdenada);
				}
				case ESTADO -> {
					var estadoOrdenado = builder.selectCase(root.<EstadoTarefa>get("estado"))
						.when(EstadoTarefa.ABERTA, 1)
						.when(EstadoTarefa.CONCLUIDA, 2);
					yield direcao.isAscending() ? builder.asc(estadoOrdenado) : builder.desc(estadoOrdenado);
				}
			};
			query.orderBy(
					ordem,
					builder.asc(root.get("id"))
			);
			return builder.and(condicoes.toArray(Predicate[]::new));
		};
		return findAll(filtros);
	}

	Optional<Tarefa> findByIdAndCriador_Id(Long id, Long criadorId);
}
