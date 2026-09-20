
data "aws_eks_cluster" "autoflow" {
  name = "eks-autoflow"
}

data "aws_eks_cluster_auth" "auth" {
  name = "eks-autoflow"
}

data "kubernetes_service_v1" "backend_lb" {
  depends_on = [time_sleep.wait_seconds]

  metadata {
    name      = "autoflow-backend-service"
    namespace = "default"
  }
}

data "aws_db_instance" "autoflow" {
  db_instance_identifier = "autoflow-db"
}