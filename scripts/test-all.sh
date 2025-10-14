#!/bin/bash
set -e

echo "Running tests for all services..."
mvn test

echo "All tests completed successfully!"