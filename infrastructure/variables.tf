variable "profile" {
  type    = string
  default = "default"
}


variable "region" {
  description = "AWS region"
  default     = "eu-north-1"
}

variable "env" {
  description = "Deployment environment"
  default     = "dev"
}

variable "image_repo_name" {
  description = "Deployment environment"
  default     = "ecsdemo"
}



variable "image_tag" {
  description = "Deployment ECR Image Tag"
  default     = "latest"
}
variable "github_branch" {
  description = "Github Branch"
  default     = "main"
}

variable "github_repo_name" {
  description = "Github Repo Name"
  default     = "aws-awsecsdemo-e2e"
}


variable "github_repo_owner" {
  description = "Github Repo Owner"
  default     = "aritnag"
}


variable "secrets_manager_name" {
  description = "Secrets Manager Name"
  default     = "awsecsdemodemo/ecsdemo"
}

variable "vpc_id" {
  description = "VPC ID"
  default     = "vpc-1234567"
}

variable "rds_external_secret" {
  default = "rds_credentials_dummy"
  description = "rds_external_secret"

}