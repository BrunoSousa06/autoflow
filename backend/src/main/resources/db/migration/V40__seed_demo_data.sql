-- =============================================================
-- SEED DE DEMONSTRAÇÃO — dados completos para demo
-- Cobre todos os status de OS, orçamento, serviço, item e reparo.
-- Senha padrão de todos os usuários: Senha@1234
-- =============================================================

-- -------------------------------------------------------------
-- NOVOS USUÁRIOS
-- -------------------------------------------------------------
INSERT INTO usuarios (nome, email, senha, role) VALUES
    ('Rodrigo Mecânico',   'mecanico3@autoflow.com',    crypt('Senha@1234', gen_salt('bf', 10)), 'MECANICO'),
    ('Fernanda Atendente', 'atendente2@autoflow.com',   crypt('Senha@1234', gen_salt('bf', 10)), 'ATENDENTE'),
    ('Ana Oliveira',       'ana@exemplo.com',           crypt('Senha@1234', gen_salt('bf', 10)), 'CLIENTE'),
    ('Pedro Santos',       'pedro@exemplo.com',         crypt('Senha@1234', gen_salt('bf', 10)), 'CLIENTE'),
    ('XYZ Automóveis',     'contato@xyz.com.br',        crypt('Senha@1234', gen_salt('bf', 10)), 'CLIENTE'),
    ('Fernanda Lima',      'fernanda@exemplo.com',      crypt('Senha@1234', gen_salt('bf', 10)), 'CLIENTE')
ON CONFLICT (email) DO NOTHING;

-- -------------------------------------------------------------
-- NOVOS CLIENTES
-- -------------------------------------------------------------
INSERT INTO clientes (nome, cpf_cnpj, telefone, email, usuario_id)
SELECT 'Ana Oliveira', '22255588814', '11977776666', 'ana@exemplo.com', u.id
FROM usuarios u WHERE u.email = 'ana@exemplo.com'
ON CONFLICT (email) DO NOTHING;

INSERT INTO clientes (nome, cpf_cnpj, telefone, email, usuario_id)
SELECT 'Pedro Santos', '33366699926', '11966665555', 'pedro@exemplo.com', u.id
FROM usuarios u WHERE u.email = 'pedro@exemplo.com'
ON CONFLICT (email) DO NOTHING;

INSERT INTO clientes (nome, cpf_cnpj, telefone, email, usuario_id)
SELECT 'XYZ Automóveis Ltda', '12345678000195', '1133334444', 'contato@xyz.com.br', u.id
FROM usuarios u WHERE u.email = 'contato@xyz.com.br'
ON CONFLICT (email) DO NOTHING;

INSERT INTO clientes (nome, cpf_cnpj, telefone, email, usuario_id)
SELECT 'Fernanda Lima', '44477700815', '11955554444', 'fernanda@exemplo.com', u.id
FROM usuarios u WHERE u.email = 'fernanda@exemplo.com'
ON CONFLICT (email) DO NOTHING;

-- -------------------------------------------------------------
-- NOVOS VEÍCULOS
-- -------------------------------------------------------------
INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Honda', 'Civic', 2020, 'XYZ2A34', c.id
FROM clientes c WHERE c.cpf_cnpj = '11144477735'
ON CONFLICT (placa) DO NOTHING;

INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Fiat', 'Strada', 2022, 'DEF3G45', c.id
FROM clientes c WHERE c.cpf_cnpj = '22255588814'
ON CONFLICT (placa) DO NOTHING;

INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Volkswagen', 'Golf', 2019, 'GHI4J56', c.id
FROM clientes c WHERE c.cpf_cnpj = '33366699926'
ON CONFLICT (placa) DO NOTHING;

INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Ford', 'Ka', 2018, 'JKL5M67', c.id
FROM clientes c WHERE c.cpf_cnpj = '33366699926'
ON CONFLICT (placa) DO NOTHING;

INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Hyundai', 'HB20', 2023, 'MNO6P78', c.id
FROM clientes c WHERE c.cpf_cnpj = '12345678000195'
ON CONFLICT (placa) DO NOTHING;

INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
SELECT 'Chevrolet', 'Onix', 2021, 'PQR7S89', c.id
FROM clientes c WHERE c.cpf_cnpj = '44477700815'
ON CONFLICT (placa) DO NOTHING;

-- =============================================================
-- ORDENS DE SERVIÇO
-- Cenários cobertos:
--   OS-1: ENTREGUE          (João / Corolla)
--   OS-2: FINALIZADA        (Ana / Strada)
--   OS-3: EM_EXECUCAO       (Pedro / Golf)  — reparo adicional RECUSADO
--   OS-4: AGUARDANDO_APROV. (João / Civic)  — orçamento DISPONIVEL
--   OS-5: AGUARDANDO_APROV. (Ana / Strada)  — v1 SUBSTITUIDO, v2 REPROVADO, v3 DISPONIVEL
--   OS-6: EM_DIAGNOSTICO    (Pedro / Ford Ka)
--   OS-7: RECEBIDA          (XYZ / HB20)
--   OS-8: EM_EXECUCAO       (Fernanda / Onix) — reparo APROVADO + COMPLEMENTAR APROVADO
--                                               + reparo PENDENTE_APROVACAO + COMPLEMENTAR DISPONIVEL
-- =============================================================

