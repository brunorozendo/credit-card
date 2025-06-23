#!/bin/bash

# Run tests with JaCoCo code coverage

echo "=== Running Unit Tests with JaCoCo Coverage ==="
echo ""

cd credit-card-service

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Run tests
echo ""
echo "Running tests..."
./gradlew test

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
    
    # Generate JaCoCo report
    echo ""
    echo "Generating JaCoCo coverage report..."
    ./gradlew jacocoTestReport
    
    # Run coverage verification
    echo ""
    echo "Running coverage verification (90% target)..."
    ./gradlew jacocoTestCoverageVerification
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Code coverage target met (90%+)!"
    else
        echo ""
        echo "⚠️  Code coverage below 90% target"
    fi
    
    # Display coverage summary
    echo ""
    echo "=== Coverage Report ==="
    echo "HTML Report: build/reports/jacoco/test/html/index.html"
    echo "XML Report: build/reports/jacoco/test/jacocoTestReport.xml"
    
    # Try to open HTML report in browser
    if command -v open &> /dev/null; then
        echo ""
        echo "Opening coverage report in browser..."
        open build/reports/jacoco/test/html/index.html
    elif command -v xdg-open &> /dev/null; then
        echo ""
        echo "Opening coverage report in browser..."
        xdg-open build/reports/jacoco/test/html/index.html
    else
        echo ""
        echo "To view the report, open: $(pwd)/build/reports/jacoco/test/html/index.html"
    fi
    
else
    echo ""
    echo "❌ Tests failed! Fix failing tests before checking coverage."
    exit 1
fi

echo ""
echo "=== Coverage Analysis Complete ==="
