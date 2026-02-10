variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "ap-south-1"
}

variable "app_name" {
  description = "Name for AWS resources (no slashes or special characters)"
  type        = string
  default     = "facedec-backend" # Change this back to a simple name
}


variable "image_tag" {
  description = "Docker image tag to deploy"
  type        = string
  default     = "latest"
}

variable "spring_datasource_url" {
  description = "Database connection URL"
  type        = string
  sensitive   = true
}

variable "spring_datasource_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "spring_datasource_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT secret key"
  type        = string
  sensitive   = true
}

variable "cert_pass" {
  description = "Certificate password"
  type        = string
  sensitive   = true
}
