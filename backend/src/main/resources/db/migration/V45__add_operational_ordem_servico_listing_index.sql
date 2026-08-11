CREATE INDEX IF NOT EXISTS idx_ordem_servico_operacional_prioridade_data
    ON ordem_servico (
        (CASE status
            WHEN 'EM_EXECUCAO' THEN 1
            WHEN 'AGUARDANDO_APROVACAO' THEN 2
            WHEN 'EM_DIAGNOSTICO' THEN 3
            WHEN 'RECEBIDA' THEN 4
            ELSE 5
        END),
        data_abertura ASC,
        id ASC
    )
    WHERE status IN (
        'EM_EXECUCAO',
        'AGUARDANDO_APROVACAO',
        'EM_DIAGNOSTICO',
        'RECEBIDA'
    );
