-- Criação da tabela de Usuários
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nome VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          senha VARCHAR(255) NOT NULL,
                          role VARCHAR(50) NOT NULL
);

-- Criação da tabela de Serviços
CREATE TABLE servicos (
                          id BIGSERIAL PRIMARY KEY,
                          nome VARCHAR(255) NOT NULL UNIQUE,
                          valor NUMERIC(19, 2) NOT NULL
);

-- Criação da tabela de Peças e Insumos
CREATE TABLE pecas_insumos (
                               id BIGSERIAL PRIMARY KEY,
                               nome VARCHAR(255) NOT NULL UNIQUE,
                               quantidade INTEGER NOT NULL,
                               valor NUMERIC(19, 2) NOT NULL
);

