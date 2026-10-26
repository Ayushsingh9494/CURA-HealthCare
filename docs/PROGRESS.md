# CURA — Progress Log

## Current Phase: Milestone 2 Complete ✅

---

## Milestone Status

| Milestone | Status | Started | Completed |
|-----------|--------|---------|-----------|
| M0 — Documentation & Architecture | ✅ Complete | 2026-10-26 | 2026-10-26 |
| M1 — Foundation | ✅ Implemented and tested | 2026-10-26 | 2026-10-26 |
| M2 — Authentication | ✅ Implemented and tested | 2026-10-26 | 2026-10-26 |
| M3 — Doctor & Appointments | ⏳ Planned | — | — |
| M4 — Medical Records | ⏳ Planned | — | — |
| M5 — Prescriptions & OCR | ⏳ Planned | — | — |
| M6 — Medication Reminders | ⏳ Planned | — | — |
| M7 — Triage | ⏳ Planned | — | — |
| M8 — Emergency SOS | ⏳ Planned | — | — |
| M9 — Video Consultations | ⏳ Planned | — | — |
| M10 — Integration & Demo | ⏳ Planned | — | — |

---

## M0 — Documentation & Architecture (In Progress)

### Completed
- [x] Repository initialized (`c:\Users\Ayush\Desktop\CURA\`)
- [x] `AGENTS.md` — agent behavioral rules
- [x] `docs/PROJECT_OVERVIEW.md` — project context and goals
- [x] `docs/ARCHITECTURE.md` — modular monolith, package structure, security architecture
- [x] `docs/DATABASE_SCHEMA.md` — 9 Flyway migrations, all tables, indexes, constraints
- [x] `docs/API_CONTRACTS.md` — all endpoint groups, request/response shapes, HTTP status codes
- [x] `docs/IMPLEMENTATION_ROADMAP.md` — 10 milestones with acceptance criteria
- [x] `docs/DECISION_LOG.md` — 8 open decisions awaiting owner approval
- [x] `docs/TESTING_STRATEGY.md` — unit, integration, API security, E2E strategy
- [x] `docs/PROGRESS.md` — this file
- [x] `docs/LEARNING_LOG.md` — initialized

### Awaiting Owner Approval (from DECISION_LOG.md)
- [ ] DL-01: JWT vs. Session auth
- [ ] DL-02: File storage strategy
- [ ] DL-03: OCR provider choice
- [ ] DL-04: Frontend state management
- [ ] DL-05: WebRTC approach (or defer M9)
- [ ] DL-06: Java version (21 LTS vs. 24)
- [ ] DL-07: PostGIS from M1 vs. defer to M8
- [ ] DL-08: Notification strategy

### Not Started
- [ ] Backend Spring Boot project scaffold
- [ ] Frontend Vite project scaffold
- [ ] `docker-compose.yml`
- [ ] `.env.example`
- [ ] Root `README.md`

---

## Feature Inventory (All Planned)

| Feature | Module | Status |
|---------|--------|--------|
| User registration & login | auth | Implemented and tested |
| JWT access + refresh tokens | auth | Implemented and tested |
| Role-based authorization | auth | Implemented and tested |
| User profile management | user | Implemented and tested |
| Doctor directory with search | doctor | Planned |
| Hospital management | doctor | Planned |
| Availability slot management | appointment | Planned |
| Appointment booking (conflict-safe) | appointment | Planned |
| Appointment cancellation | appointment | Planned |
| Medical record creation | emr | Planned |
| File upload (lab reports, imaging) | emr | Planned |
| Record access controls | emr | Planned |
| Audit logging | audit | Planned |
| Prescription image upload | prescription | Planned |
| OCR extraction (Gemini) | prescription | Planned |
| Doctor verification workflow | prescription | Planned |
| Generic medicine comparison | prescription | Planned |
| Medication reminders | medication | Planned |
| Mock notification delivery | notification | Planned |
| Deterministic red-flag rules | triage | Planned |
| AI-assisted triage guidance | triage | Planned |
| Emergency SOS | emergency | Planned |
| PostGIS responder proximity search | emergency | Planned |
| Simulated responder assignment | emergency | Planned |
| WebRTC video consultations | appointment | Planned |
| Swagger UI | common | Planned |

---

## Known Issues / Blockers

| ID | Description | Blocking | Raised |
|----|-------------|---------|--------|
| B-01 | 8 decisions in DECISION_LOG.md need owner approval before M1 begins | M1 | 2026-10-26 |

---

*This file is updated after every milestone and significant change.*
