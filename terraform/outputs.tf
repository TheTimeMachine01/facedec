output "ecr_repository_url" {
  value = aws_ecr_repository.app_repo.repository_url
}

output "lambda_function_url" {
  description = "The public URL to access your backend"
  value       = aws_lambda_function_url.url.function_url
}

output "lambda_last_modified" {
  description = "The date and time that the Lambda function was last modified."
  value       = aws_lambda_function.backend.last_modified
}
