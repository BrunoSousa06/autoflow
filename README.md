# AutoFlow

Sistema de gerenciamento de ordens de serviço para oficinas mecânicas. Controla o ciclo completo de atendimento: abertura de OS, diagnóstico, orçamento, execução e encerramento, com controle de acesso por perfil e área do cliente.

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

| Arquivo | Finalidade |
|---------|------------|
| `README.md` | Documentação principal do projeto AutoFlow, com visão geral, arquitetura, execução completa, fluxos principais e aderência ao Tech Challenge. |
| `backend/README.md` | Documentação técnica da API Spring Boot, incluindo execução local, variáveis de ambiente, testes, Swagger, Flyway e Docker. |
| `frontend/README.md` | Documentação técnica do frontend Angular, incluindo execução local, estrutura, environments, build e Docker/Nginx. |
| `backend/docs/diagrama-sequencia.md` | Diagramas de sequência dos principais fluxos do sistema: OS, orçamento e reparo adicional. |

Para uma visão geral da solução, comece por este arquivo. Para detalhes técnicos de cada aplicação, consulte o README correspondente.

---

## Stack

### Backend

| Tecnologia               | Detalhe                    |
|--------------------------|----------------------------|
| Java 21                  |                            |
| Spring Boot 3.5.x        | Web, Data JPA, Security, Validation |
| PostgreSQL               | Banco de dados principal   |
| SpringDoc OpenAPI        | Documentação Swagger       |
| Lombok                   |                            |
| JUnit 5 + Mockito        | Testes unitários e integração |
| SonarQube + Jacoco       | Qualidade e cobertura      |
| Docker + GitHub Actions  | Build, CI/CD               |

### Frontend

| Tecnologia         | Versão  |
|--------------------|---------|
| Angular            | 17.3    |
| Angular Material   | 17.3    |
| TypeScript         | 5.4     |
| RxJS               | 7.8     |
| SCSS               | —       |

---

## Arquitetura

### Backend — Arquitetura em Camadas

```
Controller  →  Service  →  Repository  →  Database
```

Cada camada tem responsabilidade única:

- **Controller** — expõe endpoints REST, valida e converte requisições
- **Service** — implementa regras de negócio, valida domínio, controla transações
- **Repository** — persistência via Spring Data JPA
- **Domain/Entity** — entidades e relacionamentos

Boas práticas adotadas: SOLID, injeção de dependências, tratamento centralizado de exceções, Bean Validation, análise estática com SonarQube e pipeline CI via GitHub Actions.

### Frontend — Angular Standalone + Signals

- Standalone Components com lazy loading por rota
- `AuthService` com Signals (sem NgRx)
- Guards funcionais: `authGuard`, `roleGuard`
- Interceptors funcionais: JWT e tratamento global de erros

---

## Funcionalidades

| Módulo              | Detalhe                                                                 |
|---------------------|-------------------------------------------------------------------------|
| Usuários            | Cadastro, autenticação, perfis de acesso, e-mail único                  |
| Clientes            | CRUD, exclusão lógica, validação CPF/CNPJ                               |
| Veículos            | Cadastro e associação com clientes                                      |
| Serviços            | Catálogo de serviços com exclusão lógica                                |
| Peças e Insumos     | Catálogo de peças e insumos utilizados nas OS                           |
| Ordens de Serviço   | Abertura, diagnóstico, inclusão de itens, controle de status            |
| Orçamentos          | Geração automática, aprovação/recusa, download em PDF                   |
| Reparos Adicionais  | Solicitação durante execução, orçamento adicional com e-mail ao cliente |
| Minha Conta         | Área do cliente: perfil, OS próprias, aprovação de orçamentos           |

---

## Usuários e Perfis

Usuários criados pelo seed do banco (senha padrão: `Senha@1234`):

| Email                    | Role       | Acesso                                         |
|--------------------------|------------|------------------------------------------------|
| `admin@autoflow.com`     | ADMIN      | Acesso total ao sistema                        |
| `atendente@autoflow.com` | ATENDENTE  | Clientes, Veículos, OS, Orçamentos             |
| `mecanico1@autoflow.com` | MECANICO   | Dashboard, OS, Serviços, Peças                 |
| `mecanico2@autoflow.com` | MECANICO   | Idem                                           |
| `cliente@autoflow.com`   | CLIENTE    | Minha Conta, Minhas Ordens, Veículos           |

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

