# Flyway PostgreSQL Version Issue - Solutions

## Problem
Flyway error: "Unsupported Database: PostgreSQL 15.13"

## Solutions

### Solution 1: Updated Flyway Version (Already Applied)
The `build.gradle` has been updated to use Flyway 10.15.0 which supports PostgreSQL 15:
```gradle
implementation 'org.flywaydb:flyway-core:10.15.0'
implementation 'org.flywaydb:flyway-database-postgresql:10.15.0'
```

**To apply:**
```bash
cd credit-card-service
./gradlew clean build
```

### Solution 2: Use Development Profile (Flyway Disabled)
Run the application with Flyway disabled:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Or set environment variable:
```bash
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

### Solution 3: Use PostgreSQL 14
Use the alternative docker-compose file with PostgreSQL 14:
```bash
docker-compose -f docker-compose-pg14.yml up -d postgres
```

### Solution 4: Disable Flyway Temporarily
Add to `application.yml` or as environment variable:
```yaml
spring:
  flyway:
    enabled: false
```

Or run with:
```bash
./gradlew bootRun --args='--spring.flyway.enabled=false'
```

### Solution 5: Manual Schema Creation
If Flyway continues to have issues:

1. Connect to PostgreSQL:
```bash
docker exec -it creditcard-postgres psql -U creditcard_user -d creditcard_db
```

2. Run the schema manually:
```sql
-- Copy and paste the content from:
-- src/main/resources/db/migration/V1__create_credit_card_schema.sql
```

3. Run with Flyway disabled and Hibernate validate:
```bash
./gradlew bootRun --args='--spring.flyway.enabled=false'
```

## Verification Steps

1. Check PostgreSQL version:
```bash
docker exec creditcard-postgres psql -U creditcard_user -c "SELECT version();"
```

2. Check Flyway version in logs:
```bash
./gradlew bootRun | grep Flyway
```

3. Test database connection:
```bash
docker exec creditcard-postgres pg_isready
```

## Recommended Approach

For immediate development:
1. Use Solution 2 (dev profile) to start quickly
2. Once running, re-enable Flyway for production readiness

For production:
1. Ensure Solution 1 is applied (Flyway 10.15.0)
2. Use the standard `docker-compose.yml` with PostgreSQL 15
3. Run with default profile
