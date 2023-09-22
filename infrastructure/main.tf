

module "awsecsdemo" {
  source = "./modules/awsecsdemo"
  providers = {
    aws = aws
  }
  image_repo_name      = var.image_repo_name
  image_tag            = var.image_tag
  aws_region           = var.region
  rds_external_secret = var.rds_external_secret
}

 module "iam_definations" {
  source = "./modules/iam"
  providers = {
    aws = aws
  }
  aws_region           = var.region
  image_repo_name = var.image_repo_name
}




module "devops" {
  source = "./modules/devops"
  providers = {
    aws = aws
  }
  github_branch = var.github_branch
  github_repo_name = var.github_repo_name
  github_repo_owner = var.github_repo_owner
  aws_region = var.region
  image_repo_name = var.image_repo_name
  image_tag = var.image_tag

} 
