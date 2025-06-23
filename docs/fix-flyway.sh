#!/bin/bash

echo "Applying Flyway PostgreSQL 15 fix..."

cd credit-card-service

# Clean and rebuild with new dependencies
echo "1. Cleaning and rebuilding project..."
./gradlew clean build -x test

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✓ Build successful with updated Flyway version"
else
    echo "✗ Build failed"
    exit 1
fi

# Option to run immediately
echo ""
echo "Build complete. Would you like to:"
echo "1) Run with Flyway enabled (standard mode)"
echo "2) Run with Flyway disabled (dev mode)"
echo "3) Exit"
echo ""
read -p "Enter choice (1-3): " choice

case $choice in
    1)
        echo "Starting application with Flyway..."
        ./gradlew bootRun
        ;;
    2)
        echo "Starting application without Flyway..."
        ./gradlew bootRun --args='--spring.profiles.active=dev'
        ;;
    3)
        echo "Exiting. You can run the application manually with:"
        echo "  ./gradlew bootRun"
        echo "or"
        echo "  ./gradlew bootRun --args='--spring.profiles.active=dev'"
        ;;
    *)
        echo "Invalid choice."
        ;;
esac
