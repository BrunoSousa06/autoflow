
data "aws_eks_cluster" "autoflow" {
  name = "eks-autoflow"
}

data "aws_eks_cluster_auth" "auth" {
  name = "eks-autoflow"
}

data "kubernetes_service_v1" "backend_lb" {

  metadata {
    name      = "autoflow-backend-service"
    namespace = "default"
  }
}