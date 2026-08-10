output "postgres_endpoint" {
  value = aws_db_instance.autoflow_rds.address
}

output "postgres_port" {
  value = aws_db_instance.autoflow_rds.port
}


