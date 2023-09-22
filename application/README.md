# Welcome to AWS AWSEcs Demo, Simple Rest API built with Spring Boot which then gets deployed to AWS ECS

## Introduction

- Authentication with JWT
- Unit & Integration Tests
- Deploying Spring Boot App to AWS AWSEcs

- ### `IMPORTANT`

- I did not configure NAT Gateway to avoid extra bills as this is an open source thing.
- If you have security concerns with your app or it's real app, do not forget to put your stack behind a NAT Gateway in VPC Settings.

### How to Run Spring Boot App?

#### Quick Way

- Go to `application.properties` file in spring boot app and fill it with your credentials.

#### What If We have secrets?

- Navigate into AWSEcsdemo folder and then, find create .env file.
- Fill your secrets, have a look at `application.properties` it'll give you info about how to reference the secrets.
- Run `export $(cat .env | xargs)` and then, run `mvn spring-boot:run`.
- Optionally, you can run `DATABASE_URL=url other-secrets mvn spring:boot run`.
- If you fill the credentials which work, your app should start successfully.
- The connection is tested with AWS RDS so, you can use it confidently.

### How to Run Tests?

- mvn test

### Docker commands

- `docker build -t e2eecsdemo .`
- `docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:postgresql://AWS-RDS-URL:5432/postgres -e SPRING_DATASOURCE_USERNAME=postgres -e SPRING_DATASOURCE_PASSWORD=password e2eecsdemo`

### Endpoints

#### Health

- /health - Health Check API for AWS ALB see CDK Stack for detailed info.

#### Auth

- /auth/signup - POST - Sign Up
- /auth/login - POST - Sign In / Login

#### Categories

- Requests below require Authorization Header: Bearer JWT
- /categories - GET - Lists categories
- /categories - POST - Add new category with given name
- /categories/id - GET - Finds category for given id.
- /categories/id - PUT - Updates category for given id.
- /categories/id - DELETE - Deletes category for given id.
- /categories/id/products - Lists products for given category id.

#### Products

- Requests below require Authorization Header: Bearer JWT
- /products - POST - Add new product with given name, price, category_id
- /products/id - GET - Finds product for given id.
- /products/id - PUT - Updates product for given id.
- /products/id - DELETE - Deletes product for given id.
