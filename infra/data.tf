
data "aws_eks_cluster" "autoflow" {
  name = aws_eks_cluster.autoflow.name
}

data "aws_eks_cluster_auth" "auth" {
  name = aws_eks_cluster.autoflow.name
}

data "kubernetes_service_v1" "backend_lb" {
  depends_on = [time_sleep.wait_seconds]

  metadata {
    name      = "autoflow-backend-service"
    namespace = "default"
  }
}
