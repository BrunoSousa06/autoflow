# Operação de CI/CD AWS

O inventário por nome de secrets, variables, inputs Terraform e configuração da aplicação está no [catálogo deste repositório](../docs/variables-and-secrets.md), com [histórico](../docs/variables-changelog.md). Esta página permanece como referência operacional dos fluxos.

## Fluxo de branches e ambientes

CI executa automaticamente em Pull Requests e pushes para `develop` e `main`, incluindo testes, builds, análise de segurança e validações estáticas. Esses eventos não publicam imagens, não executam Terraform Apply/Destroy e não fazem deploy. Planos que acessam AWS e podem conter dados sensíveis permanecem em jobs de `workflow_dispatch`.

O CD atualmente habilitado é somente `develop` → `homolog`, disparado por `workflow_dispatch`. Os jobs de homolog usam esse GitHub Environment, variáveis/secrets de homolog e states identificados pelas keys configuradas para a homologação. O Apply exige ainda os guards de conta, state e destruições.

**Produção permanece desabilitada.** Os roots atuais não demonstram isolamento completo de produção: há nomes fixos de recursos, um único conjunto de keys/variáveis de RDS e workloads Kubernetes com nomes/namespace compartilhados. Não habilitar despacho em `main` nem criar o Environment `production` para esses workflows até concluir o plano de segregação abaixo, implementar roots/estados independentes e validar os planos sem aplicar.

Para habilitar qualquer CD, configurar nos GitHub Environments `homolog` e `production`: restrição de deployment à branch correspondente (`develop` ou `main`), Required reviewers, proteção de branch com PR/checks obrigatórios e desabilitação de bypass quando o plano GitHub utilizado oferecer essa opção. A aprovação do job ocorre pela proteção efetiva do Environment; a variável `TERRAFORM_APPLY_APPROVAL_CONFIGURED=true` é somente um guard adicional e nunca substitui Required reviewers. Definir a variável apenas depois de confirmar manualmente a proteção. No GitHub Actions, habilitar prevent self-review quando disponível e limitar quem pode administrar environments.

### Plano bloqueante para produção (sem alterações de infraestrutura nesta etapa)

1. Inventariar os recursos atualmente homologados e confirmar por revisão humana os estados AWS existentes, sem importar ou reutilizar state de homologação em produção.
2. Definir e provisionar isolamento independente para produção: keys de state/backend, VPC/EKS e roles, RDS/subnet group/SG/credenciais/banco, Lambda e API Gateway, nomes de recursos, logs/alarms e workload Kubernetes/namespace. Não permitir que production consuma remote states ou dados de RDS de homologação.
3. Parametrizar os roots e módulos para ambiente explícito, preservando por padrão os nomes e estados homologados já operacionais. Exigir que plans de produção sejam revisados antes de qualquer Apply.
4. Avaliar limites e serviços permitidos na conta AWS Academy Learner Lab, duração/renovação das credenciais temporárias, quotas, custos e disponibilidade dos serviços necessários antes de escolher topologia/capacidade. Essas condições não foram verificadas na conta nesta revisão; não assumir que existe capacidade para uma segunda VPC/EKS/RDS.
5. Só então adicionar jobs `workflow_dispatch` em `main` apontados exclusivamente ao Environment `production`, com credenciais/variables/state próprios, guard contra branch/ambiente cruzados, aprovação obrigatória e gates de destruição. Manter produção fechada até CI e planos estáticos confirmarem a separação.

Proteger `develop` e `main` com Pull Request obrigatorio, checks CI aprovados, conversas resolvidas e bloqueio de force-push/exclusao.

## Ordem e responsabilidades

Ordem operacional: VPC/EKS; RDS; Lambda e API Gateway HTTP; imagens e workloads Kubernetes. A HTTP API integra a Lambda para login e usa VPC Link para o ALB interno, que encaminha para NodePorts do EKS.

O root Terraform da aplicacao gerencia manifests Kubernetes. Spring Boot executa Flyway na inicializacao dos pods; as migrations precisam ser compativeis com a versao corrente da aplicacao. Aplicar primeiro mudancas de schema compativeis, depois a imagem; aguardar rollout/readiness antes dos smoke tests. Flyway coordena locking no banco; nao iniciar migrations concorrentes deliberadamente.

