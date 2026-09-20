resource "kubectl_manifest" "backend_deployment" {
  yaml_body = file("${path.module}/../k8s/backend-deployment.yaml")
  depends_on = [kubectl_manifest.autoflow_config,kubectl_manifest.autoflow_secrets]
}

resource "kubectl_manifest" "frontend_deployment" {
  yaml_body = file("${path.module}/../k8s/frontend-deployment.yaml")

  depends_on = [kubectl_manifest.autoflow_config]
}