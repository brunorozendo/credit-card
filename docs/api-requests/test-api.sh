#!/bin/bash

# Credit Card Application API Test Commands

# Base URL - update this based on your environment
BASE_URL="http://localhost:8080"
# Basic auth credentials
AUTH="admin:admin123"

# Function to make API call and parse response
api_call() {
    local method=$1
    local url=$2
    local data=$3
    
    if [ -z "$data" ]; then
        response=$(curl -s -X "$method" "$url" \
            -u "$AUTH" \
            -w "\n|||HTTP_STATUS:%{http_code}|||")
    else
        response=$(curl -s -X "$method" "$url" \
            -u "$AUTH" \
            -H "Content-Type: application/json" \
            -d "$data" \
            -w "\n|||HTTP_STATUS:%{http_code}|||")
    fi
    
    json_response=$(echo "$response" | sed 's/|||HTTP_STATUS:.*//')
    http_status=$(echo "$response" | grep -o '|||HTTP_STATUS:[0-9]*|||' | sed 's/[^0-9]//g')
    
    if [ -n "$json_response" ] && [ "$json_response" != "" ]; then
        echo "$json_response" | jq . 2>/dev/null || echo "$json_response"
    fi
    echo "HTTP Status: $http_status"
}

echo "=== Credit Card Application API Tests ==="
echo ""

# 1. Submit a new application
echo "1. Submitting a new credit card application..."
api_call "POST" "$BASE_URL/api/v1/credit-card-applications" "@submit-application.json"

# Extract application number from the response for later use
APP_NUMBER=$(echo "$json_response" | jq -r '.applicationNumber' 2>/dev/null)

echo ""
echo "2. Get application by application number..."
if [ -n "$APP_NUMBER" ] && [ "$APP_NUMBER" != "null" ]; then
    echo "Using application number: $APP_NUMBER"
    api_call "GET" "$BASE_URL/api/v1/credit-card-applications/$APP_NUMBER"
else
    read -p "Enter application number: " APP_NUMBER
    api_call "GET" "$BASE_URL/api/v1/credit-card-applications/$APP_NUMBER"
fi

echo ""
echo "3. Get applications by customer email..."
echo "Using email: john.doe@example.com"
api_call "GET" "$BASE_URL/api/v1/credit-card-applications/customer/john.doe@example.com"

echo ""
echo "4. Get all pending applications..."
api_call "GET" "$BASE_URL/api/v1/credit-card-applications/pending"

echo ""
echo "5. Check health endpoint..."
response=$(curl -s -X GET "$BASE_URL/actuator/health" -w "\n|||HTTP_STATUS:%{http_code}|||")
json_response=$(echo "$response" | sed 's/|||HTTP_STATUS:.*//')
http_status=$(echo "$response" | grep -o '|||HTTP_STATUS:[0-9]*|||' | sed 's/[^0-9]//g')
echo "$json_response" | jq . 2>/dev/null || echo "$json_response"
echo "HTTP Status: $http_status"

echo ""
echo "=== Additional Test Cases ==="
echo ""

# Test with invalid data
echo "6. Submit application with invalid data (missing required fields)..."
api_call "POST" "$BASE_URL/api/v1/credit-card-applications" '{"firstName": "Test"}'

# Test duplicate application
echo ""
echo "7. Submit duplicate application (same SSN)..."
api_call "POST" "$BASE_URL/api/v1/credit-card-applications" "@submit-application.json"

# Test without authentication
echo ""
echo "8. Test endpoint without authentication..."
response=$(curl -s -X GET "$BASE_URL/api/v1/credit-card-applications/pending" \
    -w "\nHTTP Status: %{http_code}\n")
echo "$response"

echo ""
echo "=== High Income Application Test ==="
echo ""

# Test with high income application
echo "9. Submit high income application (better approval chances)..."
if [ -f "high-income-application.json" ]; then
    api_call "POST" "$BASE_URL/api/v1/credit-card-applications" "@high-income-application.json"
else
    echo "high-income-application.json not found, skipping..."
fi

echo ""
echo "10. Submit good credit application (high approval chance)..."
if [ -f "good-credit-application.json" ]; then
    api_call "POST" "$BASE_URL/api/v1/credit-card-applications" "@good-credit-application.json"
else
    echo "good-credit-application.json not found, skipping..."
fi

echo ""
echo "=== Tests Complete ==="
echo ""
echo "Notes:"
echo "- Applications may be approved or rejected based on random credit scores"
echo "- Credit scores are distributed: 70% good (650-850), 20% medium (580-649), 10% poor (300-579)"
echo "- Minimum credit score for approval: 580"
echo "- Maximum risk score for approval: 75"