-- ---- OS-1: ENTREGUE ------------------------------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1730372400001',
    c.id, v.id,
    'ENTREGUE',
    '2024-11-01 08:00:00',
    m.id,
    '2024-11-05 08:30:00', '2024-11-07 16:00:00',
    'Troca de óleo necessária. Pastilhas de freio dianteiras desgastadas abaixo do limite seguro.',
    'João da Silva', '11144477735', 'cliente@autoflow.com', '11988887777',
    '2024-11-08 08:00:00', '2024-11-11 17:00:00', '2024-11-12 10:00:00', '2024-11-12 10:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'ABC1D23'
JOIN usuarios m ON m.email = 'mecanico1@autoflow.com'
WHERE c.cpf_cnpj = '11144477735'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-2: FINALIZADA ----------------------------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1732964400002',
    c.id, v.id,
    'FINALIZADA',
    '2024-11-30 09:00:00',
    m.id,
    '2024-12-02 08:00:00', '2024-12-04 17:00:00',
    'Pneus desalinhados. Velas de ignição com desgaste além do recomendado pelo fabricante.',
    'Ana Oliveira', '22255588814', 'ana@exemplo.com', '11977776666',
    '2024-12-05 08:00:00', '2024-12-08 16:00:00', NULL, '2024-12-08 16:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'DEF3G45'
JOIN usuarios m ON m.email = 'mecanico2@autoflow.com'
WHERE c.cpf_cnpj = '22255588814'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-3: EM_EXECUCAO ---------------------------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1735728000003',
    c.id, v.id,
    'EM_EXECUCAO',
    '2025-01-01 10:00:00',
    m.id,
    '2025-01-03 08:00:00', '2025-01-06 17:00:00',
    'Correia dentada próxima do limite. Freios traseiros desgastados. Diagnóstico eletrônico sem falhas críticas.',
    'Pedro Santos', '33366699926', 'pedro@exemplo.com', '11966665555',
    '2025-01-10 08:00:00', NULL, NULL, '2025-01-10 08:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'GHI4J56'
JOIN usuarios m ON m.email = 'mecanico1@autoflow.com'
WHERE c.cpf_cnpj = '33366699926'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-4: AGUARDANDO_APROVACAO ------------------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1738406400004',
    c.id, v.id,
    'AGUARDANDO_APROVACAO',
    '2025-02-01 09:00:00',
    m.id,
    '2025-02-03 08:00:00', '2025-02-08 15:00:00',
    'Revisão dos 30.000 km. Troca de todos os filtros, fluidos e velas conforme manual do fabricante.',
    'João da Silva', '11144477735', 'cliente@autoflow.com', '11988887777',
    NULL, NULL, NULL, '2025-02-08 15:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'XYZ2A34'
JOIN usuarios m ON m.email = 'mecanico2@autoflow.com'
WHERE c.cpf_cnpj = '11144477735'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-5: AGUARDANDO_APROVACAO (v1 SUBSTITUIDO, v2 REPROVADO, v3 DISPONIVEL) ---
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1740844800005',
    c.id, v.id,
    'AGUARDANDO_APROVACAO',
    '2025-03-01 08:00:00',
    m.id,
    '2025-03-03 08:00:00', '2025-03-05 17:00:00',
    'Amortecedores dianteiros com vazamento de óleo. Recomendada substituição em pares.',
    'Ana Oliveira', '22255588814', 'ana@exemplo.com', '11977776666',
    NULL, NULL, NULL, '2025-03-15 10:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'DEF3G45'
JOIN usuarios m ON m.email = 'mecanico3@autoflow.com'
WHERE c.cpf_cnpj = '22255588814'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-6: EM_DIAGNOSTICO ------------------------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1743523200006',
    c.id, v.id,
    'EM_DIAGNOSTICO',
    '2025-04-01 10:00:00',
    m.id,
    '2025-04-03 08:00:00', NULL, NULL,
    'Pedro Santos', '33366699926', 'pedro@exemplo.com', '11966665555',
    NULL, NULL, NULL, '2025-04-03 08:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'JKL5M67'
JOIN usuarios m ON m.email = 'mecanico3@autoflow.com'
WHERE c.cpf_cnpj = '33366699926'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-7: RECEBIDA ------------------------------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1748736000007',
    c.id, v.id,
    'RECEBIDA',
    '2025-06-01 11:00:00',
    NULL, NULL, NULL, NULL,
    'XYZ Automóveis Ltda', '12345678000195', 'contato@xyz.com.br', '1133334444',
    NULL, NULL, NULL, '2025-06-01 11:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'MNO6P78'
WHERE c.cpf_cnpj = '12345678000195'
ON CONFLICT (numero_os) DO NOTHING;

