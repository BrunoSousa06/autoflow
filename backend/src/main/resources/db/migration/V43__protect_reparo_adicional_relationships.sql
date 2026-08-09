-- Protege as cardinalidades assumidas pelos gateways e o versionamento por tipo.
CREATE UNIQUE INDEX IF NOT EXISTS uq_reparo_adicional_orcamento_id
    ON public.reparo_adicional (orcamento_id)
    WHERE orcamento_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_orcamento_os_tipo_versao
    ON public.orcamento (ordem_servico_id, tipo, versao);

ALTER TABLE public.reparo_adicional
    ADD CONSTRAINT fk_reparo_adicional_ordem_servico
        FOREIGN KEY (ordem_servico_id)
        REFERENCES public.ordem_servico(id);

ALTER TABLE public.reparo_adicional
    ADD CONSTRAINT fk_reparo_adicional_mecanico
        FOREIGN KEY (mecanico_id)
        REFERENCES public.usuarios(id);

ALTER TABLE public.reparo_adicional
    ADD CONSTRAINT fk_reparo_adicional_orcamento
        FOREIGN KEY (orcamento_id)
        REFERENCES public.orcamento(id);
