# Operação de CI/CD AWS

O inventário por nome de secrets, variables, inputs Terraform e configuração da aplicação está no [catálogo deste repositório](../docs/variables-and-secrets.md), com [histórico](../docs/variables-changelog.md). Esta página permanece como referência operacional dos fluxos.

## Fluxo de branches e ambientes

CI executa em Pull Requests e pushes para `develop` e `main`, incluindo testes, builds, análise de segurança e validações estáticas. Pull Requests não publicam imagens. Push aprovado em `develop` publica backend e frontend no Docker Hub com a tag imutável do SHA do commit. O workflow encerra intencionalmente o caminho de deploy em um guard bloqueante antes de qualquer acesso Kubernetes/Terraform.

O endpoint Kubernetes do EKS `eks-autoflow` não tem conectividade a partir de `ubuntu-latest`; `aws eks update-kubeconfig` configura autenticação, mas não cria rota de rede. O provider Terraform `kubernetes` e o provider `kubectl` consultam a API durante operações Terraform, inclusive no Plan. Por isso Plan/Apply e `kubectl` rodam somente na EC2 runner privada; o endpoint não é aberto a `0.0.0.0/0`.

### Arquitetura de acesso privado proposta

O root `autoflow-infra/infra` provisiona uma EC2 dedicada em subnet privada existente, sem IP público, com IMDSv2, disco criptografado, sem entrada de rede e saída HTTPS via NAT já existente. A EC2 não altera nós existentes. Uma regra dedicada permite apenas HTTPS do Security Group do runner ao Security Group usado pelo control plane EKS. O bootstrap usa uma GitHub App instalada somente no repositório `autoflow`, guarda sua chave em Secrets Manager e obtém registration tokens curtos; o serviço systemd registra o runner como efêmero para um job por vez. Consulte [o guia do runner](../../autoflow-infra/docs/github-runner.md) para inputs, pré-requisitos e ativação.

O código do provisionamento e da pipeline está implementado localmente, mas ainda não foi aplicado nem validado em AWS/GitHub. O workflow publica imagens por push em `develop`; para implantar, o operador dispara manualmente em `develop`. `homolog-plan` gera `infra.tfplan`, verifica exclusões/substituições e envia o arquivo criptografado como artifact da execução com retenção de um dia. O artifact contém apenas o ciphertext do plan e metadados sem valores de variáveis Terraform; a chave `TF_PLAN_ENCRYPTION_KEY` (mínimo de 32 caracteres aleatórios) é secret, duplicada nos dois Environments e nunca incluída no artifact. O job publica hashes de integridade como outputs da execução.

O job `homolog` mantém Required reviewers reais e baixa o artifact nomeado com `run_id` e `run_attempt` da mesma execução. Antes de qualquer mutação Kubernetes, verifica os hashes, commit, versão Terraform, hash de `.terraform.lock.hcl`, fingerprint de todas as `TF_VAR_*` e backend; então descriptografa e reaplica os guards de exclusão/substituição. Não executa outro `terraform plan`: `terraform apply` consome exatamente `infra.tfplan` baixado. A saída detalhada de `terraform plan` e o JSON completo são gravados temporariamente em arquivos locais e removidos; o job summary contém somente endereços e ações, sem atributos ou valores. O Environment `homolog-plan` precisa ter branch `develop`, secrets de credenciais e de criptografia, sem required reviewers; `homolog` precisa de branch `develop`, Required reviewers reais, mesmas variables/segredos de criptografia e secrets de deploy. Proteger o runner contra jobs de forks e workflows não confiáveis.

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
| `DOCKERHUB_PASSWORD` | Repository secret usado na publicação automática de imagens em push `develop` |
| `SONAR_TOKEN`, `SNYK_TOKEN` | Análises existentes de qualidade e segurança |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Credenciais temporárias Learner Lab usadas nos Environments `homolog-plan` e `homolog`; renovar juntas antes da expiração |
| `TF_PLAN_ENCRYPTION_KEY` | Chave aleatória com ao menos 32 caracteres para criptografar/descriptografar o `.tfplan`; configurar como secret nos dois Environments |
| `AUTOFLOW_DB_USERNAME`, `AUTOFLOW_DB_PASSWORD`, `AUTOFLOW_JWT_SECRET`, `AUTOFLOW_MAIL_PASSWORD`, `AUTOFLOW_PUBLIC_TOKEN_SECRET` | Secrets de runtime aplicados ao Secret Kubernetes no job aprovado `homolog` |
| `AUTOFLOW_TEST_CPF_CNPJ`, `AUTOFLOW_TEST_PASSWORD` | Secrets de smoke tests, se esses testes forem habilitados |

## GitHub Variables

Configurar por Environment; não depender de defaults para conta, região ou state. Os valores de produção devem ser próprios e distintos dos de homologação:

| Variable | Uso |
| --- | --- |
| `DOCKERHUB_USERNAME` | Namespace Docker Hub, obrigatório para publicação SHA |
| `AWS_REGION`, `AWS_EXPECTED_ACCOUNT_ID` | Região e conta esperadas; duplicar nos Environments `homolog-plan` e `homolog` |
| `AWS_TERRAFORM_STATE_BUCKET`, `AWS_TF_STATE_KEY`, `AWS_RDS_STATE_KEY` | Bucket e keys `application/autoflow/terraform.tfstate` e `rds/terraform.tfstate`; duplicar nos dois Environments |
| `EKS_CLUSTER_NAME`, `API_GATEWAY_ORIGIN`, `API_GATEWAY_CORS_ORIGINS` | Cluster e origens; CORS deve ser array JSON, por exemplo `[]`; duplicar nos dois Environments |
| `DOCKERHUB_USERNAME` | Namespace Docker Hub usado nas imagens; disponibilizar nos dois Environments |
| `TERRAFORM_APPLY_APPROVAL_CONFIGURED` | Guard adicional em `homolog`; não substitui Required reviewers efetivos |

## AWS Academy Learner Lab

Não presumir OIDC ou access keys permanentes no Learner Lab. O pipeline usa secrets temporários AWS fornecidos pelo operador; atualize os três valores juntos antes do Plan. O artifact de uma execução é baixável por usuários com acesso ao repositório, mas seu `.tfplan` está criptografado com AES-256-CBC/PBKDF2; a chave permanece em GitHub Secrets. O plan em claro existe temporariamente no disco do runner e é removido após empacotamento/aplicação. O summary restrito exibe apenas endereços e ações, nunca valores do plan.

O updater do Secret Kubernetes envia os valores por stdin, captura a saida de `kubectl`, relê o objeto e compara chaves/valores em memoria sem imprimir os valores.
