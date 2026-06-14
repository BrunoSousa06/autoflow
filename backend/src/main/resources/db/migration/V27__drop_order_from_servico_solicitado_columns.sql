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
DECLARE
pk_name TEXT;
BEGIN
SELECT conname
INTO pk_name
FROM pg_constraint
WHERE conrelid = 'public.ordem_servico_servico_solicitado'::regclass
      AND contype = 'p';

IF pk_name IS NOT NULL THEN
        EXECUTE format(
            'ALTER TABLE public.ordem_servico_servico_solicitado DROP CONSTRAINT %I',
            pk_name
        );
END IF;
END $$;


ALTER TABLE public.ordem_servico_servico_solicitado
    ADD CONSTRAINT pk_ordem_servico_servico_solicitado PRIMARY KEY (id);


ALTER TABLE public.ordem_servico_servico_solicitado
    ALTER COLUMN ordem DROP NOT NULL;