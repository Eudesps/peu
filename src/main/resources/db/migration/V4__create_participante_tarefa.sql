CREATE TABLE participante_tarefa (
    tarefa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    adicionado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_participante_tarefa
        PRIMARY KEY (tarefa_id, usuario_id),
    CONSTRAINT fk_participante_tarefa_tarefa
        FOREIGN KEY (tarefa_id) REFERENCES tarefa (id) ON DELETE CASCADE,
    CONSTRAINT fk_participante_tarefa_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE RESTRICT
);

CREATE INDEX idx_participante_tarefa_usuario
    ON participante_tarefa (usuario_id);