-- ---- OS-8: EM_EXECUCAO + reparo adicional --------------------
INSERT INTO ordem_servico (
    numero_os, cliente_id, veiculo_id, status, data_abertura,
    diagnostico_mecanico_id, diagnostico_iniciado_em, diagnostico_concluido_em, diagnostico_laudo,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    execucao_iniciada_em, finalizada_em, entregue_em, ultima_atualizacao
)
SELECT
    'OS-1746144000008',
    c.id, v.id,
    'EM_EXECUCAO',
    '2025-05-02 08:00:00',
    m.id,
    '2025-05-04 08:00:00', '2025-05-06 16:00:00',
    'Troca de óleo e filtro no prazo. Durante inspeção: pneus desalinhados e sistema de escapamento com folga.',
    'Fernanda Lima', '44477700815', 'fernanda@exemplo.com', '11955554444',
    '2025-05-08 08:00:00', NULL, NULL, '2025-05-15 10:00:00'
FROM clientes c
JOIN veiculos v ON v.placa = 'PQR7S89'
JOIN usuarios m ON m.email = 'mecanico1@autoflow.com'
WHERE c.cpf_cnpj = '44477700815'
ON CONFLICT (numero_os) DO NOTHING;

-- =============================================================
-- SERVIÇOS SOLICITADOS POR OS
-- =============================================================

