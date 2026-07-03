# Governance Policy Management System

A microservices-based governance policy management system with audit logging, built using Spring Boot, Apache Kafka, and PostgreSQL.

## Architecture

The system follows an event-driven microservices architecture with two independent services communicating asynchronously via Apache Kafka.

  Governance Service (Port 8081) 
 (REST API) │─────▶│ - Policy CRUD operations 
  - Status transitions 
  - Kafka event publisher 
   │
   ▼
 Kafka Topic (Port 9092)
 governance-events
   │
   ▼
 Audit Service (Port 8082) 
   - Kafka event consumer 
   - Immutable audit logging 
   - PostgreSQL (Audit Logs) 

### Key Components

| Component | Description |
|-----------|-------------|
| **Governance Service** | Manages policies and their lifecycle. Publishes events to Kafka. |
| **Audit Service** | Listens to Kafka events and stores immutable audit logs. |
| **Apache Kafka** | Event streaming platform for async communication. |
| **PostgreSQL** | Separate databases for policies and audit logs. |

## System Flow

1. **Client** sends request to Governance Service (REST API)
2. **Governance Service**:
   - Creates/updates policy in PostgreSQL
   - Publishes event to Kafka (`governance-events` topic)
3. **Kafka** stores events asynchronously
4. **Audit Service** consumes events from Kafka
5. **Audit Service** stores audit logs in PostgreSQL

### Policy Lifecycle

DRAFT → PENDING_APPROVAL → APPROVED
DRAFT → PENDING_APPROVAL → REJECTED

## Instructions to run the system
  1. Start Infrastructure with Docker
     on cmd run docker-compose up -d
   This starts:

   PostgreSQL on port 5432 (Governance DB)
   PostgreSQL on port 5433 (Audit DB)
   Zookeeper on port 2181
   Kafka on port 9092
  2. Create Kafka Topic
    run below command on cmd
    docker exec -it kafka-broker kafka-topics --create \
    --topic governance-events \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1

   3. Running the Services
      - Start Governance Service
      - Start Audit Service
   4. Testing the System
      using Swagger UI test the system with  this URL http://localhost:8081/swagger-ui/index.html
      




