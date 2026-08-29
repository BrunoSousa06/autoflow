# AutoFlow

Sistema de gerenciamento de ordens de serviço para oficinas mecânicas. Controla o ciclo completo de atendimento:
abertura de OS, diagnóstico, orçamento, execução e encerramento, com controle de acesso por perfil e área do cliente.

---

## Estrutura do Repositório

```
autoflow/
├── backend/    # API REST — Java 21 + Spring Boot
└── frontend/   # SPA — Angular 17 + Angular Material
```

---

## Documentação do Projeto

Este repositório possui três arquivos README, cada um com um objetivo específico:

| Arquivo                                          | Finalidade                                                                                                                                    |
|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `README.md`                                      | Documentação principal do projeto AutoFlow, com visão geral, arquitetura, execução completa, fluxos principais e aderência ao Tech Challenge. |
| `backend/README.md`                              | Documentação técnica da API Spring Boot, incluindo execução local, variáveis de ambiente, testes, Swagger, Flyway e Docker.                   |
| `frontend/README.md`                             | Documentação técnica do frontend Angular, incluindo execução local, estrutura, environments, build e Docker/Nginx.                            |
| `docs/diagramas-sequencia/diagrama-sequencia.md` | Índice dos diagramas de sequência dos principais fluxos do sistema.                                                                           |
| `docs/fluxo-orcamento-publico.md`                | Fluxo técnico de consulta, download, aprovação e recusa de orçamento por link público.                                                        |

Para uma visão geral da solução, comece por este arquivo. Para detalhes técnicos de cada aplicação, consulte o README
correspondente.

---

## Stack

### Backend

| Tecnologia              | Detalhe                             |
|-------------------------|-------------------------------------|
| Java 21                 |                                     |
| Spring Boot 3.5.x       | Web, Data JPA, Security, Validation |
| PostgreSQL              | Banco de dados principal            |
| SpringDoc OpenAPI       | Documentação Swagger                |
| Lombok                  |                                     |
| JUnit 5 + Mockito       | Testes unitários e integração       |
| SonarQube + Jacoco      | Qualidade e cobertura               |
| Docker + GitHub Actions | Build, CI/CD                        |

### Frontend

| Tecnologia       | Versão |
|------------------|--------|
| Angular          | 17.3   |
| Angular Material | 17.3   |
| TypeScript       | 5.4    |
| RxJS             | 7.8    |
| SCSS             | —      |

---

## Arquitetura

### Backend — Clean Architecture

O AutoFlow é um monólito Spring Boot organizado segundo a Clean Architecture. O backend possui as camadas
`presentation`, `application`, `domain`, `infrastructure` e `config`.

A direção principal das dependências é `presentation → application → domain`. A camada `infrastructure` implementa
detalhes técnicos e as portas definidas pela `application`, enquanto `config` realiza a composição da aplicação.

Os detalhes da organização e das regras arquiteturais estão em [`docs/architecture.md`](docs/architecture.md).

### Frontend — Angular Standalone + Signals

- Standalone Components com lazy loading por rota
- `AuthService` com Signals (sem NgRx)
- Guards funcionais: `authGuard`, `roleGuard`
- Interceptors funcionais: JWT e tratamento global de erros

---

## Funcionalidades

| Módulo             | Detalhe                                                                      |
|--------------------|------------------------------------------------------------------------------|
| Usuários           | Cadastro, autenticação, perfis de acesso, e-mail único                       |
| Clientes           | CRUD, exclusão lógica, validação CPF/CNPJ                                    |
| Veículos           | Cadastro e associação com clientes                                           |
| Serviços           | Catálogo de serviços com exclusão lógica                                     |
| Peças e Insumos    | Catálogo de peças e insumos utilizados nas OS                                |
| Ordens de Serviço  | Abertura, diagnóstico, inclusão de itens, controle de status                 |
| Orçamentos         | Geração automática, aprovação/recusa autenticada ou externa, download em PDF |
| Reparos Adicionais | Solicitação durante execução, orçamento adicional com e-mail ao cliente      |
| Minha Conta        | Área do cliente: perfil, OS próprias, aprovação de orçamentos                |

---

## Usuários e Perfis

Usuários criados pelo seed do banco (senha padrão: `Senha@1234`):

| Email                    | Role      | Acesso                               |
|--------------------------|-----------|--------------------------------------|
| `admin@autoflow.com`     | ADMIN     | Acesso total ao sistema              |
| `atendente@autoflow.com` | ATENDENTE | Clientes, Veículos, OS, Orçamentos   |
| `mecanico1@autoflow.com` | MECANICO  | Dashboard, OS, Serviços, Peças       |
| `mecanico2@autoflow.com` | MECANICO  | Idem                                 |
| `cliente@autoflow.com`   | CLIENTE   | Minha Conta, Minhas Ordens, Veículos |