-- OS-1: Troca de óleo + Revisão de freios (ambos FINALIZADO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'FINALIZADO', '2024-11-08 08:00:00', '2024-11-09 12:00:00'
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1730372400001' AND s.nome = 'Troca de óleo e filtro';

INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 2, 'FINALIZADO', '2024-11-09 13:00:00', '2024-11-11 17:00:00'
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1730372400001' AND s.nome = 'Revisão de freios';

-- OS-2: Alinhamento + Velas (ambos FINALIZADO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'FINALIZADO', '2024-12-05 08:00:00', '2024-12-06 14:00:00'
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1732964400002' AND s.nome = 'Alinhamento e balanceamento';

INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 2, 'FINALIZADO', '2024-12-06 15:00:00', '2024-12-08 16:00:00'
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1732964400002' AND s.nome = 'Troca de velas de ignição';

-- OS-3: Correia (EM_EXECUCAO) + Freios (AGUARDANDO) + Diagnóstico (CANCELADO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'EM_EXECUCAO', '2025-01-10 08:00:00', NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1735728000003' AND s.nome = 'Troca de correia dentada';

INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 2, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1735728000003' AND s.nome = 'Revisão de freios';

INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 3, 'CANCELADO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1735728000003' AND s.nome = 'Diagnóstico eletrônico';

-- OS-4: Revisão completa 30k (AGUARDANDO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1738406400004' AND s.nome = 'Revisão completa 30.000 km';

-- OS-5: Troca de amortecedores (AGUARDANDO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1740844800005' AND s.nome = 'Troca de amortecedores';

-- OS-6: Diagnóstico eletrônico (AGUARDANDO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1743523200006' AND s.nome = 'Diagnóstico eletrônico';

-- OS-7: Troca de óleo (AGUARDANDO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1748736000007' AND s.nome = 'Troca de óleo e filtro';

-- OS-8: Troca de óleo (EM_EXECUCAO) + Alinhamento via reparo adicional (AGUARDANDO)
INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 1, 'EM_EXECUCAO', '2025-05-08 08:00:00', NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1746144000008' AND s.nome = 'Troca de óleo e filtro';

INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 2, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1746144000008' AND s.nome = 'Alinhamento e balanceamento';

INSERT INTO ordem_servico_servico_solicitado (ordem_servico_id, servico_id, nome, valor, ordem, status, iniciado_em, finalizado_em)
SELECT os.id, s.id, s.nome, s.valor, 3, 'AGUARDANDO', NULL, NULL
FROM ordem_servico os, servicos s
WHERE os.numero_os = 'OS-1746144000008' AND s.nome = 'Troca de velas de ignição';

-- =============================================================
-- ITENS NECESSÁRIOS DOS SERVIÇOS
-- Cobertos: UTILIZADO (OS-1, OS-2), EM_EXECUCAO/PENDENTE/CANCELADO (OS-3), DISPONIVEL (OS-8)
-- NOTA: ordem é 0-based — mapeado pelo @OrderColumn do JPA (@ElementCollection em ServicoSolicitadoEntity)
-- =============================================================

-- OS-1 / Troca de óleo → Óleo 5W30 ×4 (UTILIZADO) + Filtro de óleo ×1 (UTILIZADO)
INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 0, p.id, p.nome, p.tipo, 4, 50, p.valor, p.valor * 4, 'UTILIZADO', NULL, 'Baixado do estoque em 09/11/2024'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Óleo de motor 5W30 sintético (1L)'
WHERE os.numero_os = 'OS-1730372400001' AND ss.nome = 'Troca de óleo e filtro';

INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 1, p.id, p.nome, p.tipo, 1, 30, p.valor, p.valor, 'UTILIZADO', NULL, 'Baixado do estoque em 09/11/2024'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Filtro de óleo'
WHERE os.numero_os = 'OS-1730372400001' AND ss.nome = 'Troca de óleo e filtro';

-- OS-1 / Revisão de freios → Pastilha dianteira ×1 (UTILIZADO) + Fluido de freio ×1 (UTILIZADO)
INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 0, p.id, p.nome, p.tipo, 1, 15, p.valor, p.valor, 'UTILIZADO', NULL, 'Baixado do estoque em 11/11/2024'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Pastilha de freio dianteira (jogo)'
WHERE os.numero_os = 'OS-1730372400001' AND ss.nome = 'Revisão de freios';

INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 1, p.id, p.nome, p.tipo, 1, 30, p.valor, p.valor, 'UTILIZADO', NULL, 'Baixado do estoque em 11/11/2024'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Fluido de freio DOT4 (500ml)'
WHERE os.numero_os = 'OS-1730372400001' AND ss.nome = 'Revisão de freios';

-- OS-2 / Alinhamento → sem peças físicas (serviço de mão de obra)
-- OS-2 / Velas → Vela de ignição ×4 (UTILIZADO)
INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 0, p.id, p.nome, p.tipo, 4, 60, p.valor, p.valor * 4, 'UTILIZADO', NULL, 'Baixado do estoque em 07/12/2024'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Vela de ignição (unidade)'
WHERE os.numero_os = 'OS-1732964400002' AND ss.nome = 'Troca de velas de ignição';

-- OS-3 / Correia dentada → Kit correia (UTILIZADO) + Fluido arrefecimento (PENDENTE — estoque zerado)
INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 0, p.id, p.nome, p.tipo, 1, 10, p.valor, p.valor, 'UTILIZADO', NULL, 'Baixado do estoque em 10/01/2025'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Correia dentada (kit completo)'
WHERE os.numero_os = 'OS-1735728000003' AND ss.nome = 'Troca de correia dentada';

INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 1, p.id, p.nome, p.tipo, 1, 0, p.valor, p.valor, 'PENDENTE', 'ESTOQUE_INSUFICIENTE', 'Pendência de peça'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Fluido de arrefecimento (1L)'
WHERE os.numero_os = 'OS-1735728000003' AND ss.nome = 'Troca de correia dentada';

-- OS-3 / Revisão de freios → Pastilha dianteira (DISPONIVEL) + Disco dianteiro (CANCELADO)
INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 0, p.id, p.nome, p.tipo, 1, 15, p.valor, p.valor, 'DISPONIVEL', NULL, NULL
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Pastilha de freio dianteira (jogo)'
WHERE os.numero_os = 'OS-1735728000003' AND ss.nome = 'Revisão de freios';

INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 1, p.id, p.nome, p.tipo, 1, 12, p.valor, p.valor, 'CANCELADO', NULL, 'Item removido do escopo após inspeção visual'
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Disco de freio dianteiro'
WHERE os.numero_os = 'OS-1735728000003' AND ss.nome = 'Revisão de freios';

-- OS-8 / Troca de óleo → Óleo 5W40 ×4 (DISPONIVEL) + Filtro ×1 (DISPONIVEL)
INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 0, p.id, p.nome, p.tipo, 4, 40, p.valor, p.valor * 4, 'DISPONIVEL', NULL, NULL
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Óleo de motor 5W40 sintético (1L)'
WHERE os.numero_os = 'OS-1746144000008' AND ss.nome = 'Troca de óleo e filtro';

INSERT INTO ordem_servico_servico_item_necessario
    (servico_solicitado_id, ordem, peca_insumo_id, nome, tipo, quantidade, quantidade_disponivel, valor_unitario, valor_total, status, motivo_pendencia, mensagem_status)
SELECT ss.id, 1, p.id, p.nome, p.tipo, 1, 30, p.valor, p.valor, 'DISPONIVEL', NULL, NULL
FROM ordem_servico_servico_solicitado ss
JOIN ordem_servico os ON os.id = ss.ordem_servico_id
JOIN pecas_insumos p ON p.nome = 'Filtro de óleo'
WHERE os.numero_os = 'OS-1746144000008' AND ss.nome = 'Troca de óleo e filtro';

-- =============================================================
-- ORÇAMENTOS
-- OS-1: PRINCIPAL v1 APROVADO
-- OS-2: PRINCIPAL v1 APROVADO
-- OS-3: PRINCIPAL v1 APROVADO
-- OS-4: PRINCIPAL v1 DISPONIVEL
-- OS-5: PRINCIPAL v1 SUBSTITUIDO, v2 REPROVADO, v3 DISPONIVEL
-- OS-8: PRINCIPAL v1 APROVADO, COMPLEMENTAR v1 APROVADO, COMPLEMENTAR v2 DISPONIVEL
-- =============================================================

-- OS-1: orçamento PRINCIPAL v1 APROVADO
-- total_servicos: 120+200=320 | total_itens: 4×28.90+35+95+22=267.60 | total=587.60
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 1, 'APROVADO',
    '2024-11-07 16:30:00', '2024-11-07 17:00:00', '2024-11-08 07:30:00', NULL,
    320.00, 267.60, 587.60,
    'João da Silva', NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'ABC1D23'
WHERE os.numero_os = 'OS-1730372400001';

-- OS-2: orçamento PRINCIPAL v1 APROVADO
-- total_servicos: 150+130=280 | total_itens: 4×28=112 | total=392
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 1, 'APROVADO',
    '2024-12-04 17:30:00', '2024-12-04 18:00:00', '2024-12-05 07:45:00', NULL,
    280.00, 112.00, 392.00,
    'Ana Oliveira', NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'DEF3G45'
WHERE os.numero_os = 'OS-1732964400002';

-- OS-3: orçamento PRINCIPAL v1 APROVADO
-- total_servicos: 380+200=580 | total_itens: 180+95+22+140=437 | total=1017
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 1, 'APROVADO',
    '2025-01-06 17:30:00', '2025-01-06 18:00:00', '2025-01-07 08:00:00', NULL,
    580.00, 437.00, 1017.00,
    'Pedro Santos', NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'GHI4J56'
WHERE os.numero_os = 'OS-1735728000003';

-- OS-4: orçamento PRINCIPAL v1 DISPONIVEL
-- total_servicos: 520 | total_itens: 4×28.90+45+40+4×28+18.50=331.10 | total=851.10
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 1, 'DISPONIVEL',
    '2025-02-08 15:30:00', '2025-02-08 16:00:00', NULL, NULL,
    520.00, 331.10, 851.10,
    NULL, NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'XYZ2A34'
WHERE os.numero_os = 'OS-1738406400004';

-- OS-5: PRINCIPAL v1 SUBSTITUIDO (substituído antes de aprovação por nova versão)
-- total_servicos: 450 | total_itens: 2×220=440 | total=890
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 1, 'SUBSTITUIDO',
    '2025-03-05 17:30:00', '2025-03-05 18:00:00', NULL, NULL,
    450.00, 440.00, 890.00,
    NULL, NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'DEF3G45'
WHERE os.numero_os = 'OS-1740844800005';

-- OS-5: PRINCIPAL v2 REPROVADO (cliente recusou — orçamento incluía dianteiro + traseiro)
-- total_servicos: 450 | total_itens: 2×220+2×195=830 | total=1280
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 2, 'REPROVADO',
    '2025-03-06 09:00:00', '2025-03-06 09:30:00', NULL, '2025-03-07 11:00:00',
    450.00, 830.00, 1280.00,
    NULL, 'Valor acima do esperado. Solicito orçamento apenas para amortecedores dianteiros.', os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'DEF3G45'
WHERE os.numero_os = 'OS-1740844800005';

-- OS-5: PRINCIPAL v3 DISPONIVEL (somente dianteiros)
-- total_servicos: 450 | total_itens: 2×220=440 | total=890
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 3, 'DISPONIVEL',
    '2025-03-08 10:00:00', '2025-03-08 10:30:00', NULL, NULL,
    450.00, 440.00, 890.00,
    NULL, NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'DEF3G45'
WHERE os.numero_os = 'OS-1740844800005';

-- OS-8: PRINCIPAL v1 APROVADO
-- total_servicos: 120 | total_itens: 4×34.90+35=174.60 | total=294.60
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'PRINCIPAL', 1, 'APROVADO',
    '2025-05-06 16:30:00', '2025-05-06 17:00:00', '2025-05-07 08:00:00', NULL,
    120.00, 174.60, 294.60,
    'Fernanda Lima', NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'PQR7S89'
WHERE os.numero_os = 'OS-1746144000008';

-- OS-8: COMPLEMENTAR v1 APROVADO (reparo adicional — alinhamento)
-- total_servicos: 150 | total_itens: 0 | total=150
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'COMPLEMENTAR', 1, 'APROVADO',
    '2025-05-10 09:00:00', '2025-05-10 09:30:00', '2025-05-11 08:00:00', NULL,
    150.00, 0.00, 150.00,
    'Fernanda Lima', NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'PQR7S89'
WHERE os.numero_os = 'OS-1746144000008';

-- OS-8: COMPLEMENTAR v2 DISPONIVEL (reparo adicional — velas, aguardando aprovação)
-- total_servicos: 130 | total_itens: 4×28=112 | total=242
INSERT INTO orcamento (
    ordem_servico_id, tipo, versao, status,
    criado_em, disponibilizado_em, aprovado_em, reprovado_em,
    total_servicos, total_itens, total_geral,
    assinatura_nome, recusa_motivo, numero_os,
    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
)
SELECT os.id, 'COMPLEMENTAR', 2, 'DISPONIVEL',
    '2025-05-14 14:00:00', '2025-05-14 14:30:00', NULL, NULL,
    130.00, 112.00, 242.00,
    NULL, NULL, os.numero_os,
    os.cliente_nome, os.cliente_cpf_cnpj, os.cliente_email, os.cliente_telefone,
    v.placa, v.marca, v.modelo, v.ano
FROM ordem_servico os
JOIN veiculos v ON v.placa = 'PQR7S89'
WHERE os.numero_os = 'OS-1746144000008';

-- =============================================================
-- ITENS DO ORÇAMENTO (orcamento_servico_item)
-- NOTA: ordem é 0-based — mapeado pelo @OrderColumn do JPA (@ElementCollection em OrcamentoEntity)
-- =============================================================

-- OS-1 orçamento PRINCIPAL v1
INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Troca de óleo e filtro';

INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Revisão de freios';

-- OS-2 orçamento PRINCIPAL v1
INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1732964400002' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Alinhamento e balanceamento';

INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1732964400002' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Troca de velas de ignição';

-- OS-3 orçamento PRINCIPAL v1
INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Troca de correia dentada';

INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Revisão de freios';

-- OS-4 orçamento PRINCIPAL v1
INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Revisão completa 30.000 km';

-- OS-5 orçamentos v1, v2, v3 (mesmo serviço, valores iguais)
INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1740844800005' AND o.tipo = 'PRINCIPAL'
  AND s.nome = 'Troca de amortecedores';

-- OS-8 orçamentos
INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND s.nome = 'Troca de óleo e filtro';

INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'COMPLEMENTAR' AND o.versao = 1
  AND s.nome = 'Alinhamento e balanceamento';

INSERT INTO orcamento_servico_item (orcamento_id, servico_id, nome, valor, ordem)
SELECT o.id, s.id, s.nome, s.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, servicos s
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'COMPLEMENTAR' AND o.versao = 2
  AND s.nome = 'Troca de velas de ignição';

-- =============================================================
-- PEÇAS DO ORÇAMENTO (orcamento_item_necessario_item)
-- NOTA: ordem é 0-based — mapeado pelo @OrderColumn do JPA (@ElementCollection em OrcamentoEntity)
-- =============================================================

-- OS-1 PRINCIPAL v1: Óleo 5W30 ×4 + Filtro óleo ×1 + Pastilha ×1 + Fluido freio ×1
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 4, p.valor*4, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Óleo de motor 5W30 sintético (1L)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Filtro de óleo';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 2
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Pastilha de freio dianteira (jogo)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 3
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Fluido de freio DOT4 (500ml)';

-- OS-2 PRINCIPAL v1: Vela ×4
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 4, p.valor*4, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1732964400002' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Vela de ignição (unidade)';

-- OS-3 PRINCIPAL v1: Correia ×1 + Pastilha ×1 + Fluido freio ×1 + Disco ×1
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Correia dentada (kit completo)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Pastilha de freio dianteira (jogo)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 2
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Fluido de freio DOT4 (500ml)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 3
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Disco de freio dianteiro';

-- OS-4 PRINCIPAL v1: Óleo 5W30 ×4 + Filtro ar ×1 + Filtro combustível ×1 + Vela ×4 + Fluido arrefecimento ×1
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 4, p.valor*4, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Óleo de motor 5W30 sintético (1L)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Filtro de ar';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 2
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Filtro de combustível';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 4, p.valor*4, 3
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Vela de ignição (unidade)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 4
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Fluido de arrefecimento (1L)';

-- OS-5 v1 SUBSTITUIDO: Amortecedor dianteiro ×2
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 2, p.valor*2, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1740844800005' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Amortecedor dianteiro';

-- OS-5 v2 REPROVADO: Amortecedor dianteiro ×2 + traseiro ×2
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 2, p.valor*2, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1740844800005' AND o.tipo = 'PRINCIPAL' AND o.versao = 2
  AND p.nome = 'Amortecedor dianteiro';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 2, p.valor*2, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1740844800005' AND o.tipo = 'PRINCIPAL' AND o.versao = 2
  AND p.nome = 'Amortecedor traseiro';

-- OS-5 v3 DISPONIVEL: Amortecedor dianteiro ×2
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 2, p.valor*2, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1740844800005' AND o.tipo = 'PRINCIPAL' AND o.versao = 3
  AND p.nome = 'Amortecedor dianteiro';

-- OS-8 PRINCIPAL v1: Óleo 5W40 ×4 + Filtro ×1
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 4, p.valor*4, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Óleo de motor 5W40 sintético (1L)';

INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 1, p.valor, 1
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'PRINCIPAL' AND o.versao = 1
  AND p.nome = 'Filtro de óleo';

-- OS-8 COMPLEMENTAR v2 (velas ×4)
INSERT INTO orcamento_item_necessario_item (orcamento_id, peca_insumo_id, nome, tipo, valor_unitario, quantidade, valor_total, ordem)
SELECT o.id, p.id, p.nome, p.tipo, p.valor, 4, p.valor*4, 0
FROM orcamento o JOIN ordem_servico os ON os.id = o.ordem_servico_id, pecas_insumos p
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'COMPLEMENTAR' AND o.versao = 2
  AND p.nome = 'Vela de ignição (unidade)';

-- =============================================================
-- HISTÓRICO DE STATUS
-- =============================================================

-- OS-1: ciclo completo
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2024-11-01 08:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',                      os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1730372400001';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2024-11-05 08:30:00', 'Seu veículo está em diagnóstico técnico.',                                                  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1730372400001';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'AGUARDANDO_APROVACAO','2024-11-07 16:00:00','O orçamento está disponível e aguardando sua aprovação.',                                    os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1730372400001';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_EXECUCAO',        '2024-11-08 08:00:00', 'Os serviços aprovados estão em execução.',                                                   os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1730372400001';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'FINALIZADA',         '2024-11-11 17:00:00', 'Os serviços foram finalizados. Seu veículo está aguardando entrega.',                        os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1730372400001';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'ENTREGUE',           '2024-11-12 10:00:00', 'Seu veículo foi entregue. Obrigado por utilizar a AutoFlow.',                                os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1730372400001';

-- OS-2: até FINALIZADA
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2024-11-30 09:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1732964400002';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2024-12-02 08:00:00', 'Seu veículo está em diagnóstico técnico.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1732964400002';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'AGUARDANDO_APROVACAO','2024-12-04 17:00:00','O orçamento está disponível e aguardando sua aprovação.',               os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1732964400002';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_EXECUCAO',        '2024-12-05 08:00:00', 'Os serviços aprovados estão em execução.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1732964400002';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'FINALIZADA',         '2024-12-08 16:00:00', 'Os serviços foram finalizados. Seu veículo está aguardando entrega.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1732964400002';

-- OS-3: até EM_EXECUCAO
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2025-01-01 10:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1735728000003';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2025-01-03 08:00:00', 'Seu veículo está em diagnóstico técnico.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1735728000003';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'AGUARDANDO_APROVACAO','2025-01-06 17:30:00','O orçamento está disponível e aguardando sua aprovação.',               os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1735728000003';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_EXECUCAO',        '2025-01-10 08:00:00', 'Os serviços aprovados estão em execução.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1735728000003';

-- OS-4: até AGUARDANDO_APROVACAO
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2025-02-01 09:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1738406400004';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2025-02-03 08:00:00', 'Seu veículo está em diagnóstico técnico.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1738406400004';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'AGUARDANDO_APROVACAO','2025-02-08 15:00:00','O orçamento está disponível e aguardando sua aprovação.',               os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1738406400004';

-- OS-5: até AGUARDANDO_APROVACAO
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2025-03-01 08:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1740844800005';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2025-03-03 08:00:00', 'Seu veículo está em diagnóstico técnico.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1740844800005';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'AGUARDANDO_APROVACAO','2025-03-05 17:00:00','O orçamento está disponível e aguardando sua aprovação.',               os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1740844800005';

-- OS-6: até EM_DIAGNOSTICO
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2025-04-01 10:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1743523200006';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2025-04-03 08:00:00', 'Seu veículo está em diagnóstico técnico.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1743523200006';

-- OS-7: RECEBIDA
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2025-06-01 11:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1748736000007';

-- OS-8: até EM_EXECUCAO
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'RECEBIDA',           '2025-05-02 08:00:00', 'Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.',  os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1746144000008';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_DIAGNOSTICO',     '2025-05-04 08:00:00', 'Seu veículo está em diagnóstico técnico.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1746144000008';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'AGUARDANDO_APROVACAO','2025-05-06 16:30:00','O orçamento está disponível e aguardando sua aprovação.',               os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1746144000008';
INSERT INTO ordem_servico_status_historico (ordem_servico_id, status, registrado_em, mensagem_cliente, numero_os)
SELECT os.id, 'EM_EXECUCAO',        '2025-05-08 08:00:00', 'Os serviços aprovados estão em execução.',                             os.numero_os FROM ordem_servico os WHERE os.numero_os = 'OS-1746144000008';

-- =============================================================
-- REPAROS ADICIONAIS
-- Cobertos: RECUSADO (OS-3), APROVADO (OS-8 v1), PENDENTE_APROVACAO (OS-8 v2)
-- =============================================================

-- OS-3: reparo adicional RECUSADO (troca de amortecedores, cliente recusou)
INSERT INTO reparo_adicional (ordem_servico_id, mecanico_id, orcamento_id, status, criado_em, aprovado_em, recusado_em, motivo_recusa, numero_os)
SELECT os.id, m.id, NULL, 'RECUSADO', '2025-01-12 10:00:00', NULL, '2025-01-13 09:00:00',
    'Aguardarei mais uma revisão antes de trocar. Por ora não aprovarei o serviço adicional.',
    os.numero_os
FROM ordem_servico os, usuarios m
WHERE os.numero_os = 'OS-1735728000003' AND m.email = 'mecanico1@autoflow.com';

-- OS-8: reparo adicional v1 APROVADO (alinhamento)
INSERT INTO reparo_adicional (ordem_servico_id, mecanico_id, orcamento_id, status, criado_em, aprovado_em, recusado_em, motivo_recusa, numero_os)
SELECT os.id, m.id, o.id, 'APROVADO', '2025-05-10 09:00:00', '2025-05-11 08:00:00', NULL, NULL, os.numero_os
FROM ordem_servico os
JOIN orcamento o ON o.ordem_servico_id = os.id AND o.tipo = 'COMPLEMENTAR' AND o.versao = 1
JOIN usuarios m ON m.email = 'mecanico1@autoflow.com'
WHERE os.numero_os = 'OS-1746144000008';

-- OS-8: reparo adicional v2 PENDENTE_APROVACAO (velas de ignição)
INSERT INTO reparo_adicional (ordem_servico_id, mecanico_id, orcamento_id, status, criado_em, aprovado_em, recusado_em, motivo_recusa, numero_os)
SELECT os.id, m.id, o.id, 'PENDENTE_APROVACAO', '2025-05-14 14:00:00', NULL, NULL, NULL, os.numero_os
FROM ordem_servico os
JOIN orcamento o ON o.ordem_servico_id = os.id AND o.tipo = 'COMPLEMENTAR' AND o.versao = 2
JOIN usuarios m ON m.email = 'mecanico1@autoflow.com'
WHERE os.numero_os = 'OS-1746144000008';

-- Vincular serviços adicionais ao reparo_adicional correspondente (OS-8)
UPDATE ordem_servico_servico_solicitado ss
SET reparo_adicional_id = ra.id
FROM ordem_servico os
JOIN reparo_adicional ra ON ra.ordem_servico_id = os.id AND ra.status = 'APROVADO'
WHERE ss.ordem_servico_id = os.id
  AND os.numero_os = 'OS-1746144000008'
  AND ss.nome = 'Alinhamento e balanceamento';

UPDATE ordem_servico_servico_solicitado ss
SET reparo_adicional_id = ra.id
FROM ordem_servico os
JOIN reparo_adicional ra ON ra.ordem_servico_id = os.id AND ra.status = 'PENDENTE_APROVACAO'
WHERE ss.ordem_servico_id = os.id
  AND os.numero_os = 'OS-1746144000008'
  AND ss.nome = 'Troca de velas de ignição';

-- =============================================================
-- NOTIFICAÇÕES
-- Cobertos: ENVIADA, FALHA, PENDENTE
-- =============================================================

-- OS-1: notificação ENVIADA (orçamento disponibilizado)
INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2024-11-07 17:05:00', NULL, '2024-11-07 17:00:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '11144477735'
WHERE os.numero_os = 'OS-1730372400001' AND o.tipo = 'PRINCIPAL' AND o.versao = 1;

-- OS-2: notificação ENVIADA
INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2024-12-04 18:05:00', NULL, '2024-12-04 18:00:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '22255588814'
WHERE os.numero_os = 'OS-1732964400002' AND o.tipo = 'PRINCIPAL' AND o.versao = 1;

-- OS-3: notificação ENVIADA
INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2025-01-06 18:05:00', NULL, '2025-01-06 18:00:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '33366699926'
WHERE os.numero_os = 'OS-1735728000003' AND o.tipo = 'PRINCIPAL' AND o.versao = 1;

-- OS-4: notificação com FALHA (servidor de email indisponível) + nova tentativa ENVIADA
INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'FALHA', NULL, 'Connection timeout: smtp.autoflow.com:587', '2025-02-08 16:00:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '11144477735'
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1;

INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2025-02-08 16:35:00', NULL, '2025-02-08 16:30:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '11144477735'
WHERE os.numero_os = 'OS-1738406400004' AND o.tipo = 'PRINCIPAL' AND o.versao = 1;

-- OS-5: notificação v3 DISPONIVEL PENDENTE (aguardando envio)
INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'PENDENTE', NULL, NULL, '2025-03-08 10:30:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '22255588814'
WHERE os.numero_os = 'OS-1740844800005' AND o.tipo = 'PRINCIPAL' AND o.versao = 3;

-- OS-8: notificação PRINCIPAL ENVIADA + COMPLEMENTAR v1 ENVIADA + COMPLEMENTAR v2 ENVIADA
INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2025-05-06 17:05:00', NULL, '2025-05-06 17:00:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '44477700815'
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'PRINCIPAL' AND o.versao = 1;

INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2025-05-10 09:35:00', NULL, '2025-05-10 09:30:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '44477700815'
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'COMPLEMENTAR' AND o.versao = 1;

INSERT INTO notificacao (orcamento_id, cliente_id, canal, destinatario, status, enviada_em, mensagem_erro, criada_em)
SELECT o.id, c.id, 'EMAIL', c.email, 'ENVIADA', '2025-05-14 14:35:00', NULL, '2025-05-14 14:30:00'
FROM orcamento o
JOIN ordem_servico os ON os.id = o.ordem_servico_id
JOIN clientes c ON c.cpf_cnpj = '44477700815'
WHERE os.numero_os = 'OS-1746144000008' AND o.tipo = 'COMPLEMENTAR' AND o.versao = 2;
