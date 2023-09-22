
data "aws_caller_identity" "current" {}

variable "github_branch" {}
variable "github_repo_name" {}
variable "github_repo_owner" {}
variable "aws_region" {}
variable "image_repo_name" {}
variable "image_tag" {}

resource "aws_codepipeline" "codepipeline" {
  name     = "awsecsdemo-springdemo-${var.image_repo_name}"
  role_arn = aws_iam_role.codepipeline_role.arn
  artifact_store {
    location = aws_s3_bucket.codepipeline_bucket.bucket
    type     = "S3"
    encryption_key {
      id   = data.aws_kms_alias.s3kmskey.arn
      type = "KMS"
    }
  }
  stage {
    name = "Source"
    action {
      name             = "Source"
      category         = "Source"
      owner            = "AWS"
      provider         = "CodeStarSourceConnection"
      version          = "1"
      output_artifacts = ["source_output"]
      configuration = {
        ConnectionArn    = aws_codestarconnections_connection.awsecsdemo_springboot.arn
        FullRepositoryId = "${var.github_repo_owner}/${var.github_repo_name}"
        BranchName       = var.github_branch
      }
    }
  }
  stage {
    name = "Build"
    action {
      name             = "Build"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      input_artifacts  = ["source_output"]
      output_artifacts = ["build_output"]
      version          = "1"
      configuration = {
        ProjectName = aws_codebuild_project.awsecsdemo-spring-demo.id
      }
    }
  }
}
resource "aws_codestarconnections_connection" "awsecsdemo_springboot" {
  name          = "awsecsdemo-${var.image_repo_name}"
  provider_type = "GitHub"
}
resource "aws_s3_bucket" "codepipeline_bucket" {
  bucket = "awsecsdemo-codepipeline-bucket-${var.image_repo_name}"
  force_destroy = true
  
}

resource "aws_iam_role" "codepipeline_role" {
  name = "awsecsdemo-${var.image_repo_name}"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "TrustPolicyStatementThatAllowsEC2ServiceToAssumeTheAttachedRole",
      "Effect": "Allow",
      "Principal": {
        "Service": "codepipeline.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}
resource "aws_iam_role_policy" "codepipeline_policy" {
  name = "awsecsdemo-spring-demo_codepipeline_policy-${var.image_repo_name}"
  role = aws_iam_role.codepipeline_role.id
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect":"Allow",
      "Action": [
        "s3:GetObject",
        "s3:GetObjectVersion",
        "s3:GetBucketVersioning",
        "s3:PutObjectAcl",
        "s3:PutObject",
        "iam:GetRole",
        "iam:PassRole"
      ],
      "Resource": [
        "${aws_s3_bucket.codepipeline_bucket.arn}",
        "${aws_s3_bucket.codepipeline_bucket.arn}/*",
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "codestar-connections:UseConnection"
      ],
      "Resource": "${aws_codestarconnections_connection.awsecsdemo_springboot.arn}"
    },
    {
      "Effect": "Allow",
      "Action": [
        "codebuild:BatchGetBuilds",
        "codebuild:StartBuild"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudformation:DescribeStacks",
        "kms:GenerateDataKey",
        "iam:GetRole",
        "iam:PassRole"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}
resource "aws_codebuild_project" "awsecsdemo-spring-demo" {
  name          = "awsecsdemo-e2e-demo-${var.image_repo_name}"
  description   = "Builds a Spring boot application with AWS awsecsdemo"
  service_role  = aws_iam_role.codebuild_role.arn
  build_timeout = "5"
  artifacts {
    type = "CODEPIPELINE"
  }
  environment {
    compute_type    = "BUILD_GENERAL1_SMALL"
    image           = "aws/codebuild/standard:2.0"
    type            = "LINUX_CONTAINER"
    privileged_mode = true
    environment_variable {
      name  = "AWS_ACCOUNT_ID"
      value = data.aws_caller_identity.current.account_id
    }
    environment_variable {
      name  = "AWS_DEFAULT_REGION"
      value = var.aws_region
    }
    environment_variable {
      name  = "IMAGE_REPO_NAME"
      value = var.image_repo_name
    }
    environment_variable {
      name  = "IMAGE_TAG"
      value = var.image_tag
    }
    environment_variable {
      name  = "GITHUB_COMMIT_ID"
      value = "CODEBUILD_RESOLVED_SOURCE_VERSION"
    }
    environment_variable {
      name  = "SERVICE_NAME"
      value = "${var.image_repo_name}-service"
    }
    environment_variable {
      name  = "CLUSTER_NAME"
      value = var.image_repo_name
    }
    environment_variable {
      name  = "TASK_DEFINITION_NAME"
      value = "demo-${var.image_repo_name}-tasks"
    }

  }
  source {
    type      = "CODEPIPELINE"
    buildspec = "buildspec.yml"
  }
}
resource "aws_iam_role" "codebuild_role" {
  name = "awsecsdemo_springboot_codebuild_role-${var.image_repo_name}"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "codebuild.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": "TrustPolicyStatementThatAllowsEC2ServiceToAssumeTheAttachedRole"
    }
  ]
}
EOF
}
resource "aws_iam_role_policy" "codebuild_policy" {
  name = "awsecsdemo_springboot_codebuild_policy-${var.image_repo_name}"
  role = aws_iam_role.codebuild_role.id
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*",
      "Effect": "Allow"
    },
    {
      "Action": [
        "s3:GetObject",
        "s3:GetObjectVersion",
        "s3:GetBucketVersioning",
        "s3:PutObjectVersionTagging",
        "s3:PutObjectLegalHold",
        "s3:GetBucketVersioning",
        "s3:PutObject",
        "s3:PutObjectRetention",
        "s3:PutObjectVersionAcl",
        "s3:PutObjectTagging",
        "s3:PutObjectAcl"
      ],
      "Resource": [
        "arn:aws:s3:::${aws_s3_bucket.codepipeline_bucket.bucket}",
        "arn:aws:s3:::${aws_s3_bucket.codepipeline_bucket.bucket}/*"
      ],
      "Effect": "Allow"
    },
    {
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:CompleteLayerUpload",
        "ecr:GetAuthorizationToken",
        "ecr:InitiateLayerUpload",
        "ecr:PutImage",
        "ecr:UploadLayerPart"
      ],
      "Resource": [
        "*"
      ],
        "Effect": "Allow"
    },
    {
      "Action": [
        "ecs:UpdateService",
        "ecs:ListTaskDefinitions",
        "ecs:DescribeTaskDefinition",
        "ecs:RegisterTaskDefinition",
        "ecs:DeregisterTaskDefinition",
        "ecs:DescribeServices",
        "ecs:DescribeClusters",
        "ecs:ListClusters",
        "ecs:ListServices",
        "iam:GetRole",
        "iam:PassRole"
      ],
      "Resource": [
        "*"
      ],
        "Effect": "Allow"
    }
  ]
}
EOF
}
data "aws_kms_alias" "s3kmskey" {
  name = "alias/aws/s3"
}