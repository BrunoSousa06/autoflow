# Arquitetura integrada de deploy da Fase 2

O desenho representa a arquitetura de deploy atualmente referenciada pelo repositório. As escolhas de AWS/EKS, RDS
PostgreSQL e Docker Hub são componentes confirmados nos artefatos versionados.

## Desenho

A fonte Mermaid está em [`arquitetura-deploy-fase-2.mermaid`](arquitetura-deploy-fase-2.mermaid).

## Componentes representados

| Componente                                 | Fonte versionada                                                                                                                                                  |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Pipeline GitHub Actions                    | [`.github/workflows/pipeline.yml`](../../.github/workflows/pipeline.yml)                                                                                          |
| Registry Docker Hub                        | [`docs/cicd.md`](../cicd.md) e pipeline                                                                                                                           |
| VPC, subnets, rotas, NAT e security groups | [`infra/`](../../infra/)                                                                                                                                          |
| Cluster EKS e node group                   | [`infra/eks-cluster.tf`](../../infra/eks-cluster.tf) e [`infra/eks-node.tf`](../../infra/eks-node.tf)                                                             |
| RDS PostgreSQL                             | [`infra/rds-postgresql.tf`](../../infra/rds-postgresql.tf)                                                                                                        |
| Frontend e backend                         | [`k8s/`](../../k8s/)                                                                                                                                              |
| ConfigMaps e Secret                        | [`k8s/configmap.yaml`](../../k8s/configmap.yaml), [`k8s/frontend-configmap.yaml`](../../k8s/frontend-configmap.yaml) e [`k8s/secret.yaml`](../../k8s/secret.yaml) |
| HPA do backend                             | [`k8s/hpa.yaml`](../../k8s/hpa.yaml)                                                                                                                              |

## Fluxo representado

1. Usuários acessam o frontend por um Service `LoadBalancer`.
2. O frontend encaminha as chamadas ao Service do backend.
3. O backend acessa o RDS PostgreSQL e o servidor SMTP.
4. O GitHub Actions executa testes, qualidade, builds e Snyk.
5. As imagens são publicadas no Docker Hub.
6. O job `deploy-aws` executa Terraform para provisionar a AWS e aplicar parte dos recursos Kubernetes.

## Observações do estado atual

- Os Deployments usam a tag mutável `latest`.
- O perfil local em [`k8s-local/`](../../k8s-local/) usa PostgreSQL StatefulSet e Metrics Server; ele não deve ser
  confundido com o perfil AWS/EKS representado no desenho principal.

Essas observações descrevem limitações operacionais do fluxo atual. O desenho não contém credenciais, tokens ou valores
de Secret.