---

## Principais Fluxos

### Fluxo completo de OS

1. **Atendente** → cadastra cliente e veículo → abre OS → seleciona serviços solicitados
2. **Mecânico** → inicia diagnóstico → adiciona serviços e peças → finaliza diagnóstico (gera orçamento)
3. **Cliente** → acessa "Minhas Ordens" → aprova ou recusa o orçamento
4. **Mecânico** → inicia e finaliza cada serviço da OS
5. **Atendente** → finaliza a OS

### Download do orçamento em PDF

1. Login como ADMIN, ATENDENTE ou CLIENTE
2. Acesse `/orcamentos/:id` ou o detalhe da OS
3. Clique em **Baixar PDF** — o arquivo é gerado pelo backend e baixado no navegador

### Aprovação externa do orçamento

1. Ao publicar um orçamento, o backend gera um token aleatório com validade configurável.
2. O cliente recebe por e-mail um link público para `/public/orcamentos/:id?token=...`.
3. A página pública permite consultar os valores, baixar o PDF e aprovar ou recusar sem login.
4. A aprovação do orçamento principal coloca a OS em `EM_EXECUCAO`; a recusa segue a regra vigente de finalização.
5. Tokens inválidos, divergentes ou expirados não alteram o orçamento nem a OS.

Detalhes de endpoints, segurança, configuração e auditoria estão em
[`docs/fluxo-orcamento-publico.md`](docs/fluxo-orcamento-publico.md).

### Reparo adicional

1. **Mecânico** → abre uma OS em status `EM_EXECUCAO` → clica em **"Identificar reparo adicional"**
2. Seleciona serviço e peças → clica em **"Criar e enviar para aprovação"**
3. Backend gera orçamento do tipo ADICIONAL e envia e-mail ao cliente
4. **Cliente** → acessa "Minhas Ordens" → aprova ou recusa o orçamento adicional
5. **ADMIN ou ATENDENTE** → `/reparos-adicionais` lista todos com filtro por status

---

## Rotas do Frontend

| Rota                                   | Roles permitidas           | Descrição                                       |
|----------------------------------------|----------------------------|-------------------------------------------------|
| `/login`                               | Público                    | Autenticação                                    |
| `/dashboard`                           | ADMIN, ATENDENTE, MECANICO | Métricas e indicadores                          |
| `/clientes`                            | ADMIN, ATENDENTE           | Lista e cadastro de clientes                    |
| `/usuarios`                            | ADMIN, ATENDENTE           | Cadastro de mecânicos/atendentes                |
| `/veiculos`                            | ADMIN, ATENDENTE, CLIENTE  | Lista de veículos                               |
| `/ordens-servico`                      | ADMIN, ATENDENTE, MECANICO | Lista de ordens de serviço                      |
| `/ordens-servico/:numeroOs`            | ADMIN, ATENDENTE, MECANICO | Detalhe e ações da OS                           |
| `/orcamentos`                          | ADMIN, ATENDENTE           | Lista de orçamentos                             |
| `/orcamentos/:id`                      | ADMIN, ATENDENTE, CLIENTE  | Detalhe do orçamento + PDF                      |
| `/reparos-adicionais`                  | ADMIN, ATENDENTE           | Lista de reparos adicionais                     |
| `/servicos`                            | ADMIN, ATENDENTE, MECANICO | Catálogo de serviços                            |
| `/peca-insumo`                         | ADMIN, ATENDENTE, MECANICO | Catálogo de peças e insumos                     |
| `/minha-conta`                         | CLIENTE                    | Perfil do cliente                               |
| `/minha-conta/minhas-ordens`           | CLIENTE                    | OS do cliente logado                            |
| `/minha-conta/minhas-ordens/:numeroOs` | CLIENTE                    | Detalhe da OS + aprovar orçamento               |
| `/public/acompanhamento?token=...`     | Público                    | Acompanha o progresso da OS por link seguro     |
| `/public/orcamentos/:id?token=...`     | Público                    | Consulta, PDF, aprovação ou recusa do orçamento |

---

## Executando Localmente

### Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Java       | 21            |
| Maven      | 3.5.7         |
| Node.js    | 20            |
| npm        | 10            |
| PostgreSQL | Qualquer      |
| Docker     | Opcional      |

### Clonar o repositório

```bash
git clone https://github.com/seu-usuario/autoflow.git
cd autoflow
```

