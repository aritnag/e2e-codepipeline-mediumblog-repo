
data "aws_caller_identity" "current" {}
variable "aws_region" {}
variable "image_repo_name" {}

# ---------------------------------------------------------------------------------------------------------------------
# awsecsdemo IAM Role
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_iam_role" "awsecsdemo-service-role" {
  name               = "awsecsdemo_springboot-awsecsdemoECRAccessRole-${var.image_repo_name}"
  path               = "/"
  assume_role_policy = data.aws_iam_policy_document.awsecsdemo-service-assume-policy.json
}

data "aws_iam_policy_document" "awsecsdemo-service-assume-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}



resource "aws_iam_role" "awsecsdemo-instance-role" {
  name               = "awsecsdemo_springboot-awsecsdemoInstanceRole-${var.image_repo_name}"
  path               = "/"
  assume_role_policy = data.aws_iam_policy_document.awsecsdemo-instance-assume-policy.json
}



resource "aws_iam_role_policy_attachment" "awsecsdemo-instance-role-xray-attachment" {
  role       = aws_iam_role.awsecsdemo-instance-role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}

data "aws_iam_policy_document" "awsecsdemo-instance-assume-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

