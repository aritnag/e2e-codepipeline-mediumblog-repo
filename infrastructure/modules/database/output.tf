output "secrets_manager_credentials" {
  value = aws_secretsmanager_secret.db_credentials
}

output "secrets_manager_version" {
  value = aws_secretsmanager_secret_version.db_credentials
}
