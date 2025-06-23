-- V1__create_credit_card_schema.sql

-- Create customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    ssn VARCHAR(11) UNIQUE NOT NULL,
    date_of_birth DATE NOT NULL,
    street_address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    country VARCHAR(50),
    identity_verified BOOLEAN DEFAULT FALSE,
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create credit card applications table
CREATE TABLE credit_card_applications (
    id UUID PRIMARY KEY,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    customer_id UUID NOT NULL,
    requested_limit DECIMAL(10,2),
    approved_limit DECIMAL(10,2),
    annual_income DECIMAL(12,2) NOT NULL,
    employment_status VARCHAR(50) NOT NULL,
    credit_score INTEGER,
    risk_score DECIMAL(5,2),
    card_type VARCHAR(20),
    decision_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    decided_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create indexes
CREATE INDEX idx_applications_status ON credit_card_applications(status);
CREATE INDEX idx_applications_customer ON credit_card_applications(customer_id);
CREATE INDEX idx_customers_ssn ON customers(ssn);
CREATE INDEX idx_customers_email ON customers(email);
