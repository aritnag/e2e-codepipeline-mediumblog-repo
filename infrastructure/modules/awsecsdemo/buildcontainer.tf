

data "aws_caller_identity" "current" {}
variable "aws_region" {}

resource "null_resource" "awsecsdemo_springboot" {
  triggers = {
    timestamp = "${timestamp()}"
  }

  provisioner "local-exec" {
    command     = <<EOT
        export DOCKER_DEFAULT_PLATFORM=linux/amd64
	      cd ../../../application && mvn clean package -Dmaven.test.skip=true
        aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com
        docker buildx install
        docker buildx create --use
      	docker buildx build --no-cache --push --platform linux/amd64 -t ${aws_ecr_repository.image_repo.repository_url}:latest . 
    	
EOT
    interpreter = ["/bin/bash", "-c"]
    working_dir = path.module
  }
}

