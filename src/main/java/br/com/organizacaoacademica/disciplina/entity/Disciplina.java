package br.com.organizacaoacademica.disciplina.entity;

import br.com.organizacaoacademica.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Disciplina particular de uma conta, usada para organizar suas tarefas. */
@Entity
@Table(
		name = "disciplina",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_disciplina_id_usuario", columnNames = {"id", "usuario_id"}),
		indexes = @Index(name = "idx_disciplina_usuario_id", columnList = "usuario_id")
)
public class Disciplina {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "usuario_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_disciplina_usuario")
	)
	private Usuario usuario;

	@NotBlank
	@Size(max = 120)
	@Column(name = "nome", nullable = false, length = 120)
	private String nome;

	@Column(name = "criada_em", nullable = false, updatable = false)
	private Instant criadaEm;

	protected Disciplina() {
		// Required by JPA.
	}

	public Disciplina(Usuario usuario, String nome) {
		this.usuario = usuario;
		this.nome = normalizarNome(nome);
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public String getNome() {
		return nome;
	}

	public Instant getCriadaEm() {
		return criadaEm;
	}

	public void alterarNome(String nome) {
		this.nome = normalizarNome(nome);
	}

	@PrePersist
	void prepararParaPersistencia() {
		this.nome = normalizarNome(this.nome);
		if (this.criadaEm == null) {
			this.criadaEm = Instant.now();
		}
	}

	private static String normalizarNome(String value) {
		return value == null ? null : value.trim();
	}
}
