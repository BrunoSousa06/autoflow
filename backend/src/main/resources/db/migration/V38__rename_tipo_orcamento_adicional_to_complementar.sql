ALTER TABLE orcamento DROP CONSTRAINT IF EXISTS orcamentos_tipo_check;
ALTER TABLE orcamento DROP CONSTRAINT IF EXISTS chk_orcamento_tipo;

UPDATE orcamento SET tipo = 'COMPLEMENTAR' WHERE tipo = 'ADICIONAL';

ALTER TABLE orcamento ADD CONSTRAINT chk_orcamento_tipo CHECK (tipo IN ('PRINCIPAL', 'COMPLEMENTAR'));
