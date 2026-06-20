ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS execucao_iniciada_em TIMESTAMP;
