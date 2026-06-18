# AutoFlow — Frontend

Sistema de gerenciamento de oficina mecânica. Desenvolvido com Angular 17 (standalone components) e Angular Material.

---

## Stack

| Tecnologia         | Versão  |
|--------------------|---------|
| Angular            | 17.3    |
| Angular Material   | 17.3    |
| TypeScript         | 5.4     |
| RxJS               | 7.8     |
| SCSS               | —       |

---

## Estrutura de pastas

```
src/
├── app/
│   ├── core/
│   │   ├── interceptors/       # JWT e tratamento global de erros
│   │   ├── guards/             # authGuard, roleGuard
│   │   └── services/           # AuthService
│   ├── layout/
│   │   └── shell/              # ShellComponent (sidenav + roteador)
│   ├── features/
│   │   ├── auth/               # Login
│   │   ├── dashboard/          # Métricas gerais
│   │   ├── clientes/           # CRUD de clientes
│   │   ├── usuarios/           # Cadastro de usuários (mecânicos/atendentes)
│   │   ├── veiculos/           # CRUD de veículos
│   │   ├── servicos/           # Catálogo de serviços
│   │   ├── peca-insumo/        # Catálogo de peças e insumos
│   │   ├── ordens-servico/     # Criação, diagnóstico, execução de OS
│   │   ├── orcamentos/         # Visualização e aprovação de orçamentos
│   │   ├── reparos-adicionais/ # Reparos solicitados durante execução
│   │   ├── minha-conta/        # Área do cliente (perfil, OS próprias)
│   │   └── public/             # Telas sem autenticação
│   ├── app.config.ts           # Providers globais (HTTP, roteamento, locale)
│   └── app.routes.ts           # Rotas da aplicação
└── environments/
    ├── environment.ts           # Desenvolvimento (localhost:8080)
    └── environment.prod.ts      # Produção (configurar apiUrl)
```

---

## Como rodar localmente

**Pré-requisitos:** Node 20+, npm 10+

```bash
npm install
npm start
```

Acesse: **http://localhost:4200**

O backend deve estar rodando em **http://localhost:8080**.

---

## Como rodar com Docker

```bash
# Build da imagem
docker build -t autoflow-frontend .

# Rodar o container (porta 4200 no host → porta 80 no container)
docker run -p 4200:80 autoflow-frontend
```

Acesse: **http://localhost:4200**

> O container serve o build de produção via nginx com roteamento SPA configurado (`try_files $uri /index.html`).

---

## Como configurar a URL da API

### Desenvolvimento (`src/environments/environment.ts`)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

