output "postgres_endpoint" {
  value = aws_db_instance.autoflow_rds.address
}

output "postgres_port" {
  value = aws_db_instance.autoflow_rds.port
}

output "ecr_repository_url_backend" {
  value       = aws_ecr_repository.autoflow_backend.repository_url
  description = "Use esta URL para fazer o push da imagem Docker Backend"
}

output "ecr_repository_url_frontend" {
  value       = aws_ecr_repository.autoflow_frontend.repository_url
  description = "Use esta URL para fazer o push da imagem Docker"
}


