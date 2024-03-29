# Spring Boot with Lambda SnapStart using AWS CDK

## Project structure

The project is a multi-module Maven project consisting of 4 modules, which aims to streamline the development:

* api - contains the Spring Boot API
* cdk - contains all the infrastructure code for deployment to AWS
* commons - contains some potentially shared, re-usable resources such as dtos, entites or services
* functions - contains the code for separate Lambda functions, which may or may not use the commons resources

Note: `functions` module is empty, however it may contain plain Java functions or Spring Cloud Functions, which would
make it easy to re-use resources from `commons` such as repositories for data access.

## Requirements

* Please ensure that Java 17 is installed on your local machine.

## Run locally

```shell
./mvnw spring-boot:run -f './api/pom.xml'
```

You should be able to access the dummy api on `http://localhost:8080/api/test`

## Requirements for deployment

* AWS account configured
* [AWS CDK CLI installed](https://docs.aws.amazon.com/cdk/v2/guide/cli.html)
* It is important to set the 'DEPLOY_STAGE' environment variable.

### Setting "DEPLOY_STAGE" environment variable

Windows Powershell:

```shell
$Env:DEPLOY_STAGE="{Put your stage name here}"
```

MacOS terminal:

```shell
export DEPLOY_STAGE=mystage
```

## Building and installing locally

```shell
./mvnw clean install
```

## Deployment

Before deploying, please make sure that you have:

* set the "DEPLOY_STAGE" variable
* built and installed code locally

Optinal:

* set the AWS_REGION environment variable or by default it will be deployed to us-east-1

```shell
cdk deploy
```

Or setting ad-hoc to deploy to eu-central-1:

```shell
AWS_REGION=eu-central-1 DEPLOY_STAGE=dev cdk deploy
```

Enjoy!