### Produção (`src/environments/environment.prod.ts`)

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080'
};
```

O build de produção substitui automaticamente o arquivo via `fileReplacements` no `angular.json`.

---

## Usuários e roles

Todos os usuários abaixo são criados pelo seed do banco (senha padrão: `Senha@1234`).

| Email                       | Role       | Acesso                                           |
|-----------------------------|------------|--------------------------------------------------|
| `admin@autoflow.com`        | ADMIN      | Acesso total ao sistema                          |
| `atendente@autoflow.com`    | ATENDENTE  | Clientes, Veículos, OS, Orçamentos               |
| `mecanico1@autoflow.com`    | MECANICO   | Dashboard, Ordens de Serviço, Serviços, Peças    |
| `mecanico2@autoflow.com`    | MECANICO   | Idem                                             |
| `cliente@autoflow.com`      | CLIENTE    | Minha Conta, Minhas Ordens, Veículos             |

---

## Principais rotas

| Rota                                    | Roles permitidas            | Descrição                          |
|-----------------------------------------|-----------------------------|------------------------------------|
| `/login`                                | Público                     | Autenticação                       |
| `/dashboard`                            | ADMIN, ATENDENTE, MECANICO  | Métricas e indicadores             |
| `/clientes`                             | ADMIN, ATENDENTE            | Lista e cadastro de clientes       |
| `/usuarios`                             | ADMIN, ATENDENTE            | Cadastro de mecânicos/atendentes   |
| `/veiculos`                             | ADMIN, ATENDENTE, CLIENTE   | Lista de veículos                  |
| `/ordens-servico`                       | ADMIN, ATENDENTE, MECANICO  | Lista de ordens de serviço         |
| `/ordens-servico/:numeroOs`             | ADMIN, ATENDENTE, MECANICO  | Detalhe e ações da OS              |
| `/orcamentos`                           | ADMIN, ATENDENTE            | Lista de orçamentos                |
| `/orcamentos/:id`                       | ADMIN, ATENDENTE, CLIENTE   | Detalhe do orçamento + PDF         |
| `/reparos-adicionais`                   | ADMIN, ATENDENTE            | Lista de orçamentos adicionais     |
| `/servicos`                             | ADMIN, ATENDENTE, MECANICO  | Catálogo de serviços               |
| `/peca-insumo`                          | ADMIN, ATENDENTE, MECANICO  | Catálogo de peças e insumos        |
| `/minha-conta`                          | CLIENTE                     | Perfil do cliente                  |
| `/minha-conta/minhas-ordens`            | CLIENTE                     | OS do cliente logado               |
| `/minha-conta/minhas-ordens/:numeroOs`  | CLIENTE                     | Detalhe da OS + aprovar orçamento  |
| `/public/acompanhamento`                | Público                     | Informa que login é necessário     |

---

## Principais fluxos demonstráveis

### Fluxo completo de OS

1. Login como **atendente** → cria cliente + veículo + abre OS
2. Login como **mecânico** → inicia diagnóstico, adiciona serviços → finaliza diagnóstico (gera orçamento)
3. Login como **cliente** → acessa "Minhas Ordens" → aprova ou recusa o orçamento
4. Login como **mecânico** → inicia e finaliza cada serviço da OS
5. Login como **atendente** → finaliza a OS

### Download do orçamento em PDF

1. Login como ADMIN, ATENDENTE ou CLIENTE
2. Acesse `/orcamentos/:id` ou o detalhe da OS
3. Clique em **Baixar PDF** — o arquivo é gerado pelo backend e baixado no navegador

### Reparo adicional

1. Login como **mecânico** → abra uma OS em status `EM_EXECUCAO`
2. No painel do mecânico, clique em **"Identificar reparo adicional"**
3. Selecione o serviço e as peças/insumos necessários → clique em **"Criar e enviar para aprovação"**
4. O backend gera um orçamento do tipo ADICIONAL e envia e-mail ao cliente
5. Login como **cliente** → acesse "Minhas Ordens" → OS → aprove ou recuse o orçamento adicional
6. Login como **ADMIN ou ATENDENTE** → `/reparos-adicionais` lista todos os orçamentos adicionais com filtro por status

---

## Limitações conhecidas

- **Acompanhamento público**: Não há endpoint público de rastreamento de OS. A rota `/public/acompanhamento` informa o cliente para fazer login.
- **Detalhe de reparo adicional**: Não há endpoint `GET /reparos-adicionais/:id`. O detalhe é visualizado via `/orcamentos/:orcamentoId` (link direto a partir da listagem de reparos adicionais).
- **Refresh de token**: O JWT não tem renovação automática. Após expirar, o usuário é redirecionado para o login.
- **apiUrl em produção**: O arquivo `environment.prod.ts` aponta para `localhost:8080`. Para deploys em ambiente diferente, altere a variável antes do build.
- **CORS**: O backend precisa estar configurado para aceitar requisições do domínio do frontend.

---

## Troubleshooting

**`npm install` falha com erro de SSL**
```bash
npm config set strict-ssl false
npm install
```

**Tela em branco após `docker run`**
- Verifique se o build passou: `docker build` deve exibir `Successfully built`
- Verifique se a porta não está em uso: `docker run -p 4201:80 autoflow-frontend`

**Erros 401 ao usar a aplicação**
- Confirme que o backend está rodando em `http://localhost:8080`
- Confirme que o token JWT não expirou (faça logout e login novamente)

**Rota retorna 404 ao atualizar a página (Docker)**
- O `nginx.conf` já possui `try_files $uri $uri/ /index.html` — se a página 404 aparecer, verifique se o `nginx.conf` foi copiado corretamente para a imagem

**Erro de CORS no browser**
- Configure o backend para liberar a origem `http://localhost:4200` (ou a URL do frontend)
