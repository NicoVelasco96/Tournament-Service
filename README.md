# 🏆 Tournament Service

Escoge tu idioma / Choose your language:

<details>
<summary><b>🇪🇸 Español</b></summary>

## Resumen

**Tournament Service** gestiona el ciclo de vida completo de un torneo — desde la creación e inscripción de jugadores hasta la generación del bracket y la determinación del campeón. El motor de brackets soporta cualquier cantidad de jugadores rellenando automáticamente hasta la siguiente potencia de dos con slots BYE, garantizando rondas de eliminación directa limpias.

Cuando un torneo comienza o finaliza, se publican eventos de dominio a RabbitMQ para que otros servicios (como el ranking-service) puedan reaccionar en consecuencia.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Base de Datos | PostgreSQL (vía Railway) |
| ORM | Hibernate / Spring Data JPA |
| Mensajería | RabbitMQ (vía CloudAMQP) |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Gradle |
| Utilidades | Lombok, Slf4j |

---

## Arquitectura

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

### Generador de Brackets

El `BracketGeneratorService` es el núcleo de este microservicio. Se encarga de:

- Rellenar la lista de jugadores hasta la siguiente potencia de dos con slots BYE
- Aleatorizar los emparejamientos para evitar matchups predecibles
- Avanzar automáticamente a los jugadores que reciben un BYE
- Generar la siguiente ronda una vez que todos los partidos de la ronda actual finalizan
- Determinar al campeón cuando queda un único ganador

---

## Primeros Pasos

### Requisitos

- Java 17+
- Gradle
- Base de datos PostgreSQL
- Instancia de RabbitMQ (local o en la nube)

### Ejecución local

1. Clonar el repositorio:
```bash
git clone https://github.com/NicoVelasco96/Tournament-Service.git
cd Tournament-Service
```

2. Configurar las variables de entorno (ver sección correspondiente).

3. Ejecutar la aplicación:
```bash
./gradlew bootRun
```

4. Acceder a Swagger UI en:
```
http://localhost:8082/api/docs
```

---

## Variables de Entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://host:port/db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `tupassword` |
| `RABBITMQ_HOST` | Host de RabbitMQ | `stingray.rmq.cloudamqp.com` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ (5671 para SSL) | `5671` |
| `RABBITMQ_USERNAME` | Usuario de RabbitMQ | `fnzrdgdf` |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ | `tupassword` |
| `RABBITMQ_VHOST` | Virtual host de RabbitMQ | `fnzrdgdf` |
| `SERVICE_TOKEN` | Token JWT para comunicación entre servicios | `eyJ...` |
| `PLAYER_SERVICE_URL` | URL del player-service | `https://player-service.onrender.com` |

> ⚠️ Nunca subas credenciales reales al repositorio. Siempre usá variables de entorno.

---

## Endpoints

URL Base: `http://localhost:8082`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/tournaments` | Crear un nuevo torneo |
| `GET` | `/api/tournaments/{id}` | Obtener torneo por ID |
| `GET` | `/api/tournaments?status=OPEN` | Obtener torneos por estado |
| `POST` | `/api/tournaments/{id}/open` | Abrir torneo para inscripciones |
| `POST` | `/api/tournaments/{id}/players/{playerId}` | Inscribir un jugador |
| `POST` | `/api/tournaments/{id}/start` | Iniciar torneo y generar bracket |
| `GET` | `/api/tournaments/{id}/matches` | Obtener partidos del torneo |
| `GET` | `/api/tournaments/{id}/players` | Obtener jugadores con datos completos |
| `POST` | `/api/tournaments/matches/{matchId}/result` | Reportar resultado de un partido |

### Ejemplo: Crear Torneo

```json
POST /api/tournaments
{
  "name": "Spring Invitational",
  "description": "Torneo de temporada para jugadores rankeados",
  "maxPlayers": 8,
  "organizerId": 1
}
```

### Ejemplo: Reportar Resultado

```json
POST /api/tournaments/matches/1/result
{
  "winnerId": 3
}
```

---

## Flujo del Torneo

```
CREAR (DRAFT)
    │
    ▼
OPEN ──► Inscribir jugadores
    │
    ▼
IN_PROGRESS ──► Bracket generado
    │            Partidos jugados ronda a ronda
    │            Siguiente ronda generada automáticamente
    ▼
FINISHED ──► Campeón determinado
             Evento TOURNAMENT_FINISHED publicado a RabbitMQ
```

### Estados del Torneo

| Estado | Descripción |
|---|---|
| `DRAFT` | Torneo creado, aún no abierto |
| `OPEN` | Aceptando inscripciones |
| `IN_PROGRESS` | Bracket activo, partidos en curso |
| `FINISHED` | Campeón determinado |
| `CANCELLED` | Torneo cancelado |

### Estados de los Partidos

| Estado | Descripción |
|---|---|
| `PENDING` | Partido aún no jugado |
| `IN_PROGRESS` | Partido en curso |
| `FINISHED` | Resultado reportado |

---

## Estructura del Proyecto

```
src/main/java/com/tournament/tournament/
├── client/
│   └── PlayerServiceClient.java        # Cliente HTTP para player-service
├── config/
│   ├── AppConfig.java                  # Configuración OpenAPI y WebClient
│   └── RabbitMQConfig.java             # Exchange, colas y bindings
├── controller/
│   └── TournamentController.java       # Endpoints REST
├── dto/
│   └── TournamentDTO.java              # DTOs de request/response
├── messaging/
│   └── TournamentEventPublisher.java   # Publicador de eventos RabbitMQ
├── model/
│   ├── Match.java                      # Entidad partido
│   ├── MatchStatus.java                # Enum estado de partido
│   ├── Tournament.java                 # Entidad torneo
│   └── TournamentStatus.java           # Enum estado de torneo
├── repository/
│   ├── IMatchRepository.java
│   └── ITournamentRepository.java
├── service/
│   ├── BracketGeneratorService.java    # Lógica del bracket
│   ├── IBracketGeneratorService.java
│   ├── ITournamentService.java
│   └── TournamentService.java          # Lógica de negocio
└── TournamentServiceApplication.java
```

</details>

---

<details>
<summary><b>🇺🇸 English</b></summary>

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
http://localhost:8082/api/docs
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
| `SERVICE_TOKEN` | JWT token for inter-service communication | `eyJ...` |
| `PLAYER_SERVICE_URL` | Player service URL | `https://player-service.onrender.com` |

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
| `GET` | `/api/tournaments/{id}/players` | Get players with full details |
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
├── client/
│   └── PlayerServiceClient.java        # HTTP client for player-service
├── config/
│   ├── AppConfig.java                  # OpenAPI & WebClient config
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

</details>

---

## 📜 Licencia / License

Este proyecto es parte de un portafolio personal. / This project is part of a personal portfolio.
