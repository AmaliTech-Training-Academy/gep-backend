#!/bin/bash

set -e

echo "======================================"
echo "Building Event Planner Services"
echo "======================================"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Navigate to services directory
cd ../services

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    echo "Please install Maven first:"
    echo "  macOS: brew install maven"
    echo "  Linux: sudo apt-get install maven"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo -e "${RED}Error: Java 21 or higher is required${NC}"
    echo "Current Java version: $JAVA_VERSION"
    exit 1
fi

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
    elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
    fi
    echo "Set JAVA_HOME to: $JAVA_HOME"
fi

echo -e "${GREEN}✓ Prerequisites check passed${NC}"
echo ""

# Build specific service or all services
if [ -z "$1" ]; then
    echo -e "${YELLOW}Building all services...${NC}"
    
    # Clean and build all
    echo "Step 1/2: Cleaning previous builds..."
    mvn clean -q
    
    echo "Step 2/2: Building all services..."
    MAVEN_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED" mvn package -DskipTests -B
    
    echo ""
    echo -e "${GREEN}✓ All services built successfully!${NC}"
else
    SERVICE=$1
    echo -e "${YELLOW}Building $SERVICE...${NC}"
    
    if [ ! -d "$SERVICE" ]; then
        echo -e "${RED}Error: Service '$SERVICE' not found${NC}"
        echo "Available services:"
        ls -d */ | grep -v target | sed 's/\///'
        exit 1
    fi
    
    MAVEN_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED" mvn clean package -DskipTests -pl $SERVICE -am -B
    
    echo ""
    echo -e "${GREEN}✓ $SERVICE built successfully!${NC}"
fi

echo ""
echo "======================================"
echo "Build Complete!"
echo "======================================"
echo ""
echo "Next steps:"
echo "  1. Start infrastructure: docker-compose up -d"
echo "  2. Start services: docker-compose --profile services up -d"
echo "  3. Check status: docker-compose ps"
