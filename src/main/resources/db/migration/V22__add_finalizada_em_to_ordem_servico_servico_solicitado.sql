ALTER TABLE ordem_servico_servico_solicitado
    ADD COLUMN IF NOT EXISTS finalizado_em TIMESTAMP;
