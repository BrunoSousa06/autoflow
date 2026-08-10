# Requisitos Nao Funcionais

## 1. Objetivo

Este documento descreve os requisitos nao funcionais do sistema Autoflow, incluindo arquitetura, seguranca, qualidade, documentacao, infraestrutura e operacao local.

## 2. Requisitos

### RNF001 - Arquitetura Monolitica

| Campo | Descricao |
| --- | --- |
| Categoria | Arquitetura |
| Prioridade | Obrigatoria |
| Descricao | O back-end deve ser implementado como uma aplicacao monolitica. |
| Criterios de aceite | A aplicacao deve ser implantavel como uma unica unidade; os modulos internos podem ser organizados por dominio ou camada. |

### RNF002 - Arquitetura em Camadas para MVP

| Campo | Descricao |
| --- | --- |
| Categoria | Arquitetura |
| Prioridade | Obrigatoria |
| Descricao | Por se tratar de um MVP, a aplicacao pode utilizar arquitetura em camadas. |
| Criterios de aceite | O codigo deve separar responsabilidades de apresentacao/API, aplicacao, dominio e infraestrutura/persistencia quando aplicavel. |

### RNF003 - Banco de Dados Justificado

| Campo | Descricao |
| --- | --- |
| Categoria | Persistencia |
| Prioridade | Obrigatoria |
| Descricao | A escolha do banco de dados e livre, mas deve ser justificada na documentacao do projeto. |
| Criterios de aceite | O README ou documento tecnico deve explicar o banco escolhido e a motivacao da escolha, considerando o contexto do MVP. |

### RNF004 - APIs RESTful Documentadas

| Campo | Descricao |
| --- | --- |
| Categoria | API |
| Prioridade | Obrigatoria |
| Descricao | As APIs devem seguir principios RESTful e ser documentadas com Swagger, OpenAPI ou ferramenta similar. |
| Criterios de aceite | Deve existir documentacao acessivel dos endpoints; a documentacao deve conter metodos HTTP, rotas, parametros, corpos de requisição e respostas esperadas. |

### RNF005 - Autenticacao JWT para APIs Administrativas

| Campo | Descricao |
| --- | --- |
| Categoria | Seguranca |
| Prioridade | Obrigatoria |
| Descricao | As APIs administrativas devem exigir autenticacao baseada em JWT. |
| Criterios de aceite | Endpoints administrativos devem rejeitar requisicoes sem token valido; tokens invalidos ou expirados devem retornar erro de autenticacao; endpoints publicos devem ser explicitamente definidos. |

### RNF006 - Validacao de Dados Sensiveis

| Campo | Descricao |
| --- | --- |
| Categoria | Seguranca e qualidade |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve validar dados sensiveis e campos criticos, como CPF, CNPJ e placa de veiculo. |
| Criterios de aceite | CPF e CNPJ invalidos devem ser rejeitados; placas invalidas devem ser rejeitadas; mensagens de erro devem indicar o campo invalido sem expor dados sensiveis desnecessarios. |

### RNF013 - Seguranca de Links Publicos de Orcamento

| Campo               | Descricao                                                                                                                                                                                               |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Categoria           | Seguranca                                                                                                                                                                                               |
| Prioridade          | Obrigatoria                                                                                                                                                                                             |
| Descricao           | Links publicos de orcamento devem usar token nao previsivel, hash persistido e expiracao configuravel.                                                                                                  |
| Criterios de aceite | O valor bruto do token nao deve ser armazenado; token invalido, divergente ou expirado deve retornar erro sem alterar o orcamento ou a OS; a API publica nao deve exigir JWT nem expor dados sensiveis. |

### RNF007 - Testes Unitarios e de Integracao

| Campo | Descricao |
| --- | --- |
| Categoria | Qualidade |
| Prioridade | Obrigatoria |
| Descricao | O sistema deve possuir testes unitarios e de integracao para os principais fluxos. |
| Criterios de aceite | Devem existir testes para criacao, acompanhamento e gestao de ordens de servico; fluxos criticos de dominio e API devem ser cobertos. |

### RNF008 - Cobertura Minima de Testes

| Campo | Descricao |
| --- | --- |
| Categoria | Qualidade |
| Prioridade | Obrigatoria |
| Descricao | Os dominios criticos devem possuir cobertura minima de 80% por testes automatizados. |
| Criterios de aceite | A cobertura deve ser mensuravel por ferramenta automatizada; dominios criticos devem incluir, no minimo, ordem de servico, orcamento, status e estoque. |

### RNF009 - Dockerfile

| Campo | Descricao |
| --- | --- |
| Categoria | Infraestrutura |
| Prioridade | Obrigatoria |
| Descricao | O projeto deve possuir Dockerfile para build da aplicacao. |
| Criterios de aceite | Deve ser possivel gerar uma imagem da aplicacao a partir do Dockerfile; o processo de build deve estar documentado. |

### RNF010 - Docker Compose

| Campo | Descricao |
| --- | --- |
| Categoria | Infraestrutura |
| Prioridade | Obrigatoria |
| Descricao | O projeto deve possuir docker-compose.yml para orquestrar o ambiente completo. |
| Criterios de aceite | O docker-compose.yml deve subir a aplicacao e suas dependencias, incluindo banco de dados quando aplicavel; as variaveis de ambiente necessarias devem estar documentadas. |

### RNF011 - Execucao Local Simples

| Campo | Descricao |
| --- | --- |
| Categoria | Operacao |
| Prioridade | Obrigatoria |
| Descricao | O projeto deve possuir configuracao para execucao local simples. |
| Criterios de aceite | O README.md deve explicar pre-requisitos, configuracao, execucao da aplicacao, execucao de testes e acesso a documentacao da API. |

### RNF012 - Repositorio Privado

| Campo | Descricao |
| --- | --- |
| Categoria | Governanca |
| Prioridade | Obrigatoria |
| Descricao | O projeto deve estar organizado em repositorio privado com acesso concedido ao usuario soat architecture. |
| Criterios de aceite | O repositorio deve estar privado; o usuario ou grupo informado deve possuir acesso conforme solicitado para avaliacao. |

## 3. Restricoes Tecnicas

| Codigo | Restricao |
| --- | --- |
| RT001 | O back-end deve ser monolitico. |
| RT002 | A arquitetura em camadas e aceita para o MVP. |
| RT003 | As APIs devem ser RESTful. |
| RT004 | A documentacao das APIs deve ser gerada por Swagger, OpenAPI ou ferramenta similar. |
| RT005 | O ambiente local deve ser reprodutivel via Docker e Docker Compose. |

## 4. Criterios Gerais de Qualidade

| Codigo | Criterio |
| --- | --- |
| CQ001 | Os endpoints devem retornar codigos HTTP coerentes com o resultado da operacao. |
| CQ002 | Erros de validacao devem retornar mensagens claras e padronizadas. |
| CQ003 | Dados sensiveis nao devem ser expostos em logs ou respostas publicas. |
| CQ004 | A documentacao do projeto deve permanecer atualizada conforme evolucao da aplicacao. |
