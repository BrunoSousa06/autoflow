#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ingress_dir="$repo_root/k8s/ingress"
service_tf="$repo_root/infra/k8s-service.tf"

require_text() {
  local file="$1"
  local expected="$2"
  if ! grep -Fq -- "$expected" "$file"; then
    printf 'Missing expected Kong configuration in %s: %s\n' "$file" "$expected" >&2
    exit 1
  fi
}

business_ingress="$ingress_dir/business-api.yaml"
auth_ingress="$ingress_dir/auth-api.yaml"
lambda_ingress="$ingress_dir/lambda-login.yaml"
unsupported_methods="$ingress_dir/auth-login-unsupported-methods.yaml"
reserved_paths="$ingress_dir/reserved-paths.yaml"
termination_plugin="$ingress_dir/request-termination-plugin.yaml"
frontend_ingress="$ingress_dir/frontend.yaml"
unknown_api="$ingress_dir/unknown-api.yaml"
disabled_login="$ingress_dir/auth-login-disabled.yaml"

# Only the explicitly approved business groups may be stripped and sent to Spring.
require_text "$business_ingress" 'konghq.com/rewrite: "/$1"'
require_text "$business_ingress" '/~/api/((clientes|veiculos|usuarios|servicos|peca-insumo|ordens-servico|orcamentos|public)(/.*)?)'
require_text "$business_ingress" 'name: autoflow-backend-internal'
require_text "$business_ingress" 'konghq.com/regex-priority: "100"'

# Authentication endpoints stay explicit; login is exact POST to Lambda only.
require_text "$auth_ingress" '/~/api/(auth/(cadastro|usuarios|mecanicos)(/.*)?)'
require_text "$lambda_ingress" 'path: /auth/login'
require_text "$lambda_ingress" 'pathType: Exact'
require_text "$lambda_ingress" 'konghq.com/methods: "POST"'
require_text "$lambda_ingress" 'konghq.com/retries: "0"'
require_text "$lambda_ingress" 'konghq.com/plugins: autoflow-lambda-login'
require_text "$unsupported_methods" 'konghq.com/methods: "GET,PUT,PATCH,DELETE,OPTIONS,HEAD,CONNECT,TRACE"'
require_text "$unsupported_methods" 'konghq.com/plugins: autoflow-reserved-paths-404'

# Reserved paths terminate with 404 and use specific paths over the SPA fallback.
require_text "$termination_plugin" 'plugin: request-termination'
require_text "$termination_plugin" 'status_code: 404'
require_text "$reserved_paths" 'konghq.com/plugins: autoflow-reserved-paths-404'
require_text "$reserved_paths" 'path: /api/actuator'
require_text "$reserved_paths" 'path: /actuator'
require_text "$reserved_paths" 'path: /api/auth/login'
require_text "$reserved_paths" 'path: /services'
require_text "$reserved_paths" 'path: /api/services'
require_text "$reserved_paths" 'path: /plugins'
require_text "$reserved_paths" 'path: /api/plugins'
require_text "$unknown_api" 'path: /~/api(/.*)?'
require_text "$unknown_api" 'konghq.com/regex-priority: "1"'
require_text "$unknown_api" 'path: /~/api(/.*)?'
require_text "$disabled_login" 'konghq.com/methods: "POST"'
require_text "$disabled_login" 'konghq.com/plugins: autoflow-reserved-paths-404'
require_text "$frontend_ingress" 'path: /'
require_text "$frontend_ingress" 'name: autoflow-frontend-internal'

# Terraform must apply the route resources only after the opt-in gate and reference
# the versioned manifests owned by this repository.
require_text "$service_tf" 'count     = var.enable_kong_ingress ? 1 : 0'
for manifest in business-api.yaml auth-api.yaml lambda-login.yaml reserved-paths.yaml; do
  require_text "$service_tf" "../k8s/ingress/$manifest"
done
require_text "$service_tf" 'var.enable_kong_ingress && var.enable_kong_lambda_login ? 1 : 0'
require_text "$service_tf" 'var.enable_kong_ingress && !var.enable_kong_lambda_login ? 1 : 0'
require_text "$service_tf" 'unknown-api.yaml'
require_text "$service_tf" 'auth-login-disabled.yaml'

# Legacy public Services must not be actively managed; internal Services stay ClusterIP.
if [[ -e "$repo_root/k8s/backend-service.yaml" || -e "$repo_root/k8s/frontend-service.yaml" ]] || \
  grep -Eq 'name: autoflow-(backend|frontend)-service|type: LoadBalancer' "$service_tf"; then
  printf 'Legacy public Services remain in the repository.\n' >&2
  exit 1
fi
require_text "$repo_root/k8s/backend-service-internal.yaml" 'type: ClusterIP'
require_text "$repo_root/k8s/frontend-service-internal.yaml" 'type: ClusterIP'
if grep -Eq 'proxy_pass|BACKEND_URL' "$repo_root/frontend/nginx.conf.template"; then
  printf 'Nginx must not proxy API paths or interpolate BACKEND_URL.\n' >&2
  exit 1
fi
require_text "$repo_root/frontend/nginx.conf.template" 'location ^~ /api/'
require_text "$repo_root/frontend/nginx.conf.template" 'return 404;'
if grep -q 'frontend-config' "$repo_root/k8s/frontend-deployment.yaml"; then
  printf 'Frontend Deployment still references the removed ConfigMap.\n' >&2
  exit 1
fi
require_text "$repo_root/frontend/nginx.conf.template" 'access_log off;'
require_text "$repo_root/frontend/nginx.conf.template" 'try_files $uri $uri/ /index.html;'
require_text "$repo_root/frontend/nginx.local.conf.template" 'proxy_pass http://app:8081;'
require_text "$repo_root/docker-compose.yml" 'NGINX_CONF: nginx.local.conf.template'

printf 'Kong application manifest assertions passed. Runtime precedence and upstream behavior still require homologation.\n'
