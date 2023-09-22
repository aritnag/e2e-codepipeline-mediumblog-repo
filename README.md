# General

This is an demostration of End to End CD and CI for a Single Page Application.

## Tech Stacks

- Angular(version - 14)
- AWS Components
  - AWS ECS to deploy the application and host the application
  - CodeCommit to store the application and infrastructure code
  - CodeBuild to deploy the application as Docker Image in AWS ECR and AWS ECS
  - CodePipeline to create automated pipelines for enabling CD CI
- IaaC ( Terraform)

### Solution Design

- Solution Design of the E2E Pipeline: ![Alt text](solution_design/CodeCommit.png?raw=true "Code-Commit")

- Solution Design of the AWS ECS and ALB: ![Alt text](solution_design/AWSECS.png?raw=true "AWS ECS Application")

### Replace the following values

- AWS Account ID
- AWS VPC ID
- AWS ECR REPO and ALB
- AWS CodeCommit Repo and branch

