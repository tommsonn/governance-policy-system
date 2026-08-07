# Governance Policy Management System

A microservices-based governance policy management system with audit logging, built using Spring Boot, Apache Kafka, PostgreSQL, and enhanced with API Gateway, JWT Authentication, Service Discovery, and gRPC.

##  Architecture Explanation

### System Architecture 

The system follows an **event-driven microservices architecture** with two independent services communicating asynchronously via Apache Kafka, enhanced with an API Gateway, service discovery, and gRPC.

                         ┌─────────────┐              
                         |   Client    |
                         └─────────────┘
                                |
                                ▼
                ┌───────────────────────────────────────────┐
                │ API Gateway (Spring Cloud Gateway)        │
                |     - JWT Authentication & Authoriza      │
                │     - Rate Limiting (Redis)               │
                │     - Circuit Breaker (Resilience4j)      │
                │     - Service Discovery (Eureka)          │
                │     - Load Balancing                      │
                └───────────────────────────────────────────┘
                                 │
             ┌───────────────────┼───────────────────┐
             ▼                   ▼                   ▼
     ┌─────────────────────┐ ┌────────────────┐ ┌───────────────────┐
     │    Eureka Server    │ │ Policy Service │ │ Audit Service     │
     │  (Service Registry) │ │ (Port 8081)    │ │ (Port 8082)       │
     │ (Port 8761)         │ │ - REST APIs    │ │ - Kafka Consumer  │
     └─────────────────────┘ │ - gRPC Client  │ │ - gRPC Server     │
                │            └────────────────┘ └───────────────────┘
                │                   │                         │
                │                   └───       gRPC   ────────┘
                │                       (Internal communication)
                ▼
    ┌─────────────────────────────────────────┐
    │ Redis (Rate Limiting)                   │
    │ (Port 6379)                             │
    └─────────────────────────────────────────┘


### Key Components

  | Component | Description |
  |-----------|-------------|
  | **API Gateway** | Single entry point for all client requests. Handles JWT validation, rate limiting, circuit breaking, and routing. |
  | **Eureka Server** | Service registry that enables dynamic service discovery and load balancing. |
  | **Policy Service** | Manages governance policies and their lifecycle (DRAFT → PENDING_APPROVAL → APPROVED/REJECTED). Publishes events to Kafka. |
  | **Audit Service** | Listens to Kafka events and gRPC requests, storing immutable audit logs. |
  | **Apache Kafka** | Event streaming platform for asynchronous communication between services. |
  | **PostgreSQL** | Separate databases for policies (governance) and audit logs (audit). |
  | **Redis** | In-memory data store used for rate limiting. |

### Policy Lifecycle
    DRAFT → PENDING_APPROVAL → APPROVED
    DRAFT → PENDING_APPROVAL → REJECTED

### Authentication Flow

  1. **Client** sends login request to `/auth/login` with username/password
  2. **AuthController** validates credentials and returns JWT token
  3. **Client** sends subsequent requests with JWT token in `Authorization: Bearer <token>` header
  4. **JwtAuthenticationFilter** validates the token and extracts user identity
  5. **Gateway** forwards user identity to downstream services via headers:
     - `X-User-Id`: User ID
     - `X-User-Name`: Username
     - `X-User-Role`: User role (ADMIN/USER)

   ##  Instructions to Run the System

### 1. Clone the Repository

    ```bash
     git clone https://github.com/tommsonn/governance-policy-system.git
     cd governance-policy-system

### 2. Start Infrastructure with Docker
    docker-compose up -d

  This starts:
- PostgreSQL on port 5434 (Governance DB)
- PostgreSQL on port 5433 (Audit DB)
- Zookeeper on port 2181
- Kafka on port 9092
- Redis on port 6379 (Rate Limiting)

### 3. Create Kafka Topic

    docker exec -it kafka-broker kafka-topics --create \
    --topic governance-events \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1

### 4. Build All Services
     1. Build Governance Service
         cd ../governance-service
         mvn clean install -DskipTests
    2. Build Audit Service
         cd ../audit-service
         mvn clean install -DskipTests
    3. Build API Gateway
         cd ../api-gateway
         mvn clean install -DskipTests
    4. Build Eureka Server
         cd ../eureka-server
         mvn clean install -DskipTests   

### 5. Start Services (In Order)
    1. Start Eureka Server (Service Registry)
    2. Start API Gateway
    3. Start Audit Service
    4. Start Governance Service

### 6. Service URLs
    - API Gateway	=>	http://localhost:8080
    - Governance Service => http://localhost:8081
    - Audit Service	=>	http://localhost:8082
    - Eureka Server	=>	http://localhost:8761
    - Swagger UI	=>	http://localhost:8081/swagger-ui/index.html
