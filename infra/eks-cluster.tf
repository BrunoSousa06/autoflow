resource "aws_eks_cluster" "autoflow" {
  name     = "eks-${var.project_name}"
  role_arn = var.role_arn
  version  = "1.35"

  access_config {
    authentication_mode                         = "API"
    bootstrap_cluster_creator_admin_permissions = true

  }

  vpc_config {
    subnet_ids         = concat(aws_subnet.subnet_public[*].id, aws_subnet.subnet_private[*].id)
    security_group_ids = [aws_security_group.sg.id]

  }


} 