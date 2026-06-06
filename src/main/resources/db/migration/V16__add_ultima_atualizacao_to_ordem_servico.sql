ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS ultima_atualizacao TIMESTAMP;

UPDATE ordem_servico
SET ultima_atualizacao = data_abertura
WHERE ultima_atualizacao IS NULL;

ALTER TABLE ordem_servico
    ALTER COLUMN ultima_atualizacao SET NOT NULL;