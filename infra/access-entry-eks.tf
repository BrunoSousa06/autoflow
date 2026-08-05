resource "aws_eks_access_entry" "access_entry" {
  cluster_name  = aws_eks_cluster.autoflow.name
  principal_arn = var.role_arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "labrole_admin" {
  cluster_name  = aws_eks_cluster.autoflow.name
  principal_arn = var.role_arn

  policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }
}