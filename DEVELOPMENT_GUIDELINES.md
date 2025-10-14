# Event Planner Backend - Development Guidelines

## Table of Contents
1. [Backend Developer Guidelines](#backend-developer-guidelines)
2. [DevOps & Infrastructure Guidelines](#devops--infrastructure-guidelines)
3. [Security Guidelines](#security-guidelines)
4. [Testing Guidelines](#testing-guidelines)
5. [CI/CD Guidelines](#cicd-guidelines)

---

## Backend Developer Guidelines

### Project Architecture
- **Microservices Architecture**: Spring Boot 3.2.0 with Java 17
- **Service Discovery**: Netflix Eureka ***
- **Package Structure**: `com.eventplanner.{domain}`
- **Multi-module Maven**: Parent POM manages dependencies

### Code Standards

#### 1. Module Structure
```
services/
├── user-service/          # User management & auth
├── event-service/         # Event CRUD operations
├── notification-service/  # Email & push notifications
└── api-gateway/          # API routing & aggregation

shared/
├── common-lib/           # DTOs, utilities, exceptions
├── security-lib/         # JWT, auth, authorization
└── messaging-lib/        # Event contracts, messaging
```

#### 2. Dependency Management
- **Shared Libraries**: Always use project version for internal dependencies
- **Spring Cloud**: Version 2023.0.0 (managed in parent POM)
- **Testcontainers**: Version 1.19.3 for integration tests
- **Security**: JWT implementation via jjwt-api 0.11.5

#### 3. Service Development Rules

**Service Independence**
- Each service must be deployable independently
- No direct service-to-service calls (use messaging)
- Shared code only through common libraries

**Configuration**
- Use Spring profiles: `dev`, `staging`, `prod`
- Environment-specific properties in `application-{profile}.yml`
- Externalize all configuration (database URLs, credentials)

**Database Design**
- Each service owns its database schema
- No cross-service database access
- Use database per service pattern

#### 4. API Design Standards

**REST Conventions**
```java
// Resource naming
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}

// Response format
{
  "data": {...},
  "status": "success|error",
  "message": "...",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Error Handling**
- Use common-lib exception classes
- Return consistent error responses
- Log errors with correlation IDs

#### 5. Development Workflow

**Branch Strategy**
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: Feature development
- `hotfix/*`: Production fixes

**Code Quality**
- Minimum 80% test coverage
- No direct commits to main/develop
- All changes via Pull Requests
- Code review required before merge

**Build Process**
```bash
# Local development
./scripts/build-all.sh    # Build all services
./scripts/test-all.sh     # Run all tests

# Service-specific
cd services/user-service
mvn clean install
mvn spring-boot:run
```

#### 6. Logging & Monitoring

**Logging Standards**
```java
// Use SLF4J with structured logging
@Slf4j
public class UserService {
    public User createUser(CreateUserRequest request) {
        log.info("Creating user: userId={}, email={}", 
                request.getUserId(), request.getEmail());
        // Implementation
        log.info("User created successfully: userId={}", user.getId());
    }
}
```

**Metrics**
- Use Micrometer for metrics
- Track business metrics (user registrations, events created)
- Monitor technical metrics (response times, error rates)

---

## DevOps & Infrastructure Guidelines

### Infrastructure Overview
- **Container Platform**: AWS ECS Fargate
- **Service Discovery**: AWS Service Discovery + Eureka
- **Database**: Amazon RDS PostgreSQL
- **Cache**: Amazon ElastiCache Redis
- **Load Balancer**: Application Load Balancer
- **Monitoring**: CloudWatch + X-Ray

### Terraform Standards

#### 1. Directory Structure
```
infrastructure/terraform/
├── environments/
│   ├── dev/
│   ├── staging/
│   └── prod/
└── modules/
    ├── vpc/
    ├── ecs/
    ├── rds/
    ├── elasticache/
    ├── s3/
    └── monitoring/
```

#### 2. Module Development Rules

**Module Standards**
- Each module must have `variables.tf`, `main.tf`, `outputs.tf`
- Use semantic versioning for module releases
- Include comprehensive documentation in `README.md`
- Validate inputs with variable validation blocks

**Example Module Structure**:
```hcl
# modules/ecs/variables.tf
variable "cluster_name" {
  description = "Name of the ECS cluster"
  type        = string
  validation {
    condition     = length(var.cluster_name) > 0
    error_message = "Cluster name cannot be empty."
  }
}

variable "environment" {
  description = "Environment name"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}
```

#### 3. Environment Configuration

**Environment Separation**
- Complete isolation between environments
- Separate AWS accounts recommended for prod
- Environment-specific variable files

**Resource Naming Convention**
```hcl
# Format: {project}-{service}-{environment}-{resource}
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-${var.environment}-cluster"
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "terraform"
  }
}
```

#### 4. ECS Configuration Standards

**Task Definition Requirements**
- Use Fargate launch type
- Minimum resources: 256 CPU, 512 MB memory
- Enable CloudWatch logging
- Use secrets manager for sensitive data

**Service Configuration**
```hcl
resource "aws_ecs_service" "user_service" {
  name            = "${var.project_name}-user-service-${var.environment}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.user_service.arn
  desired_count   = var.environment == "prod" ? 3 : 1
  
  deployment_configuration {
    maximum_percent         = 200
    minimum_healthy_percent = 100
  }
  
  network_configuration {
    subnets         = var.private_subnet_ids
    security_groups = [aws_security_group.ecs_tasks.id]
  }
  
  load_balancer {
    target_group_arn = aws_lb_target_group.user_service.arn
    container_name   = "user-service"
    container_port   = 8080
  }
}
```

#### 5. Database Configuration

**RDS Standards**
- Use Multi-AZ for production
- Enable automated backups (7-day retention minimum)
- Use parameter groups for configuration
- Enable performance insights

```hcl
resource "aws_db_instance" "main" {
  identifier = "${var.project_name}-${var.environment}-db"
  
  engine         = "postgres"
  engine_version = "15.4"
  instance_class = var.environment == "prod" ? "db.t3.medium" : "db.t3.micro"
  
  allocated_storage     = var.environment == "prod" ? 100 : 20
  max_allocated_storage = var.environment == "prod" ? 1000 : 100
  
  db_name  = "eventplanner"
  username = "admin"
  password = random_password.db_password.result
  
  multi_az               = var.environment == "prod"
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  performance_insights_enabled = true
  monitoring_interval         = 60
  
  tags = local.common_tags
}
```

#### 6. Security Configuration

**Network Security**
- Use private subnets for application tiers
- Implement security groups with least privilege
- Enable VPC Flow Logs
- Use NAT Gateway for outbound internet access

**IAM Policies**
```hcl
# ECS Task Role - Minimal permissions
data "aws_iam_policy_document" "ecs_task_policy" {
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "ssm:GetParameter",
      "ssm:GetParameters"
    ]
    resources = [
      "arn:aws:secretsmanager:${var.aws_region}:${data.aws_caller_identity.current.account_id}:secret:${var.project_name}/${var.environment}/*",
      "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/${var.project_name}/${var.environment}/*"
    ]
  }
}
```

#### 7. Monitoring & Alerting

**CloudWatch Configuration**
```hcl
resource "aws_cloudwatch_log_group" "ecs_logs" {
  for_each = toset(var.services)
  
  name              = "/ecs/${var.project_name}-${each.key}-${var.environment}"
  retention_in_days = var.environment == "prod" ? 30 : 7
  
  tags = local.common_tags
}

# Application Load Balancer alarms
resource "aws_cloudwatch_metric_alarm" "high_response_time" {
  alarm_name          = "${var.project_name}-${var.environment}-high-response-time"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = "300"
  statistic           = "Average"
  threshold           = "1"
  alarm_description   = "This metric monitors ALB response time"
  
  dimensions = {
    LoadBalancer = aws_lb.main.arn_suffix
  }
}
```

### Deployment Pipeline

#### 1. Container Registry
- Use Amazon ECR for container images
- Tag images with git commit SHA
- Implement image scanning for vulnerabilities

#### 2. Blue-Green Deployment
```hcl
resource "aws_ecs_service" "app" {
  deployment_configuration {
    deployment_circuit_breaker {
      enable   = true
      rollback = true
    }
  }
  
  # Enable blue-green deployments
  deployment_controller {
    type = "ECS"
  }
}
```

#### 3. Infrastructure Validation
- Use `terraform plan` in CI/CD
- Implement drift detection
- Use Terraform Cloud/Enterprise for state management

---

## Security Guidelines

### Application Security
1. **Authentication**: JWT tokens with 15-minute expiry
2. **Authorization**: Role-based access control (RBAC)
3. **Input Validation**: Validate all inputs at API gateway
4. **SQL Injection**: Use parameterized queries only
5. **HTTPS**: Enforce TLS 1.2+ for all communications

### Infrastructure Security
1. **Secrets Management**: Use AWS Secrets Manager
2. **Network Isolation**: Private subnets for all services
3. **Encryption**: Enable encryption at rest and in transit
4. **Access Control**: Implement least privilege IAM policies
5. **Vulnerability Scanning**: Regular container and dependency scans

---

## Testing Guidelines

### Unit Testing
- Minimum 80% code coverage
- Use JUnit 5 and Mockito
- Test business logic thoroughly
- Mock external dependencies

### Integration Testing
- Use Testcontainers for database tests
- Test API endpoints with MockMvc
- Validate service interactions

### Performance Testing
- Load test critical endpoints
- Monitor response times < 200ms
- Test database connection pooling

---

## CI/CD Guidelines

### Pipeline Stages
1. **Build**: Compile and package services
2. **Test**: Unit and integration tests
3. **Security Scan**: Dependency and SAST scanning
4. **Deploy**: Environment-specific deployments
5. **Smoke Tests**: Post-deployment validation

### Deployment Strategy
- **Development**: Automatic deployment on merge to develop
- **Staging**: Manual approval required
- **Production**: Blue-green deployment with rollback capability

### Monitoring
- Track deployment success rates
- Monitor application health post-deployment
- Set up alerts for deployment failures

---

## Quick Reference Commands

### Development
```bash
# Build all services
./scripts/build-all.sh

# Run tests
./scripts/test-all.sh

# Run specific service
cd services/user-service && mvn spring-boot:run
```

### Infrastructure
```bash
# Plan infrastructure changes
terraform plan -var-file="environments/dev/terraform.tfvars"

# Apply changes
terraform apply -var-file="environments/dev/terraform.tfvars"

# Validate configuration
terraform validate
```

### Docker
```bash
# Build service image
docker build -t user-service:latest services/user-service/

# Run local stack
docker-compose -f infrastructure/docker-compose/docker-compose.yml up
```