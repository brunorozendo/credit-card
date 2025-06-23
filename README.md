# Credit Card Application System

A comprehensive credit card application processing system built with Spring Boot 3, Java 21, and AWS CDK.

## Business Process Overview

The system implements a complete credit card application workflow:

1. **Application Submission** - Customer submits application with personal and financial information
2. **Identity Verification** - Validates customer identity and documents
3. **Credit Bureau Check** - Retrieves credit reports from bureaus (mocked)
4. **Risk Assessment** - Calculates risk score based on multiple factors
5. **Compliance Checks** - Performs KYC, AML, sanctions, and PEP screening
6. **Decision Making** - Automated approval/rejection based on criteria
7. **Card Issuance** - Creates account and issues card for approved applications

## Project Structure

```
credit-card-v2/
├── credit-card-service/         # Main Spring Boot application
│   ├── src/main/java/          # Application source code
│   ├── src/test/groovy/        # Spock framework tests
│   └── docker-compose.yml      # Local development setup
├── cdk-deployment/             # AWS CDK infrastructure code
│   └── src/main/java/          # CDK stack definitions
└── docker-compose-localstack.yml # LocalStack setup
```

## Technology Stack

- **Java 21** with Spring Boot 3
- **PostgreSQL** database
- **Spock Framework** for testing
- **Docker** for containerization
- **AWS CDK** for infrastructure as code
- **LocalStack** for local AWS development

## Prerequisites

- JDK 21
- Gradle 8.10.2+
- Docker and Docker Compose
- AWS CLI (for deployment)
- AWS CDK CLI (`npm install -g aws-cdk`)

## Getting Started

### 1. Local Development

Start the PostgreSQL database:
```bash
cd credit-card-service
docker-compose up -d postgres
```

Run the application:
```bash
./gradlew bootRun
```

The application will be available at `http://localhost:8080`

### 2. API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`
API Docs: `http://localhost:8080/api-docs`

### 3. Running Tests

```bash
./gradlew test
```

### 4. Building Docker Image

```bash
./gradlew build
docker build -t credit-card-service .
```

### 5. LocalStack Development

Start LocalStack:
```bash
docker-compose -f docker-compose-localstack.yml up -d
```

Deploy to LocalStack:
```bash
cd cdk-deployment
export AWS_ENDPOINT_URL=http://localhost:4566
export CDK_DEFAULT_ACCOUNT=000000000000
export CDK_DEFAULT_REGION=us-east-1
./gradlew build
cdk deploy --require-approval never
```

## API Endpoints

### Submit Application
```bash
POST /api/v1/credit-card-applications
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "ssn": "123-45-6789",
  "dateOfBirth": "1990-01-01",
  "address": {
    "streetAddress": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "annualIncome": 75000,
  "employmentStatus": "FULL_TIME",
  "requestedLimit": 5000,
  "cardType": "GOLD"
}
```

### Get Application Status
```bash
GET /api/v1/credit-card-applications/{applicationNumber}
```

### Get Applications by Email
```bash
GET /api/v1/credit-card-applications/customer/{email}
```

## Risk Assessment Factors

The system evaluates applications based on:
- Credit Score (35%)
- Debt-to-Income Ratio (25%)
- Delinquency History (20%)
- Credit Utilization (15%)
- Recent Inquiries (5%)

## Compliance Checks

- **KYC**: Identity verification
- **AML**: Anti-money laundering screening
- **Sanctions**: OFAC and international sanctions lists
- **PEP**: Politically exposed persons screening

## AWS Deployment

### Prerequisites
- AWS Account
- Configured AWS CLI
- CDK bootstrapped (`cdk bootstrap`)

### Deploy to AWS
```bash
cd cdk-deployment
./gradlew build
cdk deploy
```

The CDK stack creates:
- VPC with public/private subnets
- RDS PostgreSQL database
- ECS Fargate cluster
- Application Load Balancer
- Secrets Manager for credentials
- CloudWatch logs

## Environment Variables

### Application
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### AWS CDK
- `CDK_DEFAULT_ACCOUNT`: AWS account ID
- `CDK_DEFAULT_REGION`: AWS region

## Security Features

- Input validation on all API endpoints
- SQL injection prevention via JPA
- Secrets management for credentials
- HTTPS enforcement in production
- PII data masking in logs

## Monitoring

- Health endpoint: `/actuator/health`
- Metrics endpoint: `/actuator/metrics`
- CloudWatch logs in AWS deployment

## License

This project is for demonstration purposes only.
