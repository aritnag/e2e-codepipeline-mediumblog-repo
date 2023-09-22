locals {
 container_port = 8080
}
variable "image_tag" {}
variable "rds_external_secret" {}

data "aws_availability_zones" "available" { state = "available" }
module "vpc" {
 source = "terraform-aws-modules/vpc/aws"
 version = "~> 3.19.0"

 azs = slice(data.aws_availability_zones.available.names, 0, 2) # Span subnetworks across 2 avalibility zones
 cidr = "10.0.0.0/16"
 create_igw = true # Expose public subnetworks to the Internet
 enable_nat_gateway = true # Hide private subnetworks behind NAT Gateway
 private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
 public_subnets = ["10.0.101.0/24", "10.0.102.0/24"]
 single_nat_gateway = true
}






module "alb" {
 source  = "terraform-aws-modules/alb/aws"
 version = "~> 8.4.0"

 load_balancer_type = "application"
 security_groups = [module.vpc.default_security_group_id]
 subnets = module.vpc.public_subnets
 vpc_id = module.vpc.vpc_id

 security_group_rules = {
  ingress_all_http = {
   type        = "ingress"
   from_port   = 80
   to_port     = 80
   protocol    = "TCP"
   description = "Permit incoming HTTP requests from the internet"
   cidr_blocks = ["0.0.0.0/0"]
  }
  egress_all = {
   type        = "egress"
   from_port   = 0
   to_port     = 0
   protocol    = "-1"
   description = "Permit all outgoing requests to the internet"
   cidr_blocks = ["0.0.0.0/0"]
  }
 }

 http_tcp_listeners = [
  {
   # * Setup a listener on port 80 and forward all HTTP
   # * traffic to target_groups[0] defined below which
   # * will eventually point to our "Hello World" app.
   port               = 80
   protocol           = "HTTP"
   target_group_index = 0
   target_group_health_check = {
        enabled             = true
        interval            = 30
        path                = "/health"  # Your health check path
        port                = "8080"
        protocol            = "HTTP"
        timeout             = 5
        healthy_threshold   = 3
        unhealthy_threshold = 3
        matcher             = "200"
      }
  }
 ]

 target_groups = [
  {
   backend_port         = local.container_port
   backend_protocol     = "HTTP"
   target_type          = "ip"
   health_check = {
        enabled             = true
        interval            = 30
        path                = "/health"
        port                = "8080"
        healthy_threshold   = 3
        unhealthy_threshold = 3
        timeout             = 6
        protocol            = "HTTP"
        matcher             = "200-399"
      }
  }
 ]
}

module "ecs" {
 source  = "terraform-aws-modules/ecs/aws"
 version = "~> 4.1.3"

 cluster_name = var.image_repo_name

 # * Allocate 20% capacity to FARGATE and then split
 # * the remaining 80% capacity 50/50 between FARGATE
 # * and FARGATE_SPOT.
 fargate_capacity_providers = {
  FARGATE = {
   default_capacity_provider_strategy = {
    base   = 20
    weight = 50
   }
  }
  FARGATE_SPOT = {
   default_capacity_provider_strategy = {
    weight = 50
   }
  }
 }
}



resource "aws_iam_role" "ecs_task_execution_role" {
  name = "ecsTaskExecutionRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
    inline_policy {
    name = "cloudwatch-logs-policy"

    policy = jsonencode({
      Version = "2012-10-17",
      Statement = [
        {
          Action = [
            "logs:CreateLogGroup",
            "logs:CreateLogStream",
            "logs:PutLogEvents"
          ],
          Effect   = "Allow",
          Resource = "*"
        }
      ]
    })
  }
}

resource "aws_iam_policy_attachment" "ecs_execution_policy" {
  name = "ecsTaskExecutionPolicy"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  roles      = [aws_iam_role.ecs_task_execution_role.name]
}

data "aws_secretsmanager_secret" "rds_external_secret" {
  name = var.rds_external_secret 
}
data "aws_secretsmanager_secret_version" "my_secret_version" {
  secret_id     = data.aws_secretsmanager_secret.rds_external_secret.id
  version_stage = "AWSCURRENT"
}
resource "aws_ecs_task_definition" "this" {
 container_definitions = jsonencode([{
  environment: [
   { name = "DATABASE_URL", value = "dummy_endpoint.dummy_region.rds.amazonaws.com" },
   { name = "DB_INDENTIFIER", value = "postgres" },
   { name = "SPRING_DATASOURCE_USERNAME", value = jsondecode(data.aws_secretsmanager_secret_version.my_secret_version.secret_string)["username"] },
   { name = "SPRING_DATASOURCE_PASSWORD", value = jsondecode(data.aws_secretsmanager_secret_version.my_secret_version.secret_string)["password"] }
  ],
  essential = true,
  image = "${aws_ecr_repository.image_repo.repository_url}:${var.image_tag}",
  name = var.image_repo_name,
  logconfiguration = {
        logdriver = "awslogs"
        options = {
          "awslogs-group"         = "${var.image_repo_name}"
          "awslogs-region"        = "${var.aws_region}"
          "awslogs-stream-prefix" = "ecs"
          "awslogs-create-group": "true",
        }
      },
  healthcheck = {
    command            = ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"]
    interval           = 30
    timeout            = 5
    retries            = 3
    start_period       = 60
      },
  portMappings = [{ containerPort = local.container_port }],
 }])
 cpu = 256
 execution_role_arn = aws_iam_role.ecs_task_execution_role.arn
 family = "demo-${var.image_repo_name}-tasks"
 memory = 512
 network_mode = "awsvpc"
 requires_compatibilities = ["FARGATE"]
 
}

resource "aws_ecs_service" "this" {
 cluster = module.ecs.cluster_id
 desired_count = 2
 launch_type = "FARGATE"
 name = "${var.image_repo_name}-service"
 task_definition = resource.aws_ecs_task_definition.this.arn

 lifecycle {
  ignore_changes = [desired_count] # Allow external changes to happen without Terraform conflicts, particularly around auto-scaling.
 }

 load_balancer {
  container_name = var.image_repo_name
  container_port = local.container_port
  target_group_arn = module.alb.target_group_arns[0]
 }

 network_configuration {
  security_groups = [module.vpc.default_security_group_id]
  subnets = module.vpc.private_subnets
 }
 
}