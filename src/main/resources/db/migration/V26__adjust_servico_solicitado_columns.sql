ALTER TABLE public.ordem_servico_servico_solicitado
    ADD COLUMN IF NOT EXISTS status VARCHAR(50);

UPDATE public.ordem_servico_servico_solicitado
SET status = 'AGUARDANDO'
WHERE status IS NULL;

ALTER TABLE public.ordem_servico_servico_solicitado
    ALTER COLUMN status SET DEFAULT 'AGUARDANDO';

ALTER TABLE public.ordem_servico_servico_solicitado
    ALTER COLUMN status SET NOT NULL;


ALTER TABLE public.ordem_servico_servico_solicitado
    ADD COLUMN IF NOT EXISTS reparo_adicional_id BIGINT;


ALTER TABLE public.ordem_servico_servico_solicitado
    ADD COLUMN IF NOT EXISTS id BIGINT;

CREATE SEQUENCE IF NOT EXISTS public.ordem_servico_servico_solicitado_id_seq;

ALTER SEQUENCE public.ordem_servico_servico_solicitado_id_seq
    OWNED BY public.ordem_servico_servico_solicitado.id;

UPDATE public.ordem_servico_servico_solicitado
SET id = nextval('public.ordem_servico_servico_solicitado_id_seq')
WHERE id IS NULL;

ALTER TABLE public.ordem_servico_servico_solicitado
    ALTER COLUMN id SET DEFAULT nextval('public.ordem_servico_servico_solicitado_id_seq');

ALTER TABLE public.ordem_servico_servico_solicitado
    ALTER COLUMN id SET NOT NULL;


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'public.ordem_servico_servico_solicitado'::regclass
          AND contype = 'p'
    ) THEN
ALTER TABLE public.ordem_servico_servico_solicitado
    ADD CONSTRAINT pk_ordem_servico_servico_solicitado PRIMARY KEY (id);
END IF;
END $$;

ALTER TABLE public.ordem_servico_servico_solicitado
    ADD COLUMN IF NOT EXISTS reparo_adicional_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_servico_solicitado_reparo_adicional'
    ) THEN
ALTER TABLE public.ordem_servico_servico_solicitado
    ADD CONSTRAINT fk_servico_solicitado_reparo_adicional
        FOREIGN KEY (reparo_adicional_id)
            REFERENCES public.reparo_adicional(id);
END IF;
END $$;