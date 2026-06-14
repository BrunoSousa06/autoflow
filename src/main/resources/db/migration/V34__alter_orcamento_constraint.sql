DROP INDEX IF EXISTS uq_orcamento_disponivel_por_os;

CREATE UNIQUE INDEX IF NOT EXISTS uq_orcamento_disponivel_por_os_tipo
    ON orcamento (ordem_servico_id, tipo)
    WHERE status = 'DISPONIVEL';