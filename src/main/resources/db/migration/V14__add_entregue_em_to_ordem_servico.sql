ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS entregue_em TIMESTAMP;
