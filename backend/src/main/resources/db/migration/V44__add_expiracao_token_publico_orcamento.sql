ALTER TABLE orcamento
    ADD COLUMN IF NOT EXISTS public_token_expira_em TIMESTAMP;

UPDATE orcamento
SET public_token_expira_em = disponibilizado_em + INTERVAL '7 days'
WHERE public_token_hash IS NOT NULL
  AND public_token_expira_em IS NULL
  AND disponibilizado_em IS NOT NULL;
