#!/bin/bash

# Run tests in batches to avoid timeout

cd credit-card-service

echo "=== Running Tests in Batches ==="
echo ""

# Clean build
./gradlew clean

# Run tests by category
echo "1. Running Exception Tests..."
./gradlew test --tests "*ExceptionSpec"

echo ""
echo "2. Running Model Tests..."
./gradlew test --tests "*model.*Spec"

echo ""
echo "3. Running DTO Tests..."
./gradlew test --tests "*dto.*Spec"

echo ""
echo "4. Running Mapper Tests..."
./gradlew test --tests "*mapper.*Spec"

echo ""
echo "5. Running Client Tests..."
./gradlew test --tests "*client.*Spec"

echo ""
echo "6. Running Service Tests..."
./gradlew test --tests "*service.*Spec"

echo ""
echo "7. Running Controller Tests..."
./gradlew test --tests "*controller.*Spec"

echo ""
echo "8. Running Repository Tests..."
./gradlew test --tests "*repository.*Spec"

echo ""
echo "9. Running Config Tests..."
./gradlew test --tests "*config.*Spec"

echo ""
echo "10. Running Integration Tests..."
./gradlew test --tests "*integration.*Spec"

echo ""
echo "11. Running Main App Tests..."
./gradlew test --tests "*CreditCardApplicationAppSpec"

echo ""
echo "=== Generating JaCoCo Report ==="
./gradlew jacocoTestReport

echo ""
echo "=== Running Coverage Verification ==="
./gradlew jacocoTestCoverageVerification

echo ""
echo "=== Test Summary ==="
echo "HTML Report: build/reports/jacoco/test/html/index.html"
echo "Test Report: build/reports/tests/test/index.html"

# Try to extract coverage percentage
if [ -f build/reports/jacoco/test/jacocoTestReport.xml ]; then
    echo ""
    echo "Coverage Summary:"
    grep -o 'coverage[^>]*>[^<]*' build/reports/jacoco/test/jacocoTestReport.xml | head -5
fi