### Reparo adicional

1. **Mecânico** → abre uma OS em status `EM_EXECUCAO` → clica em **"Identificar reparo adicional"**
2. Seleciona serviço e peças → clica em **"Criar e enviar para aprovação"**
3. Backend gera orçamento do tipo ADICIONAL e envia e-mail ao cliente
4. **Cliente** → acessa "Minhas Ordens" → aprova ou recusa o orçamento adicional
5. **ADMIN ou ATENDENTE** → `/reparos-adicionais` lista todos com filtro por status

---

## Rotas do Frontend

| Rota                                   | Roles permitidas           | Descrição                         |
|----------------------------------------|----------------------------|------------------------------------|
| `/login`                               | Público                    | Autenticação                       |
| `/dashboard`                           | ADMIN, ATENDENTE, MECANICO | Métricas e indicadores             |
| `/clientes`                            | ADMIN, ATENDENTE           | Lista e cadastro de clientes       |
| `/usuarios`                            | ADMIN, ATENDENTE           | Cadastro de mecânicos/atendentes   |
| `/veiculos`                            | ADMIN, ATENDENTE, CLIENTE  | Lista de veículos                  |
| `/ordens-servico`                      | ADMIN, ATENDENTE, MECANICO | Lista de ordens de serviço         |
| `/ordens-servico/:numeroOs`            | ADMIN, ATENDENTE, MECANICO | Detalhe e ações da OS              |
| `/orcamentos`                          | ADMIN, ATENDENTE           | Lista de orçamentos                |
| `/orcamentos/:id`                      | ADMIN, ATENDENTE, CLIENTE  | Detalhe do orçamento + PDF         |
| `/reparos-adicionais`                  | ADMIN, ATENDENTE           | Lista de reparos adicionais        |
| `/servicos`                            | ADMIN, ATENDENTE, MECANICO | Catálogo de serviços               |
| `/peca-insumo`                         | ADMIN, ATENDENTE, MECANICO | Catálogo de peças e insumos        |
| `/minha-conta`                         | CLIENTE                    | Perfil do cliente                  |
| `/minha-conta/minhas-ordens`           | CLIENTE                    | OS do cliente logado               |
| `/minha-conta/minhas-ordens/:numeroOs` | CLIENTE                    | Detalhe da OS + aprovar orçamento  |
| `/public/acompanhamento?token=...`     | Público                    | Acompanha o progresso da OS por link seguro |

---

## Executando Localmente

### Pré-requisitos

| Ferramenta   | Versão mínima |
|--------------|---------------|
| Java         | 21            |
| Maven        | 3.5.7         |
| Node.js      | 20            |
| npm          | 10            |
| PostgreSQL   | Qualquer      |
| Docker       | Opcional      |

### Clonar o repositório

```bash
git clone https://github.com/seu-usuario/autoflow.git
cd autoflow
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
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

É necessario ter o minikube/kind ou o Docker Desktop com a opção de kubernetes ativa para conseguir subir os manifestos kubernetes localmente.

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
Observação: respeitar a ordem de subida acima  para evitar erros no deploy dos pods devido dependecias de arquivos.

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

| Interface  | URL                                    |
|------------|----------------------------------------|
| Swagger UI | http://localhost:8080/swagger-ui.html  |
| OpenAPI    | http://localhost:8080/v3/api-docs      |

---

## Segurança

- Autenticação via JWT
- Controle de autorização por perfil (ADMIN, ATENDENTE, MECANICO, CLIENTE)
- Senhas armazenadas com hash
- Endpoints públicos e privados segregados
- Validações: CPF, CNPJ, e-mail único, campos obrigatórios, formatos específicos

---

## Qualidade de Código

| Ferramenta     | Finalidade                          |
|----------------|-------------------------------------|
| SonarQube      | Code smells, bugs, duplicações      |
| Jacoco         | Cobertura de testes                 |
| Snyk           | Vulnerabilidades de dependências    |
| GitHub Actions | Pipeline CI/CD automatizado         |

Pipeline CI:
1. Build da aplicação
2. Execução dos testes
3. Geração de cobertura (Jacoco)
4. Análise SonarQube
5. Verificação de segurança com Snyk
