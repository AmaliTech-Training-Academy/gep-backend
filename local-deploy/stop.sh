#!/bin/bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "======================================"
echo "Stopping Event Planner Platform"
echo "======================================"

# Check for flags
REMOVE_VOLUMES=false
if [ "$1" == "--clean" ] || [ "$1" == "-c" ]; then
    REMOVE_VOLUMES=true
    echo -e "${YELLOW}Warning: This will remove all data volumes!${NC}"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Cancelled."
        exit 0
    fi
fi

# Stop services
echo "Stopping all services..."
if [ "$REMOVE_VOLUMES" = true ]; then
    docker-compose --profile services down -v
    echo -e "${GREEN}✓ All services stopped and volumes removed${NC}"
else
    docker-compose --profile services down
    echo -e "${GREEN}✓ All services stopped (data preserved)${NC}"
fi

echo ""
echo "To start again, run: ./start.sh"
echo "To remove all data, run: ./stop.sh --clean"
