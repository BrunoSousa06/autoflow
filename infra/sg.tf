resource "aws_security_group" "sg" {
  name        = "${var.project_name}-sg"
  description = "Security group for EKS cluster"
  vpc_id      = aws_vpc.autoflow_vpc.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }


  ingress {
    description = "Permite todo trafego interno HTTPS para  EKS API"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    self        = true
  }

  ingress {
    description = "Permite o trafego do Load Balancer para as NodePorts do Kubernetes"
    from_port   = 30000
    to_port     = 32767
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.autoflow_vpc.cidr_block]
  }

  ingress {
    description = "Permite comunicacao interna entre os nos e pods"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

}


resource "aws_security_group" "rds_sg" {
  name        = "autoflow-rds-sg"
  description = "Permite trafego do cluster EKS para o RDS"
  vpc_id      = aws_vpc.autoflow_vpc.id

  ingress {
    description     = "Acesso a partir dos nos do EKS"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.sg.id]
  }

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.autoflow_vpc.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}