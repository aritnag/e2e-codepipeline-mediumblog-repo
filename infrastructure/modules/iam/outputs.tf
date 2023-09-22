output "awsecsdemo-service-role" {
  value = aws_iam_role.awsecsdemo-service-role.arn
}
output "awsecsdemo-instance-role" {
  value = aws_iam_role.awsecsdemo-instance-role.arn
}