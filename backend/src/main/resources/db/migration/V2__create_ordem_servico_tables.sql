CREATE TABLE IF NOT EXISTS ordem_servico (
    id BIGSERIAL PRIMARY KEY,
    numero_os VARCHAR(50) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_abertura TIMESTAMP NOT NULL,
    CONSTRAINT fk_ordem_servico_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id),
    CONSTRAINT fk_ordem_servico_veiculo
        FOREIGN KEY (veiculo_id)
        REFERENCES veiculos(id)
);

CREATE TABLE IF NOT EXISTS ordem_servico_servico_solicitado (
    ordem_servico_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    ordem INTEGER NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY (ordem_servico_id, ordem),
    CONSTRAINT fk_ordem_servico_servico_solicitado_ordem_servico
        FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_cliente_id
    ON ordem_servico (cliente_id);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_veiculo_id
    ON ordem_servico (veiculo_id);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_servico_solicitado_ordem_servico_id
    ON ordem_servico_servico_solicitado (ordem_servico_id);
