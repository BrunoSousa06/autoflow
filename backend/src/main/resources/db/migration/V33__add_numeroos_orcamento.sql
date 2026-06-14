ALTER TABLE orcamento
    ADD COLUMN numero_os VARCHAR(50) NULL;

-- Adiciona coluna numero_os à tabela ordem_servico_status_historico
ALTER TABLE ordem_servico_status_historico
    ADD COLUMN numero_os VARCHAR(50) NULL;

-- Adiciona coluna numero_os à tabela reparo_adicional
ALTER TABLE reparo_adicional
    ADD COLUMN numero_os VARCHAR(50) NULL;

-- Popular os dados a partir de ordem_servico (JOIN)
UPDATE orcamento o
SET numero_os = os.numero_os
    FROM ordem_servico os
WHERE o.ordem_servico_id = os.id;

UPDATE ordem_servico_status_historico hst
SET numero_os = os.numero_os
    FROM ordem_servico os
WHERE hst.ordem_servico_id = os.id;

UPDATE reparo_adicional ra
SET numero_os = os.numero_os
    FROM ordem_servico os
WHERE ra.ordem_servico_id = os.id;

-- Torna as colunas NOT NULL após população
ALTER TABLE orcamento
    ALTER COLUMN numero_os SET NOT NULL;

ALTER TABLE ordem_servico_status_historico
    ALTER COLUMN numero_os SET NOT NULL;

ALTER TABLE reparo_adicional
    ALTER COLUMN numero_os SET NOT NULL;

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_orcamento_numero_os
    ON orcamento (numero_os);

CREATE INDEX IF NOT EXISTS idx_historico_numero_os
    ON ordem_servico_status_historico (numero_os);

CREATE INDEX IF NOT EXISTS idx_reparo_numero_os
    ON reparo_adicional (numero_os);