# Requisitos Funcionais

## 1. Objetivo

Este documento descreve os requisitos funcionais do sistema Autoflow, responsavel pela gestao de ordens de servico de uma oficina automotiva.

## 2. Escopo Funcional

O sistema deve permitir a criacao, acompanhamento e gestao administrativa de ordens de servico, clientes, veiculos, servicos, pecas e insumos.

## 3. Requisitos

### RF001 - Identificar Cliente na Criacao da Ordem de Servico

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir identificar o cliente por CPF ou CNPJ durante a criacao de uma ordem de servico. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel informar CPF ou CNPJ valido; o sistema deve localizar cliente existente ou permitir vincular um novo cadastro; documentos invalidos devem ser rejeitados. |

### RF002 - Cadastrar Veiculo na Ordem de Servico

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir cadastrar ou vincular um veiculo a ordem de servico. |
| Atores | Usuario administrativo |
| Criterios de aceite | O cadastro deve conter placa, marca, modelo e ano; a placa deve ser validada; o veiculo deve ficar associado ao cliente e a ordem de servico. |

### RF003 - Incluir Servicos Solicitados

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir incluir os servicos solicitados pelo cliente na ordem de servico. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel incluir um ou mais servicos; cada servico deve possuir descricao e valor; exemplos de servicos incluem troca de oleo e alinhamento. |

### RF004 - Incluir Pecas e Insumos

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir incluir pecas e insumos necessarios para execucao dos servicos da ordem de servico. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel associar uma ou mais pecas ou insumos a ordem de servico; os itens devem considerar quantidade e valor; itens sem estoque suficiente devem ser sinalizados. |

### RF005 - Gerar Orcamento Automaticamente

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve gerar automaticamente o orcamento da ordem de servico com base nos servicos, pecas e insumos informados. |
| Atores | Usuario administrativo |
| Criterios de aceite | O orcamento deve apresentar valores de servicos, pecas e insumos; o valor total deve ser calculado automaticamente; alteracoes nos itens devem atualizar o orcamento. |

### RF006 - Enviar Orcamento para Aprovacao

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir enviar o orcamento ao cliente para aprovacao. |
| Atores | Usuario administrativo, cliente |
| Criterios de aceite | O orcamento deve ficar disponivel para aprovacao do cliente; a ordem de servico deve refletir o status adequado enquanto aguarda aprovacao. |

### RF007 - Controlar Status da Ordem de Servico

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve controlar o status da ordem de servico durante todo o ciclo de atendimento. |
| Atores | Usuario administrativo, sistema |
| Criterios de aceite | A ordem de servico deve suportar os status: Recebida, Em diagnostico, Aguardando aprovacao, Em execucao, Finalizada e Entregue. |

### RF008 - Alterar Status Automaticamente

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve alterar automaticamente o status da ordem de servico conforme acoes executadas no sistema. |
| Atores | Sistema |
| Criterios de aceite | Ao criar a ordem de servico, o status deve ser Recebida; ao enviar orcamento para aprovacao, o status deve ser Aguardando aprovacao; ao aprovar o orcamento, a ordem deve seguir para Em execucao; ao concluir servicos, deve seguir para Finalizada; ao registrar entrega, deve seguir para Entregue. |

### RF009 - Consultar Progresso da Ordem de Servico via API

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir que o cliente consulte via API o progresso da ordem de servico. |
| Atores | Cliente |
| Criterios de aceite | A API deve retornar dados basicos da ordem de servico e o status atual; a consulta deve permitir identificar a ordem de servico do cliente; informacoes administrativas sensiveis nao devem ser expostas. |

### RF010 - Manter Clientes

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve disponibilizar operacoes de cadastro, consulta, atualizacao e remocao de clientes. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel criar, listar, detalhar, atualizar e remover clientes; CPF e CNPJ devem ser validados; documentos duplicados devem ser rejeitados. |

### RF011 - Manter Veiculos

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve disponibilizar operacoes de cadastro, consulta, atualizacao e remocao de veiculos. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel criar, listar, detalhar, atualizar e remover veiculos; placa, marca, modelo e ano devem ser obrigatorios; cada veiculo deve estar associado a um cliente. |

### RF012 - Manter Servicos

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve disponibilizar operacoes de cadastro, consulta, atualizacao e remocao de servicos. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel criar, listar, detalhar, atualizar e remover servicos; cada servico deve possuir nome, descricao e valor base. |

### RF013 - Manter Pecas e Insumos com Controle de Estoque

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve disponibilizar operacoes de cadastro, consulta, atualizacao e remocao de pecas e insumos, incluindo controle de estoque. |
| Atores | Usuario administrativo |
| Criterios de aceite | Deve ser possivel criar, listar, detalhar, atualizar e remover pecas e insumos; cada item deve possuir quantidade em estoque; o estoque deve ser atualizado conforme uso nas ordens de servico. |

### RF014 - Listar e Detalhar Ordens de Servico

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir listar e detalhar ordens de servico. |
| Atores | Usuario administrativo |
| Criterios de aceite | A listagem deve exibir dados resumidos da ordem de servico; o detalhamento deve apresentar cliente, veiculo, servicos, pecas, orcamento e status atual. |

### RF015 - Monitorar Tempo Medio de Execucao dos Servicos

| Campo | Descricao |
| --- | --- |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve permitir monitorar o tempo medio de execucao dos servicos. |
| Atores | Usuario administrativo |
| Criterios de aceite | O sistema deve registrar datas ou horarios relevantes da execucao; deve calcular e disponibilizar o tempo medio por servico ou por ordem de servico finalizada. |

## 4. Regras de Negocio Relacionadas

| Codigo | Regra |
| --- | --- |
| RN001 | Uma ordem de servico deve estar vinculada a um cliente e a um veiculo. |
| RN002 | Uma ordem de servico deve possuir pelo menos um servico solicitado. |
| RN003 | O orcamento deve ser calculado a partir da soma dos servicos, pecas e insumos vinculados. |
| RN004 | Pecas e insumos devem respeitar a disponibilidade de estoque. |
| RN005 | O cliente deve conseguir acompanhar apenas informacoes permitidas sobre suas ordens de servico. |
