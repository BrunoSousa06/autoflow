DROP TABLE IF EXISTS public.ordem_servico_servico_item_necessario;

CREATE TABLE public.ordem_servico_servico_item_necessario (
                                                              servico_solicitado_id BIGINT NOT NULL,
                                                              ordem INTEGER NOT NULL,

                                                              peca_insumo_id BIGINT,
                                                              nome VARCHAR(255),
                                                              tipo VARCHAR(50),
                                                              quantidade INTEGER,
                                                              quantidade_disponivel INTEGER,
                                                              valor_unitario NUMERIC(19, 2),
                                                              valor_total NUMERIC(19, 2),
                                                              status VARCHAR(50),
                                                              motivo_pendencia VARCHAR(255),
                                                              mensagem_status VARCHAR(255),

                                                              CONSTRAINT pk_ordem_servico_servico_item_necessario
                                                                  PRIMARY KEY (servico_solicitado_id, ordem),

                                                              CONSTRAINT fk_item_necessario_servico_solicitado
                                                                  FOREIGN KEY (servico_solicitado_id)
                                                                      REFERENCES public.ordem_servico_servico_solicitado(id)
                                                                      ON DELETE CASCADE
);