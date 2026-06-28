ALTER TABLE ordem_servico
DROP CONSTRAINT IF EXISTS fk_ordem_servico_cliente;

ALTER TABLE ordem_servico
    ADD COLUMN IF NOT EXISTS cliente_nome VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cliente_cpf_cnpj VARCHAR(50),
    ADD COLUMN IF NOT EXISTS cliente_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cliente_telefone VARCHAR(50);

UPDATE ordem_servico os
SET
    cliente_nome = c.nome,
    cliente_cpf_cnpj = c.cpf_cnpj,
    cliente_email = c.email,
    cliente_telefone = c.telefone
    FROM veiculos v
JOIN clientes c ON c.id = v.cliente_id
WHERE os.veiculo_id = v.id
  AND (os.cliente_nome IS NULL OR os.cliente_email IS NULL);

