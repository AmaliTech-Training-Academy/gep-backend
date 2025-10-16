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
%%{init: {'theme':'base', 'themeVariables': {'fontSize': '16px', 'fontFamily': 'Arial, sans-serif'}}}%%
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

style Client fill:#2196F3,stroke:#1976D2,stroke-width:3px,color:#ffffff
style Gateway fill:#F44336,stroke:#D32F2F,stroke-width:3px,color:#ffffff
style Kafka fill:#3F51B5,stroke:#303F9F,stroke-width:3px,color:#ffffff
style Redis fill:#E91E63,stroke:#C2185B,stroke-width:3px,color:#ffffff
style Auth fill:#4CAF50,stroke:#388E3C,stroke-width:3px,color:#ffffff
style Discovery fill:#FF9800,stroke:#F57C00,stroke-width:3px,color:#000000
style Event fill:#4CAF50,stroke:#388E3C,stroke-width:3px,color:#ffffff
style Booking fill:#4CAF50,stroke:#388E3C,stroke-width:3px,color:#ffffff
style Payment fill:#4CAF50,stroke:#388E3C,stroke-width:3px,color:#ffffff
style Notification fill:#4CAF50,stroke:#388E3C,stroke-width:3px,color:#ffffff
style AuthDB fill:#795548,stroke:#5D4037,stroke-width:3px,color:#ffffff
style EventDB fill:#795548,stroke:#5D4037,stroke-width:3px,color:#ffffff
style BookingDB fill:#795548,stroke:#5D4037,stroke-width:3px,color:#ffffff
style PaymentDB fill:#795548,stroke:#5D4037,stroke-width:3px,color:#ffffff
```
