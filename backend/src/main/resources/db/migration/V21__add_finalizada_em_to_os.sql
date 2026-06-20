ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS finalizada_em TIMESTAMP;
