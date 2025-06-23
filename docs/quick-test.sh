#!/bin/bash

# Quick test runner for checking specific test classes

cd credit-card-service

echo "Running specific test class..."
echo ""

# Run a specific test class
if [ -n "$1" ]; then
    echo "Running test: $1"
    ./gradlew test --tests "$1" --info
else
    echo "Running all tests with summary..."
    ./gradlew test --console=plain
fi

# Show test results
echo ""
echo "Test Results:"
find build/test-results/test -name "*.xml" -exec grep -l "testcase" {} \; | wc -l
echo "test reports generated"

# Check for failures
if [ -f build/reports/tests/test/index.html ]; then
    echo "Test report available at: build/reports/tests/test/index.html"
fi
