variable "project_name" {
  default = "autoflow"
}

variable "region_default" {
  default = "us-east-1"
}

variable "cidr_vpc" {
  default = "10.0.0.0/16"
}

variable "role_arn" {
  default = "arn:aws:iam::724623091343:role/LabRole"
}
variable "instance_type" {
  default = "t3.medium"
}

variable "database_password" {
  description = "Senha do banco de dados RDS"
  type        = string
  sensitive   = false
  default     = "postgres"

}

variable "database_username" {
  description = "Usuário do banco de dados"
  type        = string
  default     = "postgres"
}