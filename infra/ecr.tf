resource "aws_ecr_repository" "autoflow_backend" {
  name                 = "autoflow-backend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_repository" "autoflow_frontend" {
  name                 = "autoflow-frontend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}