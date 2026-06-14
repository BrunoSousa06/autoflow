# Autoflow Frontend

Angular 17 — Standalone Components — Angular Material

## Stack

- Angular 17.3
- Angular Material (tema: azure-blue)
- TypeScript
- HttpClient
- Reactive Forms
- SCSS

## API Backend

```
http://localhost:8082
```

## Execução local

```bash
npm install
npm start
```

Acesse: http://localhost:4200

## Execução com Docker

```bash
# Build da imagem
docker build -t autoflow-frontend .

# Rodar o container
docker run -p 4200:80 autoflow-frontend
```

Acesse: http://localhost:4200

## Build de produção

```bash
npm run build
```

Arquivos gerados em: `dist/frontend/browser/`

## Estrutura inicial

```
src/
├── app/
│   ├── app.component.ts       # Componente raiz (standalone)
│   ├── app.component.html     # Template raiz
│   ├── app.component.scss     # Estilos raiz
│   ├── app.config.ts          # Configuração da aplicação
│   └── app.routes.ts          # Rotas (vazio — features a definir)
├── environments/
│   ├── environment.ts         # Desenvolvimento (apiUrl: localhost:8082)
│   └── environment.prod.ts    # Produção
└── styles.scss                # Estilos globais + Material
```