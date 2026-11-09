package br.com.organizacaoacademica.usuario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Locale;

/**
 * Conta de estudante persistida no banco.
 *
 * <p>A entidade não implementa {@code UserDetails}; a integração com Spring Security
 * deve adaptar esta entidade em um serviço de autenticação.</p>
 */
@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 120)
	@Column(name = "nome", nullable = false, length = 120)
	private String nome;

	@NotBlank
	@Email
	@Size(max = 254)
	@Column(name = "email", nullable = false, length = 254)
	private String email;

	@NotBlank
	@Size(max = 255)
	@Column(name = "senha_hash", nullable = false, length = 255)
	private String senhaHash;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	protected Usuario() {
		// Required by JPA.
	}

	public Usuario(String nome, String email, String senhaHash) {
		this.nome = normalizarTexto(nome);
		this.email = normalizarEmail(email);
		this.senhaHash = senhaHash;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public void alterarNome(String nome) {
		this.nome = normalizarTexto(nome);
	}

	public void alterarEmail(String email) {
		this.email = normalizarEmail(email);
	}

	/** Recebe somente o hash produzido pelo serviço de autenticação, nunca a senha em texto puro. */
	public void atualizarSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	@PrePersist
	void prepararParaPersistencia() {
		this.nome = normalizarTexto(this.nome);
		this.email = normalizarEmail(this.email);
		if (this.criadoEm == null) {
			this.criadoEm = Instant.now();
		}
	}

	@PreUpdate
	void normalizarAntesDeAtualizar() {
		this.nome = normalizarTexto(this.nome);
		this.email = normalizarEmail(this.email);
	}

	private static String normalizarTexto(String value) {
		return value == null ? null : value.trim();
	}

	private static String normalizarEmail(String value) {
		String normalized = normalizarTexto(value);
		return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
	}
}
