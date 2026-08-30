resource "kubectl_manifest" "backend_deployment" {
  yaml_body = file("${path.module}/../k8s/backend-deployment.yaml")
  depends_on = [
    aws_eks_cluster.autoflow,
    aws_eks_node_group.node_group,
    kubectl_manifest.autoflow_config,
    kubectl_manifest.autoflow_secrets,
    aws_db_instance.autoflow_rds
  ]
}

resource "kubectl_manifest" "frontend_deployment" {
  yaml_body = file("${path.module}/../k8s/frontend-deployment.yaml")

  depends_on = [
    aws_eks_cluster.autoflow,
    aws_eks_node_group.node_group,
  kubectl_manifest.autoflow_config]
}

