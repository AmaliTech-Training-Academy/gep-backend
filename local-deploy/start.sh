#!/bin/bash

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "======================================"
echo "Starting Event Planner Platform"
echo "======================================"

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    echo "Copying .env.example to .env..."
    cp .env.example .env
    echo -e "${GREEN}✓ Created .env file${NC}"
    echo ""
    echo "Please review and update .env with your settings if needed."
    echo ""
fi

# Start infrastructure
echo "Step 1/3: Starting infrastructure (databases, Kafka, Redis)..."
docker-compose up -d

echo ""
echo "Waiting for infrastructure to be healthy (30 seconds)..."
sleep 30

# Check infrastructure health
echo ""
echo "Step 2/3: Checking infrastructure health..."
docker-compose ps

echo ""
echo "Step 3/3: Starting application services..."
docker-compose --profile services up -d

echo ""
echo -e "${GREEN}======================================"
echo "Platform Started Successfully!"
echo "======================================${NC}"
echo ""
echo "Service URLs:"
echo "  • API Gateway:       http://localhost:8080"
echo "  • Eureka Dashboard:  http://localhost:8761"
echo "  • Auth Service:      http://localhost:8081"
echo "  • Event Service:     http://localhost:8082"
echo "  • Notification:      http://localhost:8085"
echo ""
echo "Infrastructure:"
echo "  • PostgreSQL (Auth): localhost:5432"
echo "  • PostgreSQL (Event): localhost:5433"
echo "  • MongoDB:           localhost:27017"
echo "  • Redis:             localhost:6379"
echo "  • Kafka:             localhost:9092"
echo "  • LocalStack:        http://localhost:4566"
echo ""
echo "Useful commands:"
echo "  • View logs:    docker-compose logs -f"
echo "  • Stop all:     docker-compose --profile services down"
echo "  • Restart:      docker-compose restart <service-name>"
