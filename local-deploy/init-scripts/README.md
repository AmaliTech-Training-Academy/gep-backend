# Database Seed Data

## Overview
This directory contains initialization and seed scripts for the databases.

## Files

- **auth-db-seed.sql** - Seeds auth database with 51 users
- **event-db-seed.sql** - Seeds event database with 100 events
- **event-db-init.sql** - Creates event database schema
- **localstack-init.sh** - Initializes AWS services in LocalStack
- **generate-seed-data.sh** - Script to regenerate seed data

## Seed Data Contents

### Users (51 total)
- 1 Admin user: `admin@example.com`
- 50 Regular users: `firstname.lastnameN@example.com`
- All passwords: `Test123!`

### Events (100 total)
- Various categories: Technology, Business, Networking, Workshops, Conferences
- Distributed across different organizers
- Realistic attendee registrations
- Future dates starting from today

## Regenerate Seed Data

To create new seed data with different counts:

```bash
./generate-seed-data.sh
```

## Apply Seed Data

Seed data is automatically applied when you start fresh:

```bash
cd ../
docker-compose down -v
docker-compose up -d
```

## Test Credentials

Login with any user:
- Email: `john.smith2@example.com` (or any from the seed)
- Password: `Test123!`
