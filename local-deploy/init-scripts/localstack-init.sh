#!/bin/bash

echo "Initializing LocalStack resources..."

# Wait for LocalStack to be ready
sleep 5

# Create SQS Queues
awslocal sqs create-queue --queue-name user-login-queue
awslocal sqs create-queue --queue-name user-registration-queue
awslocal sqs create-queue --queue-name password-reset-queue
awslocal sqs create-queue --queue-name event-stat-queue
awslocal sqs create-queue --queue-name ticket-purchased-event-queue
awslocal sqs create-queue --queue-name payment-processing-event-queue
awslocal sqs create-queue --queue-name payment-completed-event-queue
awslocal sqs create-queue --queue-name event-invitation-queue

# Create S3 Bucket
awslocal s3 mb s3://event-planner-local

# Set bucket CORS
awslocal s3api put-bucket-cors --bucket event-planner-local --cors-configuration '{
  "CORSRules": [
    {
      "AllowedOrigins": ["*"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
      "AllowedHeaders": ["*"],
      "MaxAgeSeconds": 3000
    }
  ]
}'

echo "LocalStack initialization complete!"
