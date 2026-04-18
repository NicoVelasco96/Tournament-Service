# 🏆 Tournament Service

Microservice responsible for tournament lifecycle management, automated bracket generation, and match result reporting. Part of a larger tournament platform built with a microservices architecture.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [Tournament Flow](#tournament-flow)
- [Project Structure](#project-structure)

---

## Overview

**Tournament Service** handles the full lifecycle of a tournament — from creation and player registration to bracket generation and champion determination. The bracket engine supports any number of players by automatically padding to the next power of two using BYE slots, ensuring clean single-elimination rounds.

When a tournament starts or finishes, domain events are published to RabbitMQ so other services (such as the ranking service) can react accordingly.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Database | PostgreSQL (via Railway) |
| ORM | Hibernate / Spring Data JPA |
| Messaging | RabbitMQ (via CloudAMQP) |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Gradle |
| Utilities | Lombok, Slf4j |

---

## Architecture

```
TournamentController
        │
ITournamentService ──► TournamentService
        │                      │
ITournamentRepository    IBracketGeneratorService
IMatchRepository         BracketGeneratorService
        │                      │
   PostgreSQL          TournamentEventPublisher
                               │
                          RabbitMQ Exchange
                    ┌──────────┴──────────┐
             tournament.started    tournament.finished
```

### Bracket Generator

The `BracketGeneratorService` is the core of this microservice. It:

- Pads the player list to the next power of two using BYE slots
- Randomizes seedings to avoid predictable matchups
- Automatically advances players who receive a BYE
- Generates the next round once all matches in the current round are finished
- Determines the champion when a single winner remains

---

## Getting Started

### Prerequisites

- Java 17+
- Gradle
- PostgreSQL database
- RabbitMQ instance (local or cloud)

### Run locally

1. Clone the repository:
```bash
git clone https://github.com/NicoVelasco96/Tournament-Service.git
cd Tournament-Service
```

2. Set the required environment variables (see below).

3. Run the application:
```bash
./gradlew bootRun
```

4. Access Swagger UI at:
```
http://localhost:8082/swagger-ui.html
```

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://host:port/db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `yourpassword` |
| `RABBITMQ_HOST` | RabbitMQ host | `stingray.rmq.cloudamqp.com` |
| `RABBITMQ_PORT` | RabbitMQ port (5671 for SSL) | `5671` |
| `RABBITMQ_USERNAME` | RabbitMQ username | `fnzrdgdf` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `yourpassword` |
| `RABBITMQ_VHOST` | RabbitMQ virtual host | `fnzrdgdf` |

> ⚠️ Never commit credentials to the repository. Always use environment variables.

---

## API Endpoints

Base URL: `http://localhost:8082`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/tournaments` | Create a new tournament |
| `GET` | `/api/tournaments/{id}` | Get tournament by ID |
| `GET` | `/api/tournaments?status=OPEN` | Get tournaments by status |
| `POST` | `/api/tournaments/{id}/open` | Open tournament for registration |
| `POST` | `/api/tournaments/{id}/players/{playerId}` | Register a player |
| `POST` | `/api/tournaments/{id}/start` | Start tournament and generate bracket |
| `GET` | `/api/tournaments/{id}/matches` | Get all matches for a tournament |
| `POST` | `/api/tournaments/matches/{matchId}/result` | Report a match result |

### Example: Create Tournament

```json
POST /api/tournaments
{
  "name": "Spring Invitational",
  "description": "Seasonal tournament for ranked players",
  "maxPlayers": 8,
  "organizerId": 1
}
```

### Example: Report Result

```json
POST /api/tournaments/matches/1/result
{
  "winnerId": 3
}
```

---

## Tournament Flow

```
CREATE (DRAFT)
    │
    ▼
OPEN ──► Register players
    │
    ▼
IN_PROGRESS ──► Bracket generated
    │            Matches played round by round
    │            Next round auto-generated on round completion
    ▼
FINISHED ──► Champion determined
             TOURNAMENT_FINISHED event published to RabbitMQ
```

### Tournament Statuses

| Status | Description |
|---|---|
| `DRAFT` | Tournament created, not yet open |
| `OPEN` | Accepting player registrations |
| `IN_PROGRESS` | Bracket active, matches being played |
| `FINISHED` | Champion determined |
| `CANCELLED` | Tournament cancelled |

### Match Statuses

| Status | Description |
|---|---|
| `PENDING` | Match not yet played |
| `IN_PROGRESS` | Match currently being played |
| `FINISHED` | Result reported |

---

## Project Structure

```
src/main/java/com/tournament/tournament/
├── config/
│   ├── AppConfig.java                  # OpenAPI config
│   └── RabbitMQConfig.java             # Exchange, queues and bindings
├── controller/
│   └── TournamentController.java       # REST endpoints
├── dto/
│   └── TournamentDTO.java              # Request/Response DTOs
├── messaging/
│   └── TournamentEventPublisher.java   # RabbitMQ event publisher
├── model/
│   ├── Match.java                      # Match entity
│   ├── MatchStatus.java                # Match status enum
│   ├── Tournament.java                 # Tournament entity
│   └── TournamentStatus.java           # Tournament status enum
├── repository/
│   ├── IMatchRepository.java
│   └── ITournamentRepository.java
├── service/
│   ├── BracketGeneratorService.java    # Bracket logic
│   ├── IBracketGeneratorService.java
│   ├── ITournamentService.java
│   └── TournamentService.java          # Business logic
└── TournamentServiceApplication.java
```

---

## License

This project is part of a personal portfolio. Feel free to use it as reference.
