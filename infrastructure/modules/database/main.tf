
data "aws_kms_alias" "database" {
  name = "alias/aws/rds"
}

# ---------------------------------------------------------------------------------------------------------------------
# Security Groups for Creating the RDS  
# ---------------------------------------------------------------------------------------------------------------------

data "aws_security_groups" "rds_vpc_groups" {
  filter {
    name   = "vpc-id"
    values = [var.vpc_id]
  }
}

# ---------------------------------------------------------------------------------------------------------------------
# VPC details
# ---------------------------------------------------------------------------------------------------------------------


variable "vpc_id" {}
variable "secrets_manager_name" {}
variable "image_repo_name" {}
data "aws_vpc" "rds_vpc" {
  id = var.vpc_id
}
data "aws_subnet_ids" "rds_vpc_subnets" {
  vpc_id = var.vpc_id
}

resource "aws_db_subnet_group" "awsecsdemo_db_subnet_group" {
  name        = "awsecsdemo_db_subnet_group-${var.image_repo_name}"
  subnet_ids  = data.aws_subnet_ids.rds_vpc_subnets.ids
  description = "awsecsdemo_db DB subnet group"
}


resource "aws_db_instance" "awsecsdemo_db" {
  engine               = "postgres"
  allocated_storage    = 10
  instance_class       = "db.t3.micro"
  db_name              = "${var.image_repo_name}"
  identifier = "${var.image_repo_name}"
  username             = "dummy_user"
  password             = "dummy_password"
  parameter_group_name = "default.postgres15"
  kms_key_id           = data.aws_kms_alias.database.id
  vpc_security_group_ids = data.aws_security_groups.rds_vpc_groups.ids
  db_subnet_group_name = aws_db_subnet_group.awsecsdemo_db_subnet_group.name
  storage_encrypted = true
  skip_final_snapshot = true
 

}

resource "random_integer" "unique_id" {
  min = 100000
  max = 999999 
}

resource "aws_secretsmanager_secret" "db_credentials" {
  name = "${var.secrets_manager_name}-${var.image_repo_name}-${random_integer.unique_id.result}"
  
}



resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id     = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = aws_db_instance.awsecsdemo_db.username
    password = aws_db_instance.awsecsdemo_db.password
    host     = aws_db_instance.awsecsdemo_db.endpoint
    engine   = aws_db_instance.awsecsdemo_db.engine
  })
}
