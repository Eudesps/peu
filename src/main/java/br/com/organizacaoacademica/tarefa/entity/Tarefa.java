package br.com.organizacaoacademica.tarefa.entity;

import br.com.organizacaoacademica.disciplina.entity.Disciplina;
import br.com.organizacaoacademica.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Atividade acadêmica criada por uma conta. O tipo e o criador são definidos
 * na criação e não podem ser alterados por meio desta entidade.
 */
@Entity
@Table(
		name = "tarefa",
		indexes = {
				@Index(name = "idx_tarefa_criador_prazo", columnList = "criador_id, prazo"),
				@Index(name = "idx_tarefa_disciplina_criador", columnList = "disciplina_id, criador_id")
		}
)
public class Tarefa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "criador_id",
		nullable = false,
		updatable = false,
		foreignKey = @ForeignKey(name = "fk_tarefa_criador")
	)
	private Usuario criador;

	@Column(name = "disciplina_id")
	private Long disciplinaId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns(
			value = {
					@JoinColumn(
							name = "disciplina_id",
						referencedColumnName = "id",
						insertable = false,
						updatable = false
					),
					@JoinColumn(
							name = "criador_id",
						referencedColumnName = "usuario_id",
						insertable = false,
						updatable = false
					)
			},
			foreignKey = @ForeignKey(name = "fk_tarefa_disciplina_criador")
	)
	private Disciplina disciplina;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 16, updatable = false)
	private TipoTarefa tipo;

	@NotBlank
	@Size(max = 160)
	@Column(name = "titulo", nullable = false, length = 160)
	private String titulo;

	@Column(name = "descricao", columnDefinition = "TEXT")
	private String descricao;

	@NotNull
	@Column(name = "prazo", nullable = false)
	private LocalDate prazo;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "prioridade", nullable = false, length = 8)
	private PrioridadeTarefa prioridade;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 12)
	private EstadoTarefa estado;

	@Column(name = "criada_em", nullable = false, updatable = false)
	private Instant criadaEm;

	@Column(name = "atualizada_em", nullable = false)
	private Instant atualizadaEm;

	protected Tarefa() {
		// Required by JPA.
	}

	public Tarefa(
			Usuario criador,
			TipoTarefa tipo,
			String titulo,
			String descricao,
			Disciplina disciplina,
			LocalDate prazo,
			PrioridadeTarefa prioridade
	) {
		this.criador = criador;
		this.tipo = tipo;
		this.titulo = normalizarTexto(titulo);
		this.descricao = normalizarDescricao(descricao);
		this.disciplina = disciplina;
		if (disciplina != null && disciplina.getId() == null) {
			throw new IllegalArgumentException("A disciplina deve estar persistida antes de ser associada à tarefa.");
		}
		this.disciplinaId = disciplina == null ? null : disciplina.getId();
		this.prazo = prazo;
		this.prioridade = prioridade == null ? PrioridadeTarefa.MEDIA : prioridade;
		this.estado = EstadoTarefa.ABERTA;
	}

	public Long getId() {
		return id;
	}

	public Usuario getCriador() {
		return criador;
	}

	public Disciplina getDisciplina() {
		return disciplina;
	}

	public TipoTarefa getTipo() {
		return tipo;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public LocalDate getPrazo() {
		return prazo;
	}

	public PrioridadeTarefa getPrioridade() {
		return prioridade;
	}

	public EstadoTarefa getEstado() {
		return estado;
	}

	public Instant getCriadaEm() {
		return criadaEm;
	}

	public Instant getAtualizadaEm() {
		return atualizadaEm;
	}

	public void alterarDadosCentrais(
			String titulo,
			String descricao,
			Disciplina disciplina,
			LocalDate prazo,
			PrioridadeTarefa prioridade
	) {
		this.titulo = normalizarTexto(titulo);
		this.descricao = normalizarDescricao(descricao);
		this.disciplina = disciplina;
		this.disciplinaId = disciplina == null ? null : disciplina.getId();
		this.prazo = prazo;
		this.prioridade = prioridade;
	}

	public void alterarEstado(EstadoTarefa estado) {
		if (estado == null) {
			throw new IllegalArgumentException("O estado da tarefa é obrigatório.");
		}
		this.estado = estado;
	}

	@PrePersist
	void prepararParaPersistencia() {
		this.titulo = normalizarTexto(this.titulo);
		this.descricao = normalizarDescricao(this.descricao);
		if (this.criadaEm == null) {
			this.criadaEm = Instant.now();
		}
		if (this.atualizadaEm == null) {
			this.atualizadaEm = this.criadaEm;
		}
	}

	@PreUpdate
	void atualizarDataDeModificacao() {
		this.titulo = normalizarTexto(this.titulo);
		this.descricao = normalizarDescricao(this.descricao);
		this.atualizadaEm = Instant.now();
	}

	private static String normalizarTexto(String value) {
		return value == null ? null : value.trim();
	}

	private static String normalizarDescricao(String value) {
		String normalized = normalizarTexto(value);
		return normalized == null || normalized.isEmpty() ? null : normalized;
	}
}
