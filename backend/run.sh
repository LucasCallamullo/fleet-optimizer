#!/bin/bash
# ================================================================
# run.sh - Build and run Fleet Optimizer with Docker
# ================================================================
# 
# This script manages the entire application using Docker.
# It does NOT require Maven or Java installed locally.
# Everything is compiled inside Docker containers.
#
# Usage: ./run.sh [command]
#
# Commands:
#   build    - Build all Docker images
#   up       - Start all services (build + up)
#   start    - Alias for 'up'
#   down     - Stop and remove all containers
#   restart  - Restart all services
#   logs     - View logs from all services
#   status   - Show status of all services
#   clean    - Stop containers and remove volumes (clean slate)
#   help     - Show this help message
#
# Examples:
#   ./run.sh build && ./run.sh up   # Build and start
#   ./run.sh start                  # Build and start (one command)
#   ./run.sh logs -f                # Follow logs
#   ./run.sh down                   # Stop everything
# ================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Show help
show_help() {
    echo "================================================================"
    echo "Fleet Optimizer - Docker Management Script"
    echo "================================================================"
    echo ""
    echo "Usage: ./run.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build      - Build all Docker images"
    echo "  up         - Build and start all services"
    echo "  start      - Alias for 'up'"
    echo "  down       - Stop and remove all containers"
    echo "  restart    - Restart all services"
    echo "  logs       - View logs from all services"
    echo "  status     - Show status of all services"
    echo "  clean      - Stop containers and remove volumes (fresh start)"
    echo "  help       - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./run.sh build && ./run.sh up   # Build then start"
    echo "  ./run.sh start                  # Build and start (one command)"
    echo "  ./run.sh logs -f                # Follow logs"
    echo "  ./run.sh down                   # Stop everything"
    echo ""
}

# Build all Docker images
build_images() {
    print_info "Building all Docker images..."
    docker-compose build
    print_success "All images built successfully!"
}

# Start all services (build + up)
start_services() {
    print_info "Building and starting all services..."
    docker-compose up -d --build
    print_success "All services started successfully!"
    show_status
}

# Stop all services
stop_services() {
    print_info "Stopping all services..."
    docker-compose down
    print_success "All services stopped!"
}

# Restart all services
restart_services() {
    print_info "Restarting all services..."
    docker-compose restart
    print_success "All services restarted!"
}

# Show logs
show_logs() {
    docker-compose logs "$@"
}

# Show status
show_status() {
    echo ""
    echo "================================================================"
    echo "SERVICE STATUS"
    echo "================================================================"
    
    # Show running containers
    echo ""
    docker-compose ps
    
    echo ""
    echo "================================================================"
    echo "ACCESS POINTS"
    echo "================================================================"
    echo ""
    echo "  API Gateway:    http://localhost:8080"
    echo ""
    echo "  Swagger UI (via Gateway):"
    echo "    - Fleets:     http://localhost:8080/swagger-ui.html"
    echo "    - Routes:     http://localhost:8080/swagger-ui.html"
    echo "    - Packages:   http://localhost:8080/swagger-ui.html"
    echo "    - Auth:       http://localhost:8080/swagger-ui.html"
    echo "    - Geocoding:  http://localhost:8080/swagger-ui.html"
    echo ""
    echo "================================================================"
    echo "COMMANDS"
    echo "================================================================"
    echo ""
    echo "  View logs:     ./run.sh logs -f"
    echo "  Stop services: ./run.sh down"
    echo "  Restart:       ./run.sh restart"
    echo "  Clean slate:   ./run.sh clean"
    echo ""
}

# Clean everything (stop + remove volumes)
clean_all() {
    print_warning "This will stop all containers and remove volumes (data will be lost!)"
    read -p "Are you sure? (y/N) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Stopping containers and removing volumes..."
        docker-compose down -v
        print_success "Clean complete! All volumes removed."
    else
        print_info "Operation cancelled."
    fi
}

# Main command handling
case "$1" in
    build)
        build_images
        ;;
    up|start)
        start_services
        ;;
    down)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        shift  # Remove 'logs' from arguments
        show_logs "$@"
        ;;
    status|ps)
        show_status
        ;;
    clean)
        clean_all
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        if [ -z "$1" ]; then
            show_help
        else
            print_error "Unknown command: $1"
            echo ""
            show_help
            exit 1
        fi
        ;;
esac