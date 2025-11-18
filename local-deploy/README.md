# Event Planner Backend - Local Deployment

Complete Docker-based local deployment for the Event Planner microservices backend.

## Documentation

- **[PREREQUISITES.md](PREREQUISITES.md)** - Install Docker, Java, Maven (macOS/Windows/Ubuntu)
- **[QUICKSTART.md](QUICKSTART.md)** - Get running in 5 minutes
- **README.md** - This file (complete reference)

## Quick Start

```bash
# 1. Build services
./build-services.sh

# 2. Start everything
docker-compose up -d

# 3. Wait for services to be ready (60 seconds)
sleep 60

# 4. Test
curl http://localhost:8081/actuator/health
curl http://localhost:8082/api/v1/events/explore
```

## What's Included

### Infrastructure Services
- **PostgreSQL** (Auth DB on 5432, Event DB on 5433)
- **MongoDB** (Audit logs on 27017)
- **Redis** (Cache on 6379)
- **Kafka + Zookeeper** (Messaging on 9092)
- **LocalStack** (AWS services mock on 4566)

### Application Services
- **Auth Service** (8081) - User authentication & authorization
- **Event Service** (8082) - Event management
- **Notification Service** (8085) - Email/SMS notifications

### Seeded Test Data
- ✅ **51 Users** (1 admin + 50 regular users)
  - Admin: `admin@example.com`
  - Users: `bob.anderson2@example.com`, `alice.perez3@example.com`, etc.
  - Password for all: `Test123!`
  
- ✅ **100 Events** (Tech conferences, workshops, meetups)
  - View: `curl http://localhost:8082/api/v1/events/explore`

## Prerequisites

- Docker Desktop (24.0+)
- Java 21
- Maven 3.9+

**Need help installing?** See [PREREQUISITES.md](PREREQUISITES.md) for detailed installation guides for macOS, Windows, and Ubuntu.

## Setup

### 1. Build Services

```bash
chmod +x build-services.sh
./build-services.sh
```

### 2. Start Services

```bash
# Start all services
docker-compose up -d

# Or start infrastructure only
docker-compose --profile infra up -d

# Or start with application services
docker-compose --profile services up -d
```

### 3. Verify

```bash
# Check status
docker-compose ps

# Check logs
docker-compose logs -f auth-service
```

## Testing APIs

### Login with Seeded User

```bash
# 1. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "bob.anderson2@example.com", "password": "Test123!"}'

# 2. Get OTP from logs
docker logs gep-auth-service | grep -i "otp" | tail -3

# 3. Verify OTP
curl -X POST http://localhost:8081/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "bob.anderson2@example.com", "otp": "YOUR_OTP"}'
```

### View Seeded Events

```bash
# List events (no auth required)
curl http://localhost:8082/api/v1/events/explore

# Get specific page
curl 'http://localhost:8082/api/v1/events/explore?page=0&size=10'
```

### Create New Event (Requires Auth)

```bash
TOKEN="your_jwt_token"

curl -X POST http://localhost:8082/api/v1/events \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Event",
    "description": "Event description",
    "eventTime": "2025-12-15T09:00:00",
    "startTime": "2025-12-15T09:00:00",
    "endTime": "2025-12-15T18:00:00",
    "location": "San Francisco, CA",
    "flyerUrl": "https://example.com/flyer.jpg"
  }'
```

## Service Endpoints

| Service | Port | Health Check | Swagger |
|---------|------|--------------|---------|
| Auth Service | 8081 | http://localhost:8081/actuator/health | http://localhost:8081/swagger-ui.html |
| Event Service | 8082 | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui.html |
| Notification Service | 8085 | http://localhost:8085/actuator/health | - |

## Management Commands

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean reset)
docker-compose down -v

# Restart a service
docker-compose restart auth-service

# View logs
docker-compose logs -f auth-service

# Check database
docker exec gep-postgres-auth psql -U auth_user -d authdb -c "SELECT COUNT(*) FROM users;"
docker exec gep-postgres-event psql -U event_user -d eventdb -c "SELECT COUNT(*) FROM event_schema.events;"
```

## Seed Data Details

### Users (51 total)
All users have password: `Test123!`

Sample users:
- bob.anderson2@example.com
- alice.perez3@example.com
- charlie.smith4@example.com
- diana.davis5@example.com

### Events (100 total)
Various tech events including:
- Tech Meetups
- Coding Workshops
- Design Sprints
- Business Summits
- Innovation Forums

## Troubleshooting

### Services Won't Start
```bash
# Check logs
docker-compose logs <service-name>

# Clean restart
docker-compose down -v
docker-compose up -d
```

### Port Already in Use
```bash
# macOS/Linux
lsof -i :8081
kill -9 <PID>

# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### Build Fails
```bash
# Set Maven options
export MAVEN_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED"
./build-services.sh
```

### Reseed Database
```bash
# Stop services
docker-compose down -v

# Start fresh (seeds auto-apply)
docker-compose up -d
sleep 60
```

## Configuration

### Environment Variables
Copy `.env.example` to `.env` and customize:
- Database credentials
- JWT secrets
- AWS/LocalStack settings
- Email configuration

### Key Settings
- Spring Profile: `dev`
- JWT Secret: Base64 encoded
- Database: PostgreSQL 16
- Java: 21

## Project Structure

```
local-deploy/
├── docker-compose.yml       # Service orchestration
├── .env.example            # Environment template
├── build-services.sh       # Build script
├── start.sh               # Start script
├── stop.sh                # Stop script
├── init-scripts/          # Database initialization
│   ├── auth-db-seed.sql   # 51 users
│   └── event-db-seed-simple.sql  # 100 events
└── README.md              # This file
```

## Notes

- Discovery Server and API Gateway are not yet implemented
- Email notifications require SMTP configuration
- This is a development setup - not for production
- Seed data is automatically applied on fresh database creation

## Support

For issues or questions:
1. Check logs: `docker-compose logs -f <service-name>`
2. Verify services: `docker-compose ps`
3. Try clean restart: `docker-compose down -v && docker-compose up -d`

---

**Ready to develop! 🚀**
