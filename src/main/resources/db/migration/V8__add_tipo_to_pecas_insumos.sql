ALTER TABLE pecas_insumos
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(20) NOT NULL DEFAULT 'PECA';

ALTER TABLE pecas_insumos
    ADD CONSTRAINT chk_pecas_insumos_tipo
        CHECK (tipo IN ('PECA', 'INSUMO'));
