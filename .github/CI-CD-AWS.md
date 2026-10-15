# Operação de CI/CD AWS

O inventário por nome de secrets, variables, inputs Terraform e configuração da aplicação está no [catálogo deste repositório](../docs/variables-and-secrets.md), com [histórico](../docs/variables-changelog.md). Esta página permanece como referência operacional dos fluxos.

## Fluxo de branches e ambientes

CI executa em Pull Requests e pushes para `develop` e `main`, incluindo testes, builds, análise de segurança e validações estáticas. Pull Requests não publicam imagens. Push aprovado em `develop` publica backend e frontend no Docker Hub com a tag imutável do SHA do commit. O workflow encerra intencionalmente o caminho de deploy em um guard bloqueante antes de qualquer acesso Kubernetes/Terraform.

O deploy de homolog está bloqueado. O endpoint Kubernetes do EKS `eks-autoflow` não tem conectividade validada a partir de `ubuntu-latest`; `aws eks update-kubeconfig` configura autenticação, mas não cria rota de rede. O provider Terraform `kubernetes` e o provider `kubectl` consultam a API durante operações Terraform, inclusive no Plan. Não executar Plan/Apply ou `kubectl` em runner hospedado, nem abrir o endpoint a `0.0.0.0/0`.

### Arquitetura de acesso privado proposta

Usar um runner GitHub Actions efêmero em sub-rede privada da VPC já existente, criado por uma solução de runner-controller/scale-set com ciclo de vida por job. A instância/pod deve ter saída HTTPS controlada para GitHub, Docker Hub e endpoints AWS necessários (preferencialmente via VPC endpoints/NAT já aprovado), sem IP público e sem exposição do endpoint EKS. Conceder IAM mínimo via role associada ao runner (sem access keys estáticas), com `eks:DescribeCluster`, acesso ao state S3/lock e permissões AWS requeridas pelo root. O mapeamento de acesso EKS/RBAC deve ser explícito e limitado aos recursos Kubernetes geridos pela aplicação. Validar em sessão de teste: sub-rede/VPC e DNS, conectividade TCP/TLS ao endpoint privado EKS, identidade AWS, `kubectl get nodes`, leitura dos states, `terraform init/validate/plan` sem apply e limpeza do runner após o job.

Essa solução exige recursos/configuração no repositório de infraestrutura e autorização do owner da VPC; não está implementada nem validada neste repositório. SSM em instância existente não é adotado: não há instância/SSM documentados e um comando SSM remoto não fornece de forma segura/operável o ciclo de artifacts/plan/aprovação e execução idempotente necessário. Não habilitar apply até existir runner privado validado. Após isso, planejar no push de `develop`, guardar plan binário fora de logs/artifacts públicos com acesso restrito e hash SHA-256, revalidar hash no job aprovado pelo Environment `homolog`, e aplicar somente o mesmo plan após required reviewers efetivos. Plano Terraform pode conter dados sensíveis; proteger retenção, acesso e descarte.

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
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Não consumidos pelo workflow atualmente bloqueado; credenciais temporárias Learner Lab podem ser necessárias apenas na transição enquanto runner privado/IAM role não for autorizado. Nunca usar após migração para role |
| `AUTOFLOW_DB_USERNAME`, `AUTOFLOW_DB_PASSWORD`, `AUTOFLOW_JWT_SECRET`, `AUTOFLOW_MAIL_PASSWORD`, `AUTOFLOW_PUBLIC_TOKEN_SECRET`, `AUTOFLOW_TEST_CPF_CNPJ`, `AUTOFLOW_TEST_PASSWORD` | Segredos runtime/smoke, necessários somente ao habilitar o fluxo privado e pós-deploy |

## GitHub Variables

Configurar por Environment; não depender de defaults para conta, região ou state. Os valores de produção devem ser próprios e distintos dos de homologação:

| Variable | Uso |
| --- | --- |
| `DOCKERHUB_USERNAME` | Namespace Docker Hub, obrigatório para publicação SHA |
| `AWS_REGION`, `AWS_EXPECTED_ACCOUNT_ID` | Futuro job privado; `us-east-1` e conta Learner Lab explicitamente conferidos |
| `AWS_TERRAFORM_STATE_BUCKET`, `AWS_TF_STATE_KEY`, `AWS_RDS_STATE_KEY` | Futuro job privado; usar bucket informado e keys `application/autoflow/terraform.tfstate` e `rds/terraform.tfstate` |
| `EKS_CLUSTER_NAME`, `EKS_NODEGROUP_NAME`, `API_GATEWAY_ORIGIN`, `API_GATEWAY_CORS_ORIGINS` | Futuro preflight e validação de homolog |
| `TERRAFORM_APPLY_APPROVAL_CONFIGURED` | Guard adicional futuro; não substitui required reviewers reais do Environment |

## AWS Academy Learner Lab

Não presumir OIDC ou access keys permanentes no Learner Lab. O workflow atual não chama AWS nem executa Terraform porque a conectividade privada não está disponível. Antes de habilitar CD, priorizar role IAM no runner privado; caso o modelo Learner Lab obrigue credenciais temporárias, atualizar os três valores juntos antes do Plan. Não registrar credenciais, valores de Secret Kubernetes ou planos Terraform em logs/artifacts. Hash SHA-256 sozinho não protege confidencialidade: o plano precisa de armazenamento e acesso restritos.

O updater do Secret Kubernetes envia os valores por stdin, captura a saida de `kubectl`, relê o objeto e compara chaves/valores em memoria sem imprimir os valores.
