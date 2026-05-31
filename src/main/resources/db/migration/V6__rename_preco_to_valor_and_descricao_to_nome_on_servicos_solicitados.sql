ALTER TABLE ordem_servico_servico_solicitado
    RENAME COLUMN preco TO valor;

ALTER TABLE ordem_servico_servico_solicitado
    RENAME COLUMN descricao TO nome;
