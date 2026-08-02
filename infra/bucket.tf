resource "aws_s3_bucket" "autoflow_bucket" {
  bucket = var.project_name

}