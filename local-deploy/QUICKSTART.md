# Quick Start Guide

Get the Event Planner backend running in 5 minutes.

## Prerequisites

- Docker Desktop running
- Java 21 installed
- Maven 3.9+ installed

## Steps

### 1. Build Services (3-5 minutes)

```bash
cd local-deploy
chmod +x build-services.sh
./build-services.sh
```

### 2. Start Everything (1 minute)

```bash
docker-compose up -d
```

### 3. Wait for Services (60 seconds)

```bash
sleep 60
```

### 4. Verify

```bash
# Check services
docker-compose ps

# Test auth service
curl http://localhost:8081/actuator/health

# View seeded events
curl http://localhost:8082/api/v1/events/explore
```

## What You Get

✅ **51 Test Users** (password: `Test123!`)
- admin@example.com
- bob.anderson2@example.com
- alice.perez3@example.com
- ... and 48 more

✅ **100 Sample Events**
- Tech conferences
- Workshops
- Meetups
- Networking events

## Quick Test

### Login with Seeded User

```bash
# 1. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "bob.anderson2@example.com", "password": "Test123!"}'

# 2. Get OTP from logs
docker logs gep-auth-service | grep -oE '"otp":"[0-9]{6}"' | tail -1

# 3. Verify OTP (replace YOUR_OTP)
curl -X POST http://localhost:8081/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "bob.anderson2@example.com", "otp": "YOUR_OTP"}'
```

### Browse Events

```bash
# View all events
curl http://localhost:8082/api/v1/events/explore

# Or open in browser
open http://localhost:8082/swagger-ui.html
```

## Service URLs

- Auth Service: http://localhost:8081
- Event Service: http://localhost:8082
- Notification Service: http://localhost:8085
- Auth Swagger: http://localhost:8081/swagger-ui.html
- Event Swagger: http://localhost:8082/swagger-ui.html

## Common Commands

```bash
# Stop services
docker-compose down

# View logs
docker-compose logs -f auth-service

# Restart service
docker-compose restart event-service

# Clean reset
docker-compose down -v
docker-compose up -d
```

## Troubleshooting

**Services not starting?**
```bash
docker-compose logs <service-name>
```

**Port already in use?**
```bash
# macOS/Linux
lsof -i :8081
kill -9 <PID>
```

**Need fresh start?**
```bash
docker-compose down -v
docker-compose up -d
sleep 60
```

## Next Steps

- Read [README.md](README.md) for detailed documentation
- Test APIs with Swagger UI
- Create your own events
- Explore the seeded data

---

**You're all set! 🎉**
