ALTER TABLE ordem_servico
    ADD COLUMN acompanhamento_token_hash VARCHAR(64),
    ADD COLUMN acompanhamento_token_criado_em TIMESTAMP,
    ADD COLUMN acompanhamento_token_expira_em TIMESTAMP,
    ADD COLUMN acompanhamento_token_revogado_em TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_ordem_servico_acompanhamento_token_hash
    ON ordem_servico (acompanhamento_token_hash)
    WHERE acompanhamento_token_hash IS NOT NULL;

ALTER TABLE ordem_servico
    ADD CONSTRAINT chk_ordem_servico_acompanhamento_token_periodo
        CHECK (
            acompanhamento_token_expira_em IS NULL
                OR acompanhamento_token_criado_em IS NULL
                OR acompanhamento_token_expira_em > acompanhamento_token_criado_em
            );

ALTER TABLE ordem_servico
    ADD CONSTRAINT chk_ordem_servico_acompanhamento_token_criacao
        CHECK (
            acompanhamento_token_hash IS NULL
                OR acompanhamento_token_criado_em IS NOT NULL
            );