#!/bin/bash

# Quick test script for successful application

BASE_URL="http://localhost:8080"
AUTH="admin:admin123"

echo "=== Credit Card Application Quick Test ==="
echo ""
echo "This script submits applications with better chances of approval"
echo ""

# Function to submit application and show result
submit_application() {
    local file=$1
    local description=$2
    
    echo "Submitting $description..."
    
    response=$(curl -s -X POST "$BASE_URL/api/v1/credit-card-applications" \
        -u "$AUTH" \
        -H "Content-Type: application/json" \
        -d @"$file" \
        -w "\n|||HTTP_STATUS:%{http_code}|||")
    
    json_response=$(echo "$response" | sed 's/|||HTTP_STATUS:.*//')
    http_status=$(echo "$response" | grep -o '|||HTTP_STATUS:[0-9]*|||' | sed 's/[^0-9]//g')
    
    # Parse key fields
    status=$(echo "$json_response" | jq -r '.status' 2>/dev/null)
    credit_score=$(echo "$json_response" | jq -r '.creditScore' 2>/dev/null)
    risk_score=$(echo "$json_response" | jq -r '.riskScore' 2>/dev/null)
    approved_limit=$(echo "$json_response" | jq -r '.approvedLimit' 2>/dev/null)
    app_number=$(echo "$json_response" | jq -r '.applicationNumber' 2>/dev/null)
    
    echo "Result:"
    echo "  Application Number: $app_number"
    echo "  Status: $status"
    echo "  Credit Score: $credit_score"
    echo "  Risk Score: $risk_score"
    echo "  Approved Limit: $approved_limit"
    echo "  HTTP Status: $http_status"
    echo ""
}

# Submit different applications
submit_application "good-credit-application.json" "Good Credit Application"
submit_application "high-income-application.json" "High Income Application"
submit_application "submit-application.json" "Standard Application"

echo "=== Summary ==="
echo "Applications are processed with:"
echo "- 70% chance of good credit score (650-850)"
echo "- 20% chance of medium credit score (580-649)"
echo "- 10% chance of poor credit score (300-579)"
echo ""
echo "Approval requirements:"
echo "- Minimum credit score: 580"
echo "- Maximum risk score: 75"
echo ""
echo "Tip: Run multiple times to see different outcomes due to random credit scores"
