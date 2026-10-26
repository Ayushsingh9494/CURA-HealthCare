# CURA — Telehealth, EMR & Emergency Platform

> **Educational Prototype** — B.Tech CSE Capstone Project.
> Uses synthetic data and simulated integrations.
> Not a validated clinical system. Do not use for real patient care.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + TypeScript + Vite + Tailwind CSS |
| Backend | Java 21 + Spring Boot 3.3.5 |
| Database | PostgreSQL 16 + PostGIS 3.4 |
| Auth | Spring Security + JWT |
| Migrations | Flyway |
| API Docs | Swagger UI |

---

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 18+ | `node -version` |
| Docker Desktop | Latest | `docker -v` |

---

## Local Setup (5 minutes)

### 1 — Clone and configure
```bash
git clone <your-repo-url> CURA
cd CURA

# Copy environment template
cp .env.example .env
# Edit .env if needed (defaults work for local dev)
```

### 2 — Start the database
```bash
docker compose up -d
# Wait for the health check: docker compose ps
```

### 3 — Start the backend
```bash
cd backend

# Windows CMD:
set DB_URL=jdbc:postgresql://localhost:5432/cura
set DB_USERNAME=cura
set DB_PASSWORD=cura
set CORS_ALLOWED_ORIGINS=http://localhost:5173

mvn spring-boot:run

# OR use PowerShell:
# $env:DB_URL="jdbc:postgresql://localhost:5432/cura"; mvn spring-boot:run
```

Backend starts on **http://localhost:8080**

Verify:
- Health: http://localhost:8080/actuator/health
- Status: http://localhost:8080/api/v1/status
- Swagger: http://localhost:8080/swagger-ui.html

### 4 — Start the frontend
```bash
# In a new terminal
cd frontend
npm run dev
```

Frontend starts on **http://localhost:5173**

You should see the CURA landing page with a green "Connected" status.

---

## Running Tests

```bash
# Backend — unit + MockMvc tests (fast, no Docker needed)
cd backend
mvn test

# Frontend — component tests
cd frontend
npm run test     # (added in M5+)
```

---

## Project Structure

```
CURA/
├── backend/                    Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/cura/ Domain modules (auth, user, doctor, ...)
│       └── main/resources/
│           ├── application.yml
│           └── db/migration/   Flyway SQL migrations
├── frontend/                   React + Vite application
│   ├── src/
│   │   ├── api/                Axios API modules
│   │   ├── components/         Reusable UI components
│   │   ├── pages/              Route-level page components
│   │   ├── hooks/              Custom React hooks
│   │   ├── store/              Zustand state stores
│   │   └── types/              TypeScript type definitions
│   └── vite.config.ts
├── docker-compose.yml          PostgreSQL + PostGIS
├── .env.example                Environment variable template
└── docs/                       Architecture, API contracts, decisions
```

---

## API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/api-docs

---

## Milestones

| # | Milestone | Status |
|---|-----------|--------|
| M1 | Foundation — connectivity, health, Docker | ✅ Implemented |
| M2 | Authentication — JWT, roles, profiles | ✅ Implemented |
| M3 | Doctor directory & appointments | 🔄 Next |
| M4 | Medical records & file uploads | ⏳ Planned |
| M5 | Prescriptions & OCR | ⏳ Planned |
| M6 | Medication reminders | ⏳ Planned |
| M7 | Triage (deterministic rules + AI) | ⏳ Planned |
| M8 | Emergency SOS + PostGIS dispatch | ⏳ Planned |
| M9 | Video consultations (WebRTC) | ⏳ Planned |
| M10 | Integration, security review, demo | ⏳ Planned |

---

## Documentation

| Document | Description |
|----------|-------------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design, package structure |
| [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) | Tables, migrations, ER relationships |
| [docs/API_CONTRACTS.md](docs/API_CONTRACTS.md) | All endpoints, request/response shapes |
| [docs/IMPLEMENTATION_ROADMAP.md](docs/IMPLEMENTATION_ROADMAP.md) | Milestone acceptance criteria |
| [docs/DECISION_LOG.md](docs/DECISION_LOG.md) | Architectural decisions and rationale |
| [docs/PROGRESS.md](docs/PROGRESS.md) | Current implementation status |
| [docs/TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | Test pyramid and coverage targets |
| [docs/LEARNING_LOG.md](docs/LEARNING_LOG.md) | Concepts, viva questions, exercises |

---

## Security Notes

- Never commit `.env` — only `.env.example` with placeholders belongs in Git.
- Prescription images and patient records are for synthetic data only.
- All emergency operations are simulated (`is_simulated = true` by default).
- AI triage guidance carries a mandatory disclaimer — it is not a diagnosis.