### Backend

```bash
cd backend
mvn spring-boot:run
```

API disponível em **http://localhost:8081**

### Frontend

```bash
cd frontend
npm install
npm start
```

Acesse: **http://localhost:4200**

> O frontend espera o backend em `/api`. Para alterar, edite `src/environments/environment.ts`.

---

## Executando com Docker

```bash
# Sobe toda a stack (frontend + backend + banco)
docker-compose up -d
```

### Somente o frontend

```bash
cd frontend
docker build -t autoflow-frontend .
docker run -p 4200:80 autoflow-frontend
```

---

### Somente o backend

```bash
cd backend
docker build -t autoflow-backend .
docker run --env-file ../.env -p 8080:8080 autoflow-backend
```

---

## Executando localmente com Kubernetes

É necessario ter o minikube/kind ou o Docker Desktop com a opção de kubernetes ativa para conseguir subir os manifestos
kubernetes localmente.

```bash
cd k8s-local
minikube start 
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f postgresql-service.yaml
kubectl apply -f postgresql-stateful.yaml
kubectl apply -f . 
kubectl get pods (validar que todos pods subiram com sucesso)

```

Observação: respeitar a ordem de subida acima para evitar erros no deploy dos pods devido dependecias de arquivos.

- Acesso no frontend : http://localhost:30180
- Acesso no backend  : http://localhost:30080

---

## Executando via Terraform na AWS

Necessario ter o terraform configurado com sua conta AWS.

```bash
cd infra
terraform init
terraform plan
terraform apply
```

---

## Documentação da API (Swagger)

Com o backend rodando:

| Interface  | URL                                   |
|------------|---------------------------------------|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI    | http://localhost:8081/v3/api-docs     |

---

## Segurança

- Autenticação via JWT
- Controle de autorização por perfil (ADMIN, ATENDENTE, MECANICO, CLIENTE)
- Senhas armazenadas com hash
- Endpoints públicos e privados segregados
- Tokens públicos de orçamento armazenados como hash e protegidos por expiração
- Validações: CPF, CNPJ, e-mail único, campos obrigatórios, formatos específicos

---

## Qualidade de Código

| Ferramenta     | Finalidade                       |
|----------------|----------------------------------|
| SonarQube      | Code smells, bugs, duplicações   |
| Jacoco         | Cobertura de testes              |
| Snyk           | Vulnerabilidades de dependências |
| GitHub Actions | Pipeline CI/CD automatizado      |

## Fluxo do CI/CD

O pipeline é executado automaticamente em dois cenários:

Pull Request direcionado para develop ou main;
Push nas branches develop ou main.

Entretanto, algumas etapas são condicionadas especificamente a um push na branch develop.

O fluxo pode ser representado da seguinte forma:

                         ┌─────────────────────┐
                         │     Pull Request    │
                         │  develop / main     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                    ┌──────────────────────────────┐
                    │ Backend Tests + SonarQube    │
                    │ - Maven                      │
                    │ - Testcontainers              │
                    │ - JaCoCo                     │
                    │ - SonarQube Quality Gate     │
                    └──────────────┬───────────────┘
                                   │
                  ┌────────────────┴────────────────┐
                  ▼                                 ▼
        ┌──────────────────┐              ┌──────────────────┐
        │  Backend Build   │              │ Frontend Tests   │
        │  mvn package     │              │ npm test         │
        └────────┬─────────┘              └────────┬─────────┘
                 │                                  │
                 │                                  ▼
                 │                         ┌──────────────────┐
                 │                         │ Frontend Build   │
                 │                         │ npm run build    │
                 │                         └────────┬─────────┘
                 │                                  │
                 └────────────────┬─────────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────────┐
                    │      Push na develop?        │
                    └──────────────┬───────────────┘
                                   │ Sim
                                   ▼
                    ┌──────────────────────────────┐
                    │       Snyk Security Scan     │
                    │ - Open Source dependencies   │
                    │ - Docker image               │
                    └──────────────┬───────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │      Docker Build & Push     │
                    │                              │
                    │  Backend → Docker Hub        │
                    │  Frontend → Docker Hub       │
                    └──────────────┬───────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │       Terraform Deploy       │
                    │                              │
                    │  Terraform Init              │
                    │  Terraform Validate         │
                    │  Terraform Plan              │
                    │  Terraform Apply             │
                    └──────────────┬───────────────┘
                                   │
                                   ▼
                         ┌────────────────────┐
                         │    AWS / EKS       │
                         │                    │
                         │ Infrastructure +   │
                         │ Kubernetes         │
                         └────────────────────┘
