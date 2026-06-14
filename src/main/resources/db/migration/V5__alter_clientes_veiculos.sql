-- 1. Ajustes na tabela 'clientes'

-- Remove a coluna 'senha' que não existe na entidade ClienteEntity
ALTER TABLE clientes DROP COLUMN IF EXISTS senha;

-- Adiciona a chave estrangeira para a tabela 'usuarios' (@OneToOne no Java)
ALTER TABLE clientes ADD COLUMN usuario_id BIGINT;
ALTER TABLE clientes ADD CONSTRAINT fk_clientes_usuarios
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

-- Altera o nome e tamanho da coluna 'cpf' para 'cpf_cnpj' (suportando 14 dígitos de CNPJ)
ALTER TABLE clientes RENAME COLUMN cpf TO cpf_cnpj;
ALTER TABLE clientes ALTER COLUMN cpf_cnpj TYPE VARCHAR(14);

-- Altera o tipo de 'telefone' de BIGINT para VARCHAR para alinhar com a String do Java
ALTER TABLE clientes ALTER COLUMN telefone TYPE VARCHAR(20);


-- 2. Ajustes na tabela 'veiculos'

-- Altera o tipo da coluna 'ano' de BIGINT para INTEGER (alinhado com o mapeamento e boas práticas)
ALTER TABLE clientes ALTER COLUMN telefone TYPE VARCHAR(20);
ALTER TABLE veiculos ALTER COLUMN ano TYPE INTEGER;