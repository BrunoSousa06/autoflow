resource "aws_db_subnet_group" "autoflow_rds_subnets" {
  name       = "autoflow-db-subnet-group"
  subnet_ids = aws_subnet.subnet_private[*].id

  tags = {
    Name = "autoflow-db-subnet-group"
  }
}

resource "aws_db_instance" "autoflow_rds" {
  identifier        = "autoflow-db"
  allocated_storage = 20
  engine            = "postgres"
  engine_version    = "17"
  instance_class    = "db.t3.micro"

  db_name  = "autoflow_db"
  username = var.database_username
  password = var.database_password

  db_subnet_group_name   = aws_db_subnet_group.autoflow_rds_subnets.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible    = true

  skip_final_snapshot = true
  storage_encrypted   = false
  monitoring_interval = 0
  monitoring_role_arn = null

  tags = {
    Name = "autoflow-db"
  }
}