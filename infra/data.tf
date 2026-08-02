
data "aws_eks_cluster" "autoflow" {
  name = aws_eks_cluster.autoflow.name
}

data "aws_eks_cluster_auth" "auth" {
  name = aws_eks_cluster.autoflow.name
}
