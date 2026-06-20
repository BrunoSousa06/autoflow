-- =============================================================
-- SEED DE DADOS BÁSICOS PARA DESENVOLVIMENTO
-- Senha padrão de todos os usuários: Senha@1234
-- =============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- -------------------------------------------------------------
-- USUÁRIOS
-- -------------------------------------------------------------
INSERT INTO usuarios (nome, email, senha, role) VALUES
    ('Administrador',    'admin@autoflow.com',       crypt('Senha@1234', gen_salt('bf', 10)), 'ADMIN'),
    ('Atendente',        'atendente@autoflow.com',   crypt('Senha@1234', gen_salt('bf', 10)), 'ATENDENTE'),
    ('Carlos Mecânico',  'mecanico1@autoflow.com',   crypt('Senha@1234', gen_salt('bf', 10)), 'MECANICO'),
    ('Paulo Mecânico',   'mecanico2@autoflow.com',   crypt('Senha@1234', gen_salt('bf', 10)), 'MECANICO'),
    ('João Cliente',     'cliente@autoflow.com',     crypt('Senha@1234', gen_salt('bf', 10)), 'CLIENTE')
ON CONFLICT (email) DO NOTHING;

-- -------------------------------------------------------------
-- CLIENTE DE EXEMPLO (vinculado ao usuário CLIENTE)
-- -------------------------------------------------------------
INSERT INTO clientes (nome, cpf_cnpj, telefone, email, usuario_id)
SELECT 'João da Silva', '11144477735', '11988887777', 'cliente@autoflow.com', u.id
FROM usuarios u
WHERE u.email = 'cliente@autoflow.com'
ON CONFLICT (email) DO NOTHING;

-- -------------------------------------------------------------
-- VEÍCULO DO CLIENTE DE EXEMPLO
-- -------------------------------------------------------------
INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Toyota', 'Corolla', 2021, 'ABC1D23', c.id
FROM clientes c
WHERE c.cpf_cnpj = '11144477735'
ON CONFLICT (placa) DO NOTHING;

-- -------------------------------------------------------------
-- SERVIÇOS
-- -------------------------------------------------------------
INSERT INTO servicos (nome, valor, descricao) VALUES
    ('Troca de óleo e filtro',
     120.00,
     'Substituição do óleo do motor e filtro de óleo conforme especificação do fabricante.'),

    ('Alinhamento e balanceamento',
     150.00,
     'Alinhamento da direção e balanceamento dos quatro pneus.'),

    ('Revisão de freios',
     200.00,
     'Inspeção e substituição de pastilhas, discos e fluido de freio quando necessário.'),

    ('Troca de correia dentada',
     380.00,
     'Substituição da correia dentada e tensores conforme quilometragem recomendada pelo fabricante.'),

    ('Diagnóstico eletrônico',
     90.00,
     'Leitura e análise dos sistemas eletrônicos do veículo via scanner OBD-II.'),

    ('Troca de amortecedores',
     450.00,
     'Substituição dos amortecedores dianteiros e/ou traseiros com verificação da suspensão.'),

    ('Revisão completa 30.000 km',
     520.00,
     'Revisão completa com troca de fluidos, filtros, velas e verificação geral de todos os sistemas.'),

    ('Troca de velas de ignição',
     130.00,
     'Substituição das velas de ignição de acordo com a especificação do veículo.')
ON CONFLICT (nome) DO NOTHING;

-- -------------------------------------------------------------
-- PEÇAS E INSUMOS
-- -------------------------------------------------------------
INSERT INTO pecas_insumos (nome, quantidade, valor, tipo) VALUES
    ('Óleo de motor 5W30 sintético (1L)',   50,  28.90,  'INSUMO'),
    ('Óleo de motor 5W40 sintético (1L)',   40,  34.90,  'INSUMO'),
    ('Fluido de freio DOT4 (500ml)',         30,  22.00,  'INSUMO'),
    ('Fluido de arrefecimento (1L)',         25,  18.50,  'INSUMO'),
    ('Filtro de óleo',                       30,  35.00,  'PECA'),
    ('Filtro de ar',                         25,  45.00,  'PECA'),
    ('Filtro de combustível',                20,  40.00,  'PECA'),
    ('Pastilha de freio dianteira (jogo)',   15,  95.00,  'PECA'),
    ('Pastilha de freio traseira (jogo)',    10,  85.00,  'PECA'),
    ('Disco de freio dianteiro',             12, 140.00,  'PECA'),
    ('Correia dentada (kit completo)',       10, 180.00,  'PECA'),
    ('Vela de ignição (unidade)',            60,  28.00,  'PECA'),
    ('Amortecedor dianteiro',                8,  220.00,  'PECA'),
    ('Amortecedor traseiro',                 8,  195.00,  'PECA')
ON CONFLICT (nome) DO NOTHING;
