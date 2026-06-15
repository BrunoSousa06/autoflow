CREATE INDEX IF NOT EXISTS idx_ordem_servico_data_abertura_desc
    ON ordem_servico (data_abertura DESC);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_status_data_abertura_desc
    ON ordem_servico (status, data_abertura DESC);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_cliente_id_data_abertura_desc
    ON ordem_servico (cliente_id, data_abertura DESC);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_ordem_servico_numero_os_trgm
    ON ordem_servico USING gin (lower(numero_os) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_cliente_nome_trgm
    ON ordem_servico USING gin (lower(cliente_nome) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ordem_servico_cliente_cpf_cnpj_trgm
    ON ordem_servico USING gin (lower(cliente_cpf_cnpj) gin_trgm_ops);
