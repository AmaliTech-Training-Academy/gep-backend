# LocalStack as Message Broker (SQS) – Setup & Usage Guide

## Prerequisites

* Docker installed
* Java + Spring Boot project
* `spring-cloud-aws-sqs` dependency

## 1. Start LocalStack

From the docker-compose file, you can run the localstack service

## 2. Create SQS Queue

```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name my-queue
```
