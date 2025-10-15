# Event Planner Backend

Java microservices backend for the Event Planner platform.

## Quick Start

```bash
# Build all services
./scripts/build-all.sh

# Run tests
./scripts/test-all.sh
```

## Services

- **user-service**: User management and authentication
- **event-service**: Event creation and management
- **notification-service**: Email and push notifications
- **api-gateway**: API gateway and routing

## Shared Libraries

- **common-lib**: Common utilities and DTOs
- **security-lib**: Authentication and authorization
- **messaging-lib**: Event messaging contracts


## Architecture Diagram
```mermaid
    graph TB
    Client[Client Applications<br/>Web/Mobile]

    subgraph "Entry Point"
        Gateway[API Gateway<br/>Port: 8080]
    end

    subgraph "Core Services"
        Auth[Auth Service<br/>Port: 8081<br/>Authentication & Authorization]
        Discovery[Discovery Service<br/>Port: 8761<br/>Service Registry]
        Event[Event Service<br/>Port: 8082<br/>Event CRUD Operations]
        Booking[Booking Service<br/>Port: 8083<br/>Booking Management]
        Payment[Payment Service<br/>Port: 8084<br/>Payment Processing]
        Notification[Notification Service<br/>Port: 8085<br/>Email/SMS/Push]
    end

    subgraph "Message Broker"
        Kafka[Apache Kafka<br/>Event Streaming Platform]
    end

subgraph "Caching Layer"
Redis[Redis Cache<br/>• OTP Storage<br/>• Session Data<br/>• Frequently Accessed Data]
end

subgraph "Databases"
AuthDB[(Auth DB<br/>PostgreSQL)]
EventDB[(Event DB<br/>PostgreSQL)]
BookingDB[(Booking DB<br/>PostgreSQL)]
PaymentDB[(Payment DB<br/>PostgreSQL)]
end

Client -->|1. HTTP Requests| Gateway

Gateway -->|2. Validate Token| Auth
Gateway -->|Route Requests| Event
Gateway -->|Route Requests| Booking
Gateway -->|Route Requests| Payment

Auth -.->|Register/Discover| Discovery
Event -.->|Register/Discover| Discovery
Booking -.->|Register/Discover| Discovery
Payment -.->|Register/Discover| Discovery
Notification -.->|Register/Discover| Discovery
Gateway -.->|Service Discovery| Discovery

Auth <-->|Store/Verify OTP<br/>Cache Sessions| Redis
Event <-->|Cache Events| Redis
Booking <-->|Cache Bookings| Redis
Gateway <-->|Cache Responses| Redis

Auth <-->|Read/Write| AuthDB
Event <-->|Read/Write| EventDB
Booking <-->|Read/Write| BookingDB
Payment <-->|Read/Write| PaymentDB

Event -->|Publish:<br/>event.created<br/>event.updated| Kafka
Booking -->|Publish:<br/>booking.created<br/>booking.cancelled| Kafka
Payment -->|Publish:<br/>payment.completed<br/>payment.failed| Kafka

Kafka -->|Subscribe:<br/>booking.created| Notification
Kafka -->|Subscribe:<br/>payment.completed| Notification
Kafka -->|Subscribe:<br/>event.created| Notification

Kafka -->|Subscribe:<br/>payment.completed| Booking
Kafka -->|Subscribe:<br/>booking.created| Event

style Client fill:#e1f5ff
style Gateway fill:#ff9999
style Kafka fill:#9999ff
style Redis fill:#ff6b6b
style Auth fill:#99ff99
style Discovery fill:#ffff99
style Event fill:#99ff99
style Booking fill:#99ff99
style Payment fill:#99ff99
style Notification fill:#99ff99
style AuthDB fill:#ffcc99
style EventDB fill:#ffcc99
style BookingDB fill:#ffcc99
style PaymentDB fill:#ffcc99
```
