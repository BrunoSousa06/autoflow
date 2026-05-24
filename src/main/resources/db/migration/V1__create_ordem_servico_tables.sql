CREATE TABLE ordem_servico (
    id UUID PRIMARY KEY,
    numero_os VARCHAR(50) NOT NULL UNIQUE,
    cliente_id UUID NOT NULL,
    veiculo_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_abertura TIMESTAMP NOT NULL
);

CREATE TABLE ordem_servico_servico_solicitado (
    ordem_servico_id UUID NOT NULL,
    servico_id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    ordem INTEGER NOT NULL,
    PRIMARY KEY (ordem_servico_id, ordem),
    CONSTRAINT fk_ordem_servico_servico_solicitado_ordem_servico
        FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico (id)
);

CREATE INDEX idx_ordem_servico_servico_solicitado_ordem_servico_id
    ON ordem_servico_servico_solicitado (ordem_servico_id);
