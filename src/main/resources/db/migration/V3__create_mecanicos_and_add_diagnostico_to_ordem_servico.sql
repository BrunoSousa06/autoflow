ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS diagnostico_mecanico VARCHAR(255),
    ADD COLUMN IF NOT EXISTS diagnostico_iniciado_em TIMESTAMP,
    ADD COLUMN IF NOT EXISTS diagnostico_concluido_em TIMESTAMP,
    ADD COLUMN IF NOT EXISTS diagnostico_laudo TEXT;