ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS diagnostico_mecanico_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_ordem_servico_diagnostico_mecanico_id
    ON ordem_servico (diagnostico_mecanico_id);

ALTER TABLE ordem_servico
    ADD CONSTRAINT fk_ordem_servico_diagnostico_mecanico
        FOREIGN KEY (diagnostico_mecanico_id)
            REFERENCES usuarios(id);

ALTER TABLE ordem_servico
    DROP COLUMN IF EXISTS diagnostico_mecanico;