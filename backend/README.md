# AutoFlow

Sistema para gerenciamento de ordens de serviço de manutenção automotiva.

## Sobre o Projeto

O AutoFlow é um sistema desenvolvido em Java para controlar o ciclo completo de atendimento de oficinas mecânicas, por meio de ordens de serviço permitindo o gerenciamento de clientes, veículos, serviços, ordens de serviços e itens necessários.

O objetivo do sistema é centralizar o processo operacional da oficina, permitindo um melhor controle de atendimento com priorizações corretas e atendimentos mais eficientes

---

## Tecnologias Utilizadas

* Java 21
* Spring Boot 3.5.x
* Spring Web
* Spring Data JPA
* Spring Security
* Spring Validation
* SpringDoc OpenAPI (Swagger)
* PostgreSQL
* Lombok
* JUnit 5
* Mockito
* SonarQube
* Docker
* GitHub Actions


## Decisões Arquiteturais

### Arquitetura em Camadas

O AutoFlow foi desenvolvido utilizando a arquitetura em camadas (Layered Architecture), devido a facilidade de manutenção, escalabilidade e baixo acoplamento entre os componentes, fazendo com que a evolução do projeto tenham um menor impacto em partes ja existentes no sistema. Devido a cada camada ter as suas responsabilidades definidas isso tambem faz com que as regras de negocio sejam validadas com os testes unitarios e testes integrados de forma mais simples e direta.
Como essa arquitetura é uma arquitetura padrão do mercado isso faz com que a aprendizagem não seja um impediditivo para o desenvolvimento.

Estrutura simplificada:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

#### Camadas da Aplicação

##### Controller

Responsável por:

* Expor os endpoints REST.
* Receber e validar requisições.
* Converter dados de entrada e saída.
* Delegar regras de negócio para a camada de serviço.

##### Service

Responsável por:

* Implementar as regras de negócio.
* Realizar validações de domínio.
* Coordenar operações transacionais.
* Orquestrar o fluxo da aplicação.

##### Repository

Responsável por:

* Persistência dos dados.
* Consultas ao banco de dados.
* Abstração da comunicação com o banco utilizando Spring Data JPA.

##### Domain/Entity

Responsável por:

* Representar as entidades do negócio.
* Definir relacionamentos e regras de persistência.

---

### Banco de Dados PostgreSQL

O PostgreSQL foi escolhido como sistema gerenciador de banco de dados por sua confiabilidade e facilidade de uso e integração com o Spring Boot

#### Motivos da escolha

##### Confiabilidade

O PostgreSQL possui alta estabilidade e mecanismos avançados de integridade de dados, garantindo segurança nas operações transacionais

##### Integração com Spring Data JPA

PostgreSQL possui uma boa integração com SPring Data JPA facilitando o desenvolvimento e manutenção

---

### Boas Práticas Aplicadas

Durante o desenvolvimento foram adotadas práticas visando qualidade, segurança e manutenibilidade:

* Princípios SOLID.
* Injeção de dependências.
* Tratamento centralizado de exceções.
* Validações utilizando Jakarta Bean Validation.
* Documentação automática com OpenAPI/Swagger.
* Testes unitários e de integração.
* Análise estática de código com SonarQube.
* Pipeline de integração contínua utilizando GitHub Actions.
* Controle transacional através do Spring Transaction Management.

---

## Funcionalidades

### Usuários

* Cadastro de usuários
* Autenticação
* Controle de perfis de acesso
* Validação de e-mail único

### Clientes

* Cadastro de clientes
* Atualização de dados
* Exclusão lógica
* Consulta por identificador
* Validação de CPF/CNPJ

### Veículos

* Cadastro de veículos
* Associação com clientes
* Consulta por proprietário

### Serviços

* Cadastro de serviços
* Atualização de informações
* Exclusão lógica
* Consulta de serviços cadastrados

### Ordens de Serviço

* Abertura de ordem de serviço
* Associação de cliente e veículo
* Inclusão de serviços solicitados
* Inclusão de itens necessários
* Controle de status
* Consulta detalhada

---

## Segurança

A API utiliza Spring Security para proteção dos endpoints.

Funcionalidades implementadas:

* Autenticação baseada em token
* Controle de autorização por perfil
* Endpoints públicos e privados
* Senhas armazenadas de forma criptografada

---

## Validações

Validações implementadas utilizando Jakarta Validation:

* Campos obrigatórios
* Tamanho mínimo e máximo
* Formatos específicos
* CPF válido
* CNPJ válido
* CPF/CNPJ único
* Email unico

---

## Documentação Complementar

A documentação funcional e técnica de apoio está organizada na pasta [`docs`](docs):

* [Requisitos funcionais](docs/requisitos-funcionais.md): descreve os fluxos esperados do sistema, atores, critérios de aceite e regras de negócio.
* [Requisitos não funcionais](docs/requisitos-nao-funcionais.md): consolida requisitos de arquitetura, segurança, qualidade, infraestrutura e operação local.
* [Diagramas de sequência](docs/diagrama-sequencia.md): centraliza os principais fluxos de interação da aplicação e aponta para os arquivos Mermaid separados por cenário.

Os diagramas foram separados em arquivos `.mermaid` para facilitar manutenção, versionamento e visualização individual dos fluxos.

---

## Documentação da API via Swagger

Após iniciar a aplicação:

Swagger UI:

http://localhost:8080/swagger-ui.html

OpenAPI:

http://localhost:8080/v3/api-docs

---

## Executando Localmente

### Pré-requisitos

* Java 21
* Maven 3.5.7
* Docker (opcional)
* PostgreSQL

### Clonar o projeto

```bash
git clone https://github.com/seu-usuario/autoflow.git
cd autoflow
```

---

## Executando com Docker


```bash
docker-compose up -d 
```
---



## Qualidade de Código

O projeto utiliza:

* SonarQube
* Jacoco
* GitHub Actions
* Snyk

Análises realizadas:

* Cobertura de testes
* Code Smells
* Vulnerabilidades
* Vulnerabilidade de dependência
* Bugs
* Duplicação de código

---

## Pipeline CI/CD

Fluxo automatizado:

1. Build da aplicação
2. Execução dos testes
3. Geração de cobertura
4. Análise SonarQube
5. Verificações de segurança com Snyk



