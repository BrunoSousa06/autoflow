CREATE TABLE ordem_servico_servico_item_necessario (
                                              ordem_servico_id BIGINT NOT NULL,
                                              peca_insumo_id BIGINT NOT NULL,
                                              nome VARCHAR(255) NOT NULL,
                                              tipo VARCHAR(20) NOT NULL,
                                              valor_unitario NUMERIC(19, 2) NOT NULL,
                                              quantidade INTEGER NOT NULL,
                                              valor_total NUMERIC(19, 2) NOT NULL,
                                              status VARCHAR(20) NOT NULL,
                                              ordem INTEGER NOT NULL,

                                              PRIMARY KEY (ordem_servico_id, ordem),

                                              CONSTRAINT fk_os_item_necessario_ordem_servico
                                                  FOREIGN KEY (ordem_servico_id)
                                                      REFERENCES ordem_servico(id)
                                                      ON DELETE CASCADE,

                                              CONSTRAINT chk_os_item_necessario_tipo
                                                  CHECK (tipo IN ('PECA', 'INSUMO')),

                                              CONSTRAINT chk_os_item_necessario_status
                                                  CHECK (status IN ('DISPONIVEL', 'PENDENTE', 'UTILIZADO', 'CANCELADO'))
);
