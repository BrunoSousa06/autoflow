# Implantação gradual do Kong Gateway

## Responsabilidades

- `autoflow-infra`: cluster/plataforma, release Helm do Kong OSS + KIC, CRDs, IAM e IRSA.
- `autoflow`: Deployments, Services, Ingresses, KongPlugins e Terraform de deploy da aplicação.
- `autoflow-lambda`: handler/infra da Lambda de autenticação.
- `autoflow-db`: RDS gerenciado.

Ingresses e Terraform específicos da aplicação permanecem em `autoflow`; o pipeline de plataforma não os aplica.

## Gate para o pipeline de aplicação

O workflow de `autoflow` só executa Terraform quando a variável GitHub do repositório `AUTOFLOW_TF_STATE_READY` está configurada como `true`. Esse gate deve permanecer fechado até o responsável confirmar a localização e integridade do state existente e a adoção do backend remoto descrito abaixo. Antes de `terraform init`, o workflow também exige que o objeto de state exista na key configurada, impedindo inicialização acidental de um state vazio.

O roteamento Kong é desativado por padrão (`TF_VAR_enable_kong_ingress=false`). Para ativá-lo num ambiente, configure nele `KONG_INGRESS_ENABLED=true` somente depois de Kong Gateway, KIC, IngressClass `kong` e CRD `kongplugins.configuration.konghq.com` estarem instalados e prontos. Nesse modo, o workflow verifica a CRD, a IngressClass e Pods prontos do release `kong` no namespace `kong` antes de executar `terraform init/plan/apply`. Configure também `KONG_LAMBDA_FUNCTION_NAME` com a função daquele ambiente; nenhuma credencial AWS é colocada no KongPlugin. Esses gates do workflow não comprovam IRSA nem sucesso de invocação da Lambda.

O login permanece fechado: o bloqueio `request-termination` deve continuar aplicado. O modo acadêmico de Function URL está desabilitado por padrão, mas, quando autorizado, `NONE` torna a URL publicamente invocável diretamente pela Internet — não limita chamadas ao Kong. O NLB atual é `internet-facing`, expõe HTTP 80 e não tem TLS público válido comprovado; portanto, não habilitar uma rota de autenticação nesse proxy. HTTP público não é adequado para transmitir senhas reais. Não usar segredo compartilhado como atalho. Nenhum upstream/Ingress demonstrativo está ativo. Veja [decisões e riscos](../../docs/specs/kong-lambda-function-url/decisoes.md) e [especificação da demonstração](../../docs/specs/kong-lambda-function-url/implementacao-demonstracao.md).

Enquanto `KONG_INGRESS_ENABLED` estiver desativada, o Terraform de aplicação pode criar os Services `ClusterIP` paralelos, mas não aplica CRDs Ingress/KongPlugin. Isso permite a preparação independente e não remove os Services `LoadBalancer` legados.

`autoflow-infra` e `autoflow` têm workflows separados. A implantação de plataforma deve concluir e passar pelas verificações de prontidão antes de habilitar Ingress no ambiente da aplicação. Atualizações de CRDs/KIC e aplicação de manifests devem ser coordenadas serialmente; não iniciar um upgrade de plataforma durante o apply do repositório da aplicação.

## Persistência do Terraform state da aplicação

O root `autoflow/infra` não tinha bloco `backend`; logo o Terraform usava state local. A configuração atual declara o backend S3 `state-autoflow-terraform`, key exclusiva `application/autoflow/terraform.tfstate`, criptografia e locking S3. O state anterior, se existir fora deste workspace, ainda não foi localizado nem inspecionado.

**Não execute `terraform init`, `terraform plan` ou `terraform apply` no root depois desta alteração até o responsável pelo state confirmar a fonte atual e definir a migração segura.** O workflow fica bloqueado por `AUTOFLOW_TF_STATE_READY` enquanto isso não ocorrer. Nenhum state foi criado/migrado nesta implementação. Se houver um state local íntegro, o owner deverá preservar backup e migrar de forma controlada; se não houver state válido, inventariar os objetos e preparar importação para a key nova antes de liberar pipeline. Não libere o gate apenas para desbloquear o deploy.

## Configuração externa

O root de aplicação recebe bucket/key remotos por `terraform init -backend-config` e variáveis Terraform por `TF_VAR_*`; não depende de `backend.homolog.hcl` nem de `homolog.tfvars`. Localmente, use variáveis de ambiente e `AWS_PROFILE` ou arquivos locais ignorados pelo Git, e valide `AWS_EXPECTED_ACCOUNT_ID` com `scripts/verify_aws_account.py` antes de qualquer operação AWS. No GitHub Actions, configure `AWS_EXPECTED_ACCOUNT_ID`, `AWS_REGION`, `AWS_TERRAFORM_STATE_BUCKET`, `AWS_TF_STATE_KEY`, `AWS_RDS_STATE_KEY`, `KONG_INGRESS_ENABLED` e `KONG_LAMBDA_FUNCTION_NAME` como Variables; use Secrets para credenciais e dados sensíveis requeridos pelo pipeline. Não defina `AUTOFLOW_TF_STATE_READY=true` até reconciliar o state autoritativo.
