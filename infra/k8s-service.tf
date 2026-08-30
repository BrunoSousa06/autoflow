resource "kubectl_manifest" "backend_service" {
  yaml_body = file("${path.module}/../k8s/backend-service.yaml")

  depends_on = [

    aws_eks_cluster.autoflow,
    aws_eks_node_group.node_group
  ]
}

resource "kubectl_manifest" "frontend_service" {
  yaml_body = file("${path.module}/../k8s/frontend-service.yaml")

  depends_on = [
    aws_eks_cluster.autoflow,
    aws_eks_node_group.node_group
  ]
}

