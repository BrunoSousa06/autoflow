# Diagramas de Sequencia - AutoFlow

Esta documentacao centraliza os principais fluxos de interacao do AutoFlow usando diagramas de sequencia em Mermaid.

Os diagramas foram separados em arquivos `.mermaid` para facilitar leitura, manutencao e evolucao independente de cada cenario.

## Indice dos diagramas

| Fluxo                                | Arquivo                                                                                  | Objetivo                                                                                                                      |
|--------------------------------------|------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| Ciclo principal da ordem de servico  | [fluxo_principal.mermaid](fluxo_principal.mermaid)                                       | Descrever a abertura da OS, diagnostico, geracao de orcamento e notificacao do cliente.                                       |
| Aprovacao ou recusa do orcamento     | [aprovacao_recusa_orcamento.mermaid](aprovacao_recusa_orcamento.mermaid)                 | Mostrar a decisao do cliente e as transicoes de status decorrentes.                                                           |
| Aprovacao ou recusa por link publico | [aprovacao_recusa_orcamento_publico.mermaid](aprovacao_recusa_orcamento_publico.mermaid) | Mostrar a validacao do token, a pagina publica e os efeitos da decisao externa.                                               |
| Execucao da OS e reparo adicional    | [execucao_os_reparo_adicional.mermaid](execucao_os_reparo_adicional.mermaid)             | Apresentar uma visao resumida da execucao dos servicos, baixa de estoque, reparo adicional e transicao da OS para Finalizada. |

## 1. Ciclo principal da ordem de servico

Este fluxo cobre o processo inicial da ordem de servico, desde a abertura pelo atendente ate a notificacao do cliente sobre o orcamento gerado.

Principais pontos representados:

- Criacao da ordem de servico.
- Registro dos dados do cliente e do veiculo.
- Inicio do diagnostico pelo mecanico.
- Geracao do orcamento principal.
- Alteracao do status da OS para aguardando aprovacao.

Arquivo: [fluxo_principal.mermaid](fluxo_principal.mermaid)

## 2. Aprovacao ou recusa do orcamento

Este fluxo mostra a decisao do cliente sobre o orcamento principal e o impacto dessa acao no ciclo da ordem de servico.

Principais pontos representados:

- Consulta do orcamento pelo cliente.
- Aprovacao do orcamento e liberacao da OS para execucao.
- Recusa do orcamento e encerramento ou revisao do atendimento.
- Atualizacao do status conforme a decisao tomada.

Arquivo: [aprovacao_recusa_orcamento.mermaid](aprovacao_recusa_orcamento.mermaid)

## 2.1 Aprovacao ou recusa por link publico

Este fluxo mostra a decisao sem autenticacao JWT. O cliente acessa uma pagina
publica protegida por token, e a alteracao de estado somente ocorre nos
endpoints `POST` de aprovacao ou recusa.

Principais pontos representados:

- Hash e expiracao do token validados antes da consulta e da decisao.
- Consulta publica sem exposicao de dados sensiveis.
- Idempotencia da mesma decisao e bloqueio de decisoes conflitantes.
- Atualizacao da OS principal ou do reparo adicional vinculado.

Arquivo: [aprovacao_recusa_orcamento_publico.mermaid](aprovacao_recusa_orcamento_publico.mermaid)

## 3. Execucao da OS e reparo adicional

Este fluxo cobre a execucao dos servicos aprovados e o tratamento de novas necessidades identificadas durante o reparo.

O diagrama principal foi mantido como uma visao resumida do fluxo completo. Os detalhes foram separados em subdiagramas menores para facilitar leitura e manutencao.

Principais pontos representados:

- Inicio da execucao da ordem de servico aprovada.
- Baixa de pecas e insumos no estoque.
- Identificacao e registro de reparo adicional pelo mecanico.
- Criacao, publicacao e notificacao do orcamento complementar.
- Aprovacao ou recusa do orcamento complementar pelo cliente.
- Conclusao do servico e possivel transicao da OS para Finalizada.

Arquivo: [execucao_os_reparo_adicional.mermaid](execucao_os_reparo_adicional.mermaid)

Subdiagramas:

- [inicio_servico_baixa_estoque.mermaid](inicio_servico_baixa_estoque.mermaid): detalha o inicio do servico aprovado, validacao da OS e baixa de estoque.
- [registro_reparo_adicional.mermaid](registro_reparo_adicional.mermaid): detalha a criacao do reparo adicional, orcamento complementar, publicacao e notificacao ao cliente.
- [decisao_orcamento_complementar.mermaid](decisao_orcamento_complementar.mermaid): detalha a aprovacao ou recusa do orcamento complementar e a aprovacao ou recusa do reparo adicional vinculado.
- [conclusao_servico_os.mermaid](conclusao_servico_os.mermaid): detalha a conclusao do servico e a transicao da OS para FINALIZADA quando todos os servicos forem concluidos.

## Como visualizar

Os arquivos `.mermaid` podem ser visualizados em ferramentas compativeis com Mermaid, como extensoes de IDE, GitHub, GitLab ou visualizadores online.

Ao atualizar um fluxo, altere o arquivo `.mermaid` correspondente e mantenha este documento como indice e guia de leitura dos cenarios.

## Observacoes

- Os diagramas representam o comportamento principal do backend Spring Boot e a interacao do frontend Angular com a API REST.
- A autorizacao por perfil ocorre antes da execucao dos endpoints via Spring Security e anotacoes `@PreAuthorize`.
- A persistencia central ocorre no PostgreSQL por meio dos repositories Spring Data JPA.
- Falhas de regra de negocio retornam erro HTTP tratado pelo `GlobalExceptionHandler`.
- A documentacao da API permanece disponivel via Swagger/OpenAPI; os diagramas complementam essa documentacao com a visao temporal dos fluxos.
