CREATE TABLE IF NOT EXISTS public.reparo_adicional (
                                                       id BIGSERIAL PRIMARY KEY,
                                                       ordem_servico_id BIGINT NOT NULL,
                                                       mecanico_id BIGINT NOT NULL,
                                                       orcamento_id BIGINT,
                                                       status VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    aprovado_em TIMESTAMP,
    recusado_em TIMESTAMP,
    motivo_recusa VARCHAR(255)
    );