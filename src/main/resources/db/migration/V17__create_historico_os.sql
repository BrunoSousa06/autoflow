CREATE TABLE IF NOT EXISTS ordem_servico_status_historico (
                                                              id BIGSERIAL PRIMARY KEY,
                                                              ordem_servico_id BIGINT NOT NULL,
                                                              status VARCHAR(50) NOT NULL,
    registrado_em TIMESTAMP NOT NULL,
    mensagem_cliente VARCHAR(500) NOT NULL,

    CONSTRAINT fk_historico_status_os
    FOREIGN KEY (ordem_servico_id)
    REFERENCES ordem_servico(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_historico_status_os_ordem_servico_id
    ON ordem_servico_status_historico (ordem_servico_id);