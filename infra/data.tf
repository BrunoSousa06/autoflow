
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

data "terraform_remote_state" "database" {
  backend = "s3"

  config = {
    bucket = "state-autoflow-terraform"
    key    = "rds/terraform.tfstate"
    region = "us-east-1"
  }
}