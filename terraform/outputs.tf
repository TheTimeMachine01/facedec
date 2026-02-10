output "ecr_repository_url" {
  value = aws_ecr_repository.app_repo.repository_url
}

output "lambda_function_url" {
  description = "The public URL to access your backend"
  value       = aws_lambda_function_url.url.function_url
}