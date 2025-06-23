#!/bin/bash

# Deploy to LocalStack

echo "Starting LocalStack deployment..."

# Start LocalStack and local PostgreSQL
echo "Starting LocalStack services..."
docker-compose -f docker-compose-localstack.yml up -d

# Wait for LocalStack to be ready
echo "Waiting for LocalStack to be ready..."
sleep 30

# Build the application
echo "Building Spring Boot application..."
cd credit-card-service
./gradlew build -x test

# Build Docker image
echo "Building Docker image..."
docker build -t credit-card-service .
cd ..

# Deploy with CDK to LocalStack
echo "Deploying to LocalStack with CDK..."
cd cdk-deployment
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export CDK_DEFAULT_ACCOUNT=000000000000
export CDK_DEFAULT_REGION=us-east-1

# Bootstrap CDK (first time only)
# aws --endpoint-url=http://localhost:4566 cloudformation create-stack --stack-name CDKToolkit --template-body file://bootstrap-template.yaml

echo "Synthesizing CDK app..."
cdk synth

echo "Deploying CDK stack..."
cdk deploy --require-approval never

echo "Deployment complete!"
echo "LocalStack dashboard: http://localhost:4566"
