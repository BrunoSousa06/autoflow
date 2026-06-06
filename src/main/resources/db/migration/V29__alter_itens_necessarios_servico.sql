ALTER TABLE ordem_servico_servico_item_necessario
    ADD COLUMN IF NOT EXISTS motivo_pendencia VARCHAR(20);

ALTER TABLE ordem_servico_servico_item_necessario
    ADD COLUMN IF NOT EXISTS quantidade_disponivel INTEGER;

ALTER TABLE ordem_servico_servico_item_necessario
    ADD COLUMN IF NOT EXISTS mensagem_status VARCHAR(20);