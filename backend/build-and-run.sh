#!/bin/bash
# ================================================================
# build-and-run.sh - Build all microservices and run with Docker
# ================================================================
# Usage: ./build-and-run.sh
# ================================================================

set -e  # Detiene el script si algún comando falla

echo "========================================================="
echo "Building all microservices..."
echo "========================================================="

# Step 1: Build each microservice
for service in ms-fleets ms-routes gateway; do
  if [ -d "$service" ]; then
    echo ""
    echo "[1/3] Building $service..."
    # create a subshell to move directoyrs and return the same pwd after that
    (cd "$service" && mvn clean package -DskipTests)
    echo "[OK] $service built successfully"
  else
    echo "[WARN] Directory $service not found, skipping..."
  fi
done

echo ""
echo "========================================================="
echo "Building Docker images..."
echo "========================================================="

# Step 2: Build Docker images
docker-compose build

echo ""
echo "========================================================="
echo "Starting services..."
echo "========================================================="

# Step 3: Start services
docker-compose up -d

echo ""
echo "========================================================="
echo "All services started successfully!"
echo "========================================================="
echo ""
echo "Services:"
echo "  - Gateway: http://localhost:8080"
echo "  - Fleets:  http://localhost:8081"
echo "  - Routes:  http://localhost:8082"
echo ""
echo "Logs: docker-compose logs -f"
echo "Stop: docker-compose down"