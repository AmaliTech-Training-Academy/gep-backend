# Event Planner Backend

A Java Spring Boot microservices backend for the Event Planner platform, deployed on AWS ECS Fargate with PostgreSQL, Redis, and SQS/SNS messaging.

## Architecture

![Backend Architecture](backend-architecture.png)

## Services Overview

### Core Services
- **auth-service** (Port 8081): User authentication, profile management, JWT tokens, and user statistics
- **event-service** (Port 8082): Event CRUD operations, image uploads, event strategies, and validations
- **notification-service** (Port 8085): Email notifications via AWS SQS/SNS integration
- **common-libraries**: Shared utilities, S3Service, DTOs, exceptions, and JWT handling

### Shared Libraries
- **common-lib**: Common DTOs, utilities, and configurations
- **security-lib**: JWT authentication and authorization
- **messaging-lib**: Event messaging contracts

## Technology Stack

### Core Technologies
- **Framework**: Spring Boot 3.5.6
- **Java Version**: Java 21
- **Build Tool**: Maven (Multi-module)
- **Database**: PostgreSQL (AWS RDS)
- **Cache**: Redis (AWS ElastiCache)
- **Messaging**: AWS SQS/SNS
- **File Storage**: AWS S3
- **Container Platform**: AWS ECS Fargate

### Key Dependencies
- **Spring Cloud**: 2025.0.0
- **AWS Spring Cloud**: 3.1.1
- **AWS SDK**: 2.26.0
- **JWT**: 0.12.6 (jjwt)
- **MapStruct**: 1.6.3
- **SpringDoc OpenAPI**: 2.8.8
- **JaCoCo**: 0.8.12 (Code Coverage)

## Project Structure

```
gep-backend/
├── services/
│   ├── auth-service/           # User authentication & management
│   ├── event-service/          # Event operations & image handling
│   ├── notification-service/   # Email notifications
│   ├── common-libraries/      # Shared utilities & services
│   └── discovery-server/      # Service discovery (Eureka)
├── shared/                    # Legacy shared libraries
├── docs/                      # API documentation & architecture
├── .github/workflows/         # CI/CD pipeline
└── local-deploy/             # Local development setup
```

## Key Features

### Authentication & Security
- JWT-based authentication with refresh tokens
- Role-based access control (RBAC)
- OAuth2 integration support
- Password reset via email
- User profile management with image uploads

### Event Management
- Event creation with multiple strategies (Virtual/In-Person, Single/Multi-day)
- Image upload support (flyers and event images)
- Event validation and business rules
- Capacity management and approval workflows

### Messaging & Notifications
- Asynchronous messaging via AWS SQS/SNS
- Email notifications for user events
- Event-driven architecture for service communication

### File Management
- AWS S3 integration for image storage
- Separate buckets for static assets and user uploads
- Automatic image URL generation

## Development Setup

### Prerequisites
- Java 21
- Maven 3.8+
- Docker & Docker Compose
- AWS CLI (for cloud deployment)

### Local Development

```bash
# Clone the repository
git clone <repository-url>
cd gep-backend

# Build all services
cd services
mvn clean install

# Run individual service
cd auth-service
./mvnw spring-boot:run

# Run with Docker Compose (local stack)
cd local-deploy
docker-compose up
```

### Environment Configuration

Each service uses Spring profiles for environment-specific configuration:

- **dev**: Development environment
- **staging**: Staging environment  
- **prod**: Production environment

Environment variables are configured in `application-{profile}.properties` files.

### Key Environment Variables

```bash
# Database
AUTH_SERVICE_DB_URL=jdbc:postgresql://localhost:5432/auth_db
EVENT_SERVICE_DB_URL=jdbc:postgresql://localhost:5432/event_db

# AWS Configuration
AWS_REGION=us-east-1
AWS_S3_BUCKET=event-planner-dev-assets-904570587823
AWS_S3_BACKEND_FILES_BUCKET=event-planner-backend-dev-files-904570587823

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=86400000
```

## API Documentation

Comprehensive API documentation is available in the following files:

### API Contract Specification
- **[API Contract](docs/api/contract.md)**: Complete OpenAPI 3.0 specification with detailed endpoint documentation, request/response schemas, and authentication requirements

### Detailed API Documentation
- **[API Documentation](docs/api/docs.md)**: In-depth service documentation including architecture overview, technology stack, database schemas, security implementation, and deployment guides

### Interactive API Documentation (Swagger UI)
- Auth Service: `http://localhost:8081/swagger-ui.html`
- Event Service: `http://localhost:8082/swagger-ui.html`
- Notification Service: `http://localhost:8085/swagger-ui.html`

### Service Health Endpoints
- Auth Service: `http://localhost:8081/actuator/health`
- Event Service: `http://localhost:8082/actuator/health`
- Notification Service: `http://localhost:8085/actuator/health`

### Key API Features
- **Authentication**: JWT-based authentication with refresh tokens
- **User Management**: Complete CRUD operations with role-based access control
- **Event Management**: Event creation, management, and lifecycle operations
- **Invitation System**: Event invitation management with tracking
- **Notification System**: Asynchronous email notifications via SQS/SNS
- **File Upload**: S3 integration for profile images and event assets

## Testing

### Running Tests
```bash
# Run all tests
cd services
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# Generate aggregate coverage report
mvn jacoco:report-aggregate
```

### Test Coverage
- Minimum 80% line coverage required
- JaCoCo integration for coverage reporting
- Excludes DTOs, models, and configuration classes

## Deployment

### CI/CD Pipeline
The project uses GitHub Actions for automated deployment:

- **Trigger**: Push to `dev`, `staging`, or `main` branches
- **Build**: Maven build with tests and coverage
- **Deploy**: Triggers DevOps pipeline for ECS deployment
- **Environments**: 
  - `dev` branch to Development environment
  - `staging` branch to Staging environment
  - `main` branch to Production environment

### AWS Infrastructure
- **Compute**: ECS Fargate tasks
- **Database**: RDS PostgreSQL (Multi-AZ for production)
- **Cache**: ElastiCache Redis
- **Load Balancer**: Application Load Balancer
- **Storage**: S3 buckets for assets and user files
- **Messaging**: SQS queues and SNS topics
- **Monitoring**: CloudWatch logs and metrics

## Common Libraries

### S3Service
Handles file uploads to AWS S3 with automatic URL generation:

```java
@Service
public class S3ServiceImpl implements S3Service {
    public String uploadImage(MultipartFile file);
    public List<String> uploadImages(List<MultipartFile> files);
}
```

### JWT Utilities
JWT token generation and validation:

```java
@Component
public class JWTUtil {
    public String generateToken(UserDetails userDetails);
    public Boolean validateToken(String token, UserDetails userDetails);
}
```

### Exception Handling
Global exception handling with custom exceptions:
- `ResourceNotFoundException`
- `DuplicateResourceException`
- `FileUploadException`
- `ValidationException`

## Development Guidelines

### Code Standards
- Follow Spring Boot best practices
- Use Lombok for reducing boilerplate code
- MapStruct for object mapping
- Comprehensive unit and integration testing
- Proper exception handling and logging

### Database Design
- Each service owns its database schema
- No cross-service database access
- Use JPA/Hibernate for ORM
- Database migrations handled via Flyway (if configured)

### Security Best Practices
- JWT tokens with short expiration times
- Secure password hashing
- Input validation on all endpoints
- HTTPS enforcement in production
- Secrets management via AWS Secrets Manager

## Monitoring & Observability

### Logging
- Structured logging with SLF4J
- CloudWatch integration for centralized logs
- Correlation IDs for request tracing

### Metrics
- Spring Boot Actuator endpoints
- Custom business metrics
- CloudWatch metrics integration

### Health Checks
- Database connectivity checks
- Redis connectivity checks
- External service dependency checks

## Contributing

1. Create feature branch from `dev`
2. Follow coding standards and add tests
3. Ensure all tests pass and coverage meets requirements
4. Submit pull request with detailed description
5. Code review required before merge

## Documentation

### API Documentation
- [API Contract Specification](docs/api/contract.md) - Complete OpenAPI 3.0 specification
- [Detailed API Documentation](docs/api/docs.md) - Comprehensive service documentation

### Development & Architecture
- [Development Guidelines](DEVELOPMENT_GUIDELINES.md) - Coding standards and best practices
- [AWS Infrastructure - Development](docs/architecture/AWS_INFRASTRUCTURE_ARCHITECTURE-DEV.md)
- [AWS Infrastructure - Production](docs/architecture/AWS_INFRASTRUCTURE_ARCHITECTURE-PROD.md)