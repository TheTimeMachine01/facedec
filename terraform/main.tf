# 1. ECR Repository to store your Docker Image
resource "aws_ecr_repository" "app_repo" {
  name                 = var.app_name
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# 2. IAM Role for Lambda
resource "aws_iam_role" "lambda_role" {
  name = "${var.app_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

# Attach basic execution policy (logging)
resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# 3. Lambda Function (Container Type)
resource "aws_lambda_function" "backend" {
  function_name = var.app_name
  role          = aws_iam_role.lambda_role.arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.app_repo.repository_url}:${var.image_tag}"
  publish       = false

  description = "Last deployed at ${timestamp()}"

  memory_size = 3000 # High RAM for OpenCV processing
  timeout     = 90

  environment {
    variables = {
      JAVA_OPTS                       = "-Djava.library.path=/app/lib"
      SPRING_DATASOURCE_URL           = var.spring_datasource_url
      SPRING_DATASOURCE_USERNAME      = var.spring_datasource_username
      SPRING_DATASOURCE_PASSWORD      = var.spring_datasource_password
      JWT_SECRET                      = var.jwt_secret
      JWT_EXPIRATION_TIME             = "60000000"
      REFRESH_EXPIRATION_TIME         = "600000000"
      CERT_PASS                       = var.cert_pass
      SPRING_MAIN_LAZY_INITIALIZATION = "true"
    }
  }

  depends_on = [aws_iam_role_policy_attachment.lambda_logs]
}

# 4. Allow Public Access via Function URL (Simplest for testing)
resource "aws_lambda_function_url" "url" {
  function_name      = aws_lambda_function.backend.function_name
  authorization_type = "NONE"

  cors {
    allow_origins = ["*"]
    allow_methods = ["*"]
  }
}


resource "aws_lambda_permission" "allow_public_access" {
  statement_id           = "FunctionURLAllowPublicAccess"
  action                 = "lambda:InvokeFunctionUrl"
  function_name          = aws_lambda_function.backend.function_name
  principal              = "*"
  function_url_auth_type = "NONE"

  depends_on = [aws_lambda_function.backend, aws_lambda_function_url.url]
}
