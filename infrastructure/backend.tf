terraform {
  required_version = ">= 0.14.9"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.39.0"
      
    }
      docker = {
   source  = "kreuzwerker/docker"
   version = "~> 3.0"
  }
  }
  backend "s3" {
    region = "eu-north-1"
    bucket = "aritra-medium-blog-post"
    key    = "awsecsdemo/terraformstatefile"
    
  }
}