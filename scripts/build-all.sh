#!/bin/bash
set -e

echo "Building all services..."
mvn clean compile -DskipTests

echo "Build completed successfully!"