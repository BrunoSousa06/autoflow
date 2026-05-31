CREATE TABLE IF NOT EXISTS orcamento (

    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    versao INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,

    criado_em TIMESTAMP NOT NULL,
    disponibilizado_em TIMESTAMP,

    total_servicos NUMERIC(19, 2) NOT NULL,
    total_itens NUMERIC(19, 2) NOT NULL,
    total_geral NUMERIC(19, 2) NOT NULL,

    CONSTRAINT fk_orcamento_ordem_servico
    FOREIGN KEY (ordem_servico_id)
    REFERENCES ordem_servico(id)
    ON DELETE CASCADE,

    CONSTRAINT uq_orcamento_os_tipo_versao
    UNIQUE (ordem_servico_id, tipo, versao),

    CONSTRAINT chk_orcamento_tipo
    CHECK (tipo IN ('PRINCIPAL', 'ADICIONAL')),

    CONSTRAINT chk_orcamento_status
    CHECK (status IN ('DISPONIVEL', 'APROVADO', 'REPROVADO', 'SUBSTITUIDO'))
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_orcamento_disponivel_por_os
    ON orcamento (ordem_servico_id)
    WHERE status = 'DISPONIVEL';

CREATE INDEX IF NOT EXISTS idx_orcamento_os_id
    ON orcamento (ordem_servico_id);

CREATE INDEX IF NOT EXISTS idx_orcamento_os_tipo_versao
    ON orcamento (ordem_servico_id, tipo, versao);


CREATE TABLE IF NOT EXISTS orcamento_servico_item (
                                                      orcamento_id BIGINT NOT NULL,
                                                      servico_id BIGINT NOT NULL,
                                                      nome VARCHAR(255) NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    ordem INTEGER NOT NULL,

    PRIMARY KEY (orcamento_id, ordem),

    CONSTRAINT fk_orcamento_servico_item_orcamento
    FOREIGN KEY (orcamento_id)
    REFERENCES orcamento(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_orcamento_servico_item_orcamento_id
    ON orcamento_servico_item (orcamento_id);


CREATE TABLE IF NOT EXISTS orcamento_item_necessario_item (
                                                              orcamento_id BIGINT NOT NULL,
                                                              peca_insumo_id BIGINT NOT NULL,
                                                              nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor_unitario NUMERIC(19, 2) NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_total NUMERIC(19, 2) NOT NULL,
    ordem INTEGER NOT NULL,

    PRIMARY KEY (orcamento_id, ordem),

    CONSTRAINT fk_orcamento_item_necessario_item_orcamento
    FOREIGN KEY (orcamento_id)
    REFERENCES orcamento(id)
    ON DELETE CASCADE,

    CONSTRAINT chk_orcamento_item_necessario_tipo
    CHECK (tipo IN ('PECA', 'INSUMO'))
    );

CREATE INDEX IF NOT EXISTS idx_orcamento_item_necessario_item_orcamento_id
    ON orcamento_item_necessario_item (orcamento_id);
