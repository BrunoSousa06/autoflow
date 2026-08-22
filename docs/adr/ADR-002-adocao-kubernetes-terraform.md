# ADR-002 - Adoção de Kubernetes e Terraform

## Status

Accepted

## Contexto

O projeto AutoFlow inicialmente utilizava Docker e Docker Compose para executar seus serviços, sendo uma abordagem adequada para o desenvolvimento local e para uma infraestrutura simples.

Com a evolução do projeto, surgiu a necessidade de utilizar uma infraestrutura mais robusta, escalável e adequada para execução em ambiente de produção.

A infraestrutura passou a utilizar recursos da AWS, incluindo Amazon EKS para execução dos containers e Amazon RDS PostgreSQL para persistência dos dados.

Diante disso, foi necessário definir uma estratégia para o provisionamento e gerenciamento da infraestrutura, bem como para a orquestração e execução dos containers da aplicação.

## Decisão

Foi decidido utilizar **Terraform** como ferramenta de Infrastructure as Code (IaC) e **Kubernetes**, por meio do **Amazon EKS**, como plataforma de orquestração dos containers do AutoFlow.

O Terraform será responsável pelo provisionamento e gerenciamento dos recursos de infraestrutura da AWS, incluindo:

* VPC e recursos de rede;
* Subnets e tabelas de roteamento;
* Security Groups;
* Cluster Amazon EKS;
* Node Groups;
* Amazon RDS PostgreSQL;
* Demais recursos necessários para a infraestrutura da aplicação.

O Terraform também será utilizado para aplicar e gerenciar os manifestos Kubernetes necessários para a execução da aplicação no cluster.

O Kubernetes será responsável pela execução e gerenciamento dos workloads da aplicação no Amazon EKS, incluindo:

* Gerenciamento do ciclo de vida dos Pods;
* Distribuição e gerenciamento das réplicas;
* Exposição dos serviços;
* Health checks e mecanismos de recuperação;
* Escalabilidade dos workloads;
* Gerenciamento dos Deployments e demais recursos da aplicação.

Dessa forma, o Terraform será utilizado principalmente para o **provisionamento e gerenciamento da infraestrutura**, enquanto o Kubernetes será responsável pela **orquestração e execução dos workloads da aplicação**.

## Consequências

### Consequências positivas

* A infraestrutura passa a ser definida como código, facilitando sua reprodução e manutenção.
* O provisionamento dos recursos da AWS torna-se automatizado e padronizado.
* A aplicação pode ser escalada horizontalmente por meio do Kubernetes.
* O Kubernetes fornece mecanismos de recuperação e gerenciamento do ciclo de vida dos Pods.
* A infraestrutura pode ser versionada juntamente com o código-fonte do projeto.
* O ambiente pode ser recriado de forma consistente a partir dos arquivos Terraform e manifestos Kubernetes.
* Reduz-se a necessidade de configuração manual da infraestrutura.

### Consequências negativas

* A arquitetura passa a possuir maior complexidade operacional.
* É necessário conhecimento de Terraform, Kubernetes, AWS e seus respectivos recursos.
* O gerenciamento da infraestrutura exige maior quantidade de arquivos de configuração.
* A utilização do Amazon EKS e dos demais recursos da AWS gera custos de infraestrutura.
* Problemas de configuração no Terraform ou Kubernetes podem afetar diretamente a disponibilidade da aplicação.

## Justificativa

A adoção de Terraform e Kubernetes foi escolhida para proporcionar maior automação, escalabilidade, padronização e capacidade de gerenciamento da infraestrutura do AutoFlow.


Embora Docker Compose continue sendo adequado para o desenvolvimento local, o uso do Amazon EKS associado ao Terraform fornece uma estrutura mais apropriada para o gerenciamento da aplicação em um ambiente de produção baseado em AWS.