Cada root Terraform usa state S3 exclusivo por ambiente e lock file. Keys consumidoras/produtoras devem coincidir para EKS, RDS, Lambda, API Gateway e aplicacao. Os workflows verificam bucket/keys antes de inicializar os roots que consomem states remotos.

## GitHub Secrets

Configurar por repositório e Environment `homolog`; criar segredos independentes para `production` somente quando o plano de produção for aprovado e a infraestrutura estiver isolada:

| Secret | Uso |
| --- | --- |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Credenciais temporarias da mesma sessao Learner Lab |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET` | Terraform do RDS/Lambda |
| `DOCKERHUB_PASSWORD` | Publicacao autenticada das imagens |
| `SONAR_TOKEN`, `SNYK_TOKEN` | Analise de qualidade e seguranca |
| `AUTOFLOW_DB_USERNAME`, `AUTOFLOW_DB_PASSWORD`, `AUTOFLOW_JWT_SECRET`, `AUTOFLOW_MAIL_PASSWORD`, `AUTOFLOW_PUBLIC_TOKEN_SECRET` | Secret do workload Kubernetes |
| `AUTOFLOW_TEST_CPF_CNPJ`, `AUTOFLOW_TEST_PASSWORD` | Smoke autenticado com usuario sintetico de homologacao |
| `AUTOFLOW_READ_TOKEN` | Leitura do repositório da aplicacao no teste de contrato do gateway, se necessaria |

## GitHub Variables

Configurar por Environment; não depender de defaults para conta, região ou state. Os valores de produção devem ser próprios e distintos dos de homologação:

| Variable | Uso |
| --- | --- |
| `AWS_REGION`, `AWS_EXPECTED_ACCOUNT_ID` | Regiao explicita e conta AWS esperada |
| `AWS_TERRAFORM_STATE_BUCKET` | Bucket S3 preexistente |
| `AWS_TF_STATE_KEY_INFRA`, `AWS_TF_STATE_KEY`, `AWS_INFRA_STATE_KEY`, `AWS_RDS_STATE_KEY`, `AWS_TF_STATE_KEY_GATEWAY` | Keys exclusivas de cada state |
| `AWS_EKS_CLUSTER_ROLE_ARN`, `AWS_EKS_NODE_ROLE_ARN`, `AWS_EKS_OPERATOR_ACCESS_ROLE_ARN` | Roles EKS do ambiente |
| `AWS_LAMBDA_EXECUTION_ROLE_ARN`, `AWS_LAMBDA_FUNCTION_NAME` | Role e funcao Lambda |
| `EKS_CLUSTER_NAME`, `EKS_NODEGROUP_NAME` | Cluster e node group |
| `API_GATEWAY_ORIGIN`, `API_GATEWAY_CORS_ORIGINS` | Origem HTTPS e CORS JSON explicitos por ambiente; usar `[]` para same-origin ou lista exata de origens |
| `DOCKERHUB_USERNAME`, `BACKEND_IMAGE_TAG`, `FRONTEND_IMAGE_TAG` | Namespace e tags aprovadas das imagens |
| `AUTOFLOW_TF_STATE_READY` | Confirma bootstrap do state da aplicacao |
| `AUTOFLOW_REPOSITORY`, `AUTOFLOW_CONTRACT_REF` | Repositorio/ref para o teste de contrato do gateway |
| `TERRAFORM_APPLY_APPROVAL_CONFIGURED` | `true` somente depois de configurar revisores obrigatorios no environment `homolog` |

## AWS Academy Learner Lab

Nao presumir OIDC ou access keys permanentes. Atualizar manualmente os tres Secrets AWS, juntos, a partir da sessao temporaria Learner Lab antes de cada despacho. O GitHub Actions nao renova as credenciais; se expirarem, iniciar nova sessao e gerar novo plan. Nao registrar credenciais, valores de Secret Kubernetes ou planos Terraform em logs/artifacts.

O updater do Secret Kubernetes envia os valores por stdin, captura a saida de `kubectl`, relê o objeto e compara chaves/valores em memoria sem imprimir os valores.
