#!/bin/bash

# Start local development environment

echo "Starting Credit Card Application System..."
echo ""
echo "Select startup mode:"
echo "1) Standard mode (with Flyway migrations)"
echo "2) Development mode (Flyway disabled, auto-create schema)"
echo "3) PostgreSQL 14 mode (for compatibility)"
echo ""
read -p "Enter choice (1-3): " choice

cd credit-card-service

case $choice in
    1)
        echo "Starting in standard mode..."
        docker-compose down
        docker-compose up -d postgres
        echo "Waiting for PostgreSQL to be ready..."
        sleep 10
        ./gradlew bootRun
        ;;
    2)
        echo "Starting in development mode (Flyway disabled)..."
        docker-compose down
        docker-compose up -d postgres
        echo "Waiting for PostgreSQL to be ready..."
        sleep 10
        ./gradlew bootRun --args='--spring.profiles.active=dev'
        ;;
    3)
        echo "Starting with PostgreSQL 14..."
        docker-compose down
        docker-compose -f docker-compose-pg14.yml up -d postgres
        echo "Waiting for PostgreSQL to be ready..."
        sleep 10
        ./gradlew bootRun
        ;;
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac

# To stop: Press Ctrl+C and run: docker-compose down
