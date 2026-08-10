# Fluxo de Orcamento Publico

Este documento descreve o fluxo de consulta, download, aprovacao e recusa de
orcamentos por link publico. O fluxo nao exige login, mas exige um token
aleatorio, armazenado somente como hash no banco e com prazo de validade.

## Visao geral

1. O backend gera um token aleatorio quando publica o orcamento.
2. O banco armazena o hash do token em `public_token_hash` e a expiracao em
   `public_token_expira_em`.
3. O cliente recebe no e-mail o link da pagina:
   `/public/orcamentos/{orcamentoId}?token={token}`.
4. A pagina consulta o orcamento e permite baixar o PDF, aprovar ou recusar.
5. As acoes de decisao sao enviadas por `POST`, evitando alterar o estado
   apenas pela abertura do link ou por scanners de e-mail.

O token bruto nunca e persistido. O mesmo token tambem pode ser usado para
baixar o PDF enquanto estiver valido.

## Publicacao

A publicacao ocorre nos seguintes pontos:

- finalizacao do diagnostico, para o orcamento principal;
- criacao de reparo adicional, para o orcamento complementar.

A publicacao gera dois links:

| Link                                                            | Finalidade                           |
|-----------------------------------------------------------------|--------------------------------------|
| `app.public-base-url/public/orcamentos/{id}/pdf?token=...`      | Download direto do PDF pela API      |
| `app.frontend-public-base-url/public/orcamentos/{id}?token=...` | Pagina publica de consulta e decisao |

O e-mail informa os dois usos: download do PDF e acesso a pagina de decisao.

## Endpoints publicos

Todos os endpoints abaixo sao acessiveis sem `Authorization: Bearer`.

### Consultar o orcamento

```http
GET /public/orcamentos/{orcamentoId}?token={token}
```

Retorna `OrcamentoResponse` sem dados de autenticacao, hash do token ou
identificadores sensiveis do cliente.

### Baixar o PDF

```http
GET /public/orcamentos/{orcamentoId}/pdf?token={token}
```

O PDF continua disponivel por token publico valido. Token invalido, expirado ou
ausente nao libera o arquivo.

### Aprovar

```http
POST /public/orcamentos/{orcamentoId}/aprovar?token={token}
Content-Type: application/json

{
  "nome": "Maria da Silva"
}
```

O corpo e opcional. Quando `nome` nao e informado, o sistema usa o nome do
cliente presente no snapshot do orcamento para a assinatura.

### Recusar

```http
POST /public/orcamentos/{orcamentoId}/recusar?token={token}
Content-Type: application/json

{
  "nome": "Maria da Silva",
  "motivo": "Valor acima do esperado"
}
```

O corpo e opcional. O motivo, quando informado, possui limite de 500
caracteres; o nome da assinatura possui limite de 120 caracteres.

## Regras de decisao

- O token deve corresponder ao hash do orcamento e estar dentro do prazo.
- Somente orcamentos `DISPONIVEL` aceitam uma nova decisao.
- Repetir a mesma decisao e idempotente: aprovar um orcamento `APROVADO` ou
  recusar um orcamento `REPROVADO` nao executa novamente os efeitos.
- Decisoes conflitantes sao rejeitadas. Um orcamento `REPROVADO` nao pode ser
  aprovado e um `APROVADO` nao pode ser recusado.
- A decisao usa lock pessimista no orcamento para evitar concorrencia entre
  duas requisicoes simultaneas.

## Efeitos na OS

| Tipo do orcamento | Aprovacao                                  | Recusa                                           |
|-------------------|--------------------------------------------|--------------------------------------------------|
| Principal         | OS `AGUARDANDO_APROVACAO` -> `EM_EXECUCAO` | OS segue a regra vigente e vai para `FINALIZADA` |
| Complementar      | Atualiza o reparo adicional vinculado      | Recusa o reparo adicional vinculado              |

Os efeitos sao executados pelos mesmos casos de uso das decisoes autenticadas;
o token publico apenas fornece uma forma segura de entrada.

## Auditoria

A decisao grava os seguintes dados no orcamento:

- `aprovado_em`, quando aprovado;
- `reprovado_em`, quando recusado;
- `assinatura_nome`, quando houver nome informado ou nome do cliente;
- `recusa_motivo`, quando houver motivo.

O hash e a expiracao do token ficam no registro do orcamento. O valor bruto do
token nao e gravado em logs ou no banco.

## Erros

As regras de negocio usam o formato padrao da API:

```json
{
  "erro": "Token invalido ou expirado"
}
```

| Situacao                                      | HTTP esperado      |
|-----------------------------------------------|--------------------|
| Orcamento inexistente                         | `404 Not Found`    |
| Token invalido, expirado ou divergente        | `401 Unauthorized` |
| Orcamento indisponivel ou decisao conflitante | `400 Bad Request`  |
| Campo acima do limite                         | `400 Bad Request`  |

Em uma decisao rejeitada por token, o orcamento e a OS nao sofrem alteracao.

## Configuracao

O prazo padrao e de 7 dias e pode ser alterado por ambiente:

```bash
APP_PUBLIC_TOKEN_EXPIRATION_DAYS=7
APP_PUBLIC_TOKEN_SECRET=use-um-segredo-longo-e-aleatorio
APP_PUBLIC_BASE_URL=https://api.exemplo.com
FRONTEND_PUBLIC_BASE_URL=https://app.exemplo.com
```

Em producao, configure um segredo forte e nao use o valor padrao de
desenvolvimento. A alteracao de banco foi implementada pela migration
`V44__add_expiracao_token_publico_orcamento.sql`; migrations anteriores nao
devem ser editadas.

## Validacao

Os testes relacionados estao em:

- `backend/src/test/java/com/autoflow/presentation/orcamento/PublicOrcamentoControllerTest.java`;
- `backend/src/test/java/com/autoflow/application/usecases/orcamento/DecidirOrcamentoUseCaseTest.java`;
- `backend/src/test/java/com/autoflow/infrastructure/orcamento/OrcamentoPublicacaoServiceImplTest.java`;
- `backend/src/test/java/com/autoflow/integration/PublicAcompanhamentoIT.java`;
- `frontend/src/app/features/public/orcamento-publico.service.spec.ts`.

A especificacao OpenAPI atualizada pode ser consultada no Swagger UI enquanto o
backend estiver em execucao.
