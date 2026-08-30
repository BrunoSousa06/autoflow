resource "aws_eks_node_group" "node_group" {
  cluster_name    = aws_eks_cluster.autoflow.name
  node_group_name = "nodeg-${var.project_name}"
  node_role_arn   = var.role_arn
  subnet_ids      = aws_subnet.subnet_public[*].id
  disk_size       = 50
  instance_types  = [var.instance_type]


  scaling_config {
    desired_size = 2
    max_size     = 3
    min_size     = 2
  }

  update_config {
    max_unavailable = 1
  }

}