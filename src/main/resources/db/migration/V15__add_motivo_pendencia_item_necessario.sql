ALTER TABLE ordem_servico_item_necessario
    ADD COLUMN motivo_pendencia VARCHAR(40),
    ADD COLUMN quantidade_disponivel INTEGER,
    ADD COLUMN mensagem_status VARCHAR(255);

ALTER TABLE ordem_servico_item_necessario
    ADD CONSTRAINT chk_os_item_motivo_pendencia
    CHECK (
        motivo_pendencia IS NULL
        OR motivo_pendencia IN ('ESTOQUE_INSUFICIENTE')
    );