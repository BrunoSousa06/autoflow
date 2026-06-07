CREATE TABLE notificacao (
                             id BIGSERIAL PRIMARY KEY,
                             orcamento_id BIGINT NOT NULL,
                             cliente_id BIGINT NOT NULL,
                             canal VARCHAR(50) NOT NULL,
                             destinatario VARCHAR(255),
                             status VARCHAR(50) NOT NULL,
                             enviada_em TIMESTAMP,
                             mensagem_erro VARCHAR(1000),
                             criada_em TIMESTAMP NOT NULL
);