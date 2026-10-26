# CURA — Implementation Roadmap

## Milestones at a Glance

| # | Milestone | Key Deliverable | Status |
|---|-----------|----------------|--------|
| M1 | Foundation | Spring Boot + React + PostgreSQL connected, health endpoint, Docker Compose | Planned |
| M2 | Authentication | JWT auth, roles, Spring Security, user profiles | Planned |
| M3 | Doctor & Appointments | Doctor directory, slots, booking, conflict prevention | Planned |
| M4 | Medical Records | EMR creation, file uploads, access controls, audit logging | Planned |
| M5 | Prescriptions & OCR | Image upload, Gemini OCR, structured extraction, verification workflow | Planned |
| M6 | Medication Reminders | Reminder CRUD, scheduling, mock notification delivery | Planned |
| M7 | Triage | Symptom intake, deterministic red-flag rules, optional AI guidance | Planned |
| M8 | Emergency SOS | SOS endpoint, PostGIS responder proximity, assignment, simulated dispatch | Planned |
| M9 | Video Consultations | WebRTC room creation, appointment-linked video sessions | Planned |
| M10 | Integration & Demo | E2E tests, security review, Docker deployment, capstone documentation | Planned |

---

## Milestone 1 — Foundation
**Goal**: Every team member can run the full stack locally with a single command.

### Acceptance Criteria
- [ ] `docker compose up` starts PostgreSQL, the Spring Boot backend, and serves the frontend.
- [ ] `GET /api/v1/actuator/health` returns `{ "status": "UP" }`.
- [ ] React frontend loads at `http://localhost:5173` and calls the health endpoint successfully.
- [ ] CORS is configured — browser does not block frontend → backend calls.
- [ ] Flyway runs `V1__create_users.sql` on startup without errors.
- [ ] `.env.example` exists with all required variable names and placeholder values.
- [ ] `README.md` explains how to start the project from scratch.

### Files to Create
```
CURA/
├── backend/           (Spring Boot Maven project)
├── frontend/          (Vite + React + TS + Tailwind)
├── docker-compose.yml
├── .env.example
└── README.md
```

### Key Learning Concepts
1. How Vite proxies `/api` to Spring Boot in development.
2. What `@SpringBootApplication` scans and configures.
3. How Docker Compose networks containers together.
4. How Flyway knows which migrations to run (version tracking via `flyway_schema_history`).
5. Spring Boot Actuator and health endpoints.

---

## Milestone 2 — Authentication & Roles
**Goal**: Secure, role-based authentication using JWT.

### Acceptance Criteria
- [ ] `POST /api/v1/auth/register` creates a user with hashed password (BCrypt).
- [ ] `POST /api/v1/auth/login` returns access token (15 min) + refresh token (7 days).
- [ ] `POST /api/v1/auth/refresh` rotates refresh token and issues new access token.
- [ ] `POST /api/v1/auth/logout` revokes the refresh token.
- [ ] Protected endpoints return `401` without a valid JWT.
- [ ] Role-restricted endpoints return `403` for incorrect role.
- [ ] `GET /api/v1/auth/me` returns current user info.
- [ ] Password is never returned in any response.
- [ ] Frontend: Login/Register pages functional; JWT stored in memory (not localStorage).
- [ ] Frontend: `ProtectedRoute` redirects unauthenticated users to `/login`.
- [ ] JUnit tests for `AuthService` (register, login, refresh, logout).
- [ ] MockMvc tests for `AuthController` (happy path + validation errors).
- [ ] Flyway migration `V1` completed (users, refresh_tokens tables).

### Key Learning Concepts
1. JWT structure (header.payload.signature) and how Spring Security validates it.
2. BCrypt password hashing — why rainbow tables don't work against it.
3. Spring Security filter chain — where JWT validation hooks in.
4. Refresh token rotation — why it's more secure than long-lived access tokens.
5. `@PreAuthorize` and Spring method security.

---

## Milestone 3 — Doctor Directory & Appointments
**Goal**: Patients can discover doctors and book appointments without conflicts.

### Acceptance Criteria
- [ ] `GET /api/v1/doctors` supports filtering by specialization, hospital, video support.
- [ ] `GET /api/v1/doctors/{id}/slots` returns available (unbooked) future slots.
- [ ] `POST /api/v1/appointments` books a slot atomically — concurrent requests cannot double-book.
- [ ] Booking a slot marks it `is_booked = true` in the same transaction.
- [ ] `PUT /api/v1/appointments/{id}/cancel` cancels and frees the slot.
- [ ] A doctor cannot have two slots overlapping in time.
- [ ] Frontend: Doctor list, doctor detail, slot selection, and booking confirmation pages.
- [ ] Tests: conflict scenario (two concurrent bookings for same slot).
- [ ] Flyway migrations `V2`, `V3` completed.

### Key Learning Concepts
1. Database-level locking (`SELECT ... FOR UPDATE`) vs. optimistic locking for conflict prevention.
2. `@Transactional` propagation and isolation levels.
3. Spring Data JPA `@Query` with JPQL for availability filtering.
4. Paginated API responses with Spring Data `Pageable`.

---

## Milestone 4 — Electronic Medical Records
**Goal**: Doctors can create records; patients can view their own; uploads are stored safely.

### Acceptance Criteria
- [ ] `POST /api/v1/records` creates a medical record linked to an appointment.
- [ ] Patients can only read their own records.
- [ ] Doctors can only create/update records for their own patients.
- [ ] File upload (`multipart/form-data`) stores file on disk with a UUID filename.
- [ ] Original filename is preserved in `record_attachments`.
- [ ] Download endpoint streams file with correct `Content-Type`.
- [ ] Audit log entry created on every record read and write.
- [ ] `is_confidential` flag prevents access by unauthorized doctors.
- [ ] Tests: access control (patient A cannot read patient B's record).
- [ ] Flyway migrations `V4` completed.

---

## Milestone 5 — Prescriptions & OCR
**Goal**: Prescription images are uploaded, AI-extracted, human-verified, and never silently mutated.

### Acceptance Criteria
- [ ] Prescription image uploaded and stored; `original_image_path` is immutable after save.
- [ ] `POST /api/v1/prescriptions/{id}/ocr` calls `OcrProvider` (Gemini or Mock based on profile).
- [ ] OCR result populates `prescription_items` with `is_confidence_flagged = true` where confidence is low.
- [ ] Low-confidence items are surfaced to the verifying doctor in the UI.
- [ ] `PUT /api/v1/prescriptions/{id}/verify` sets `verification_status` to `VERIFIED` or `FLAGGED`.
- [ ] A prescription item cannot be auto-committed without a doctor's explicit verification action.
- [ ] Generic name lookup populates `generic_name` on each item.
- [ ] `MockOcrProvider` returns predictable test data; selected via `@Profile("test")`.
- [ ] Tests: OCR failure path, low-confidence flagging, verification workflow.
- [ ] Flyway migration `V5` completed.

---

## Milestone 6 — Medication Reminders
**Goal**: Patients can set medication reminders; a mock notifier fires and logs them.

### Acceptance Criteria
- [ ] Reminder CRUD endpoints functional.
- [ ] Reminders can be linked to a `prescription_item_id` or standalone.
- [ ] Spring `@Scheduled` job runs every minute and checks for due reminders.
- [ ] `MockNotificationProvider` logs reminder delivery; real provider is a drop-in replacement.
- [ ] Deactivated or expired reminders are not triggered.
- [ ] Tests: scheduler unit test with mock clock, notification provider mock.
- [ ] Flyway migration `V6` completed.

---

## Milestone 7 — Triage
**Goal**: Red-flag detection is deterministic and cannot be overridden by AI; AI guidance is clearly labelled.

### Acceptance Criteria
- [ ] `RedFlagRuleEngine` evaluates submitted symptoms against coded rules.
- [ ] If any red flag triggers, `recommendation = EMERGENCY`, AI guidance is suppressed.
- [ ] AI guidance is only requested when no red flags trigger AND `requestAiGuidance = true`.
- [ ] Every AI response includes `aiDisclaimer` in the DB record and API response.
- [ ] `MockTriageProvider` returns predictable responses; selected via profile.
- [ ] Minimum 5 coded red-flag rules implemented (chest pain, stroke symptoms, breathing difficulty, severe bleeding, loss of consciousness).
- [ ] Tests: red-flag rules (unit), AI suppression when red flag fires, triage controller (MockMvc).
- [ ] Flyway migration `V7` completed.

---

## Milestone 8 — Emergency SOS
**Goal**: SOS is logged, nearest available responders are found via PostGIS, and assignment is simulated.

### Acceptance Criteria
- [ ] `POST /api/v1/emergency/sos` creates `emergency_requests` with `is_simulated = true`.
- [ ] Every SOS response includes a disclaimer that no real emergency service has been contacted.
- [ ] `ResponderAssignmentService` queries PostGIS `ST_DWithin` to find responders within 10 km.
- [ ] Nearest available responder is assigned; their status changes to `BUSY`.
- [ ] Patient can update location; responder can update assignment status.
- [ ] `MockNotificationProvider` logs simulated dispatch notification.
- [ ] Tests: proximity query with test data at known coordinates.
- [ ] Flyway migration `V8` completed.

---

## Milestone 9 — Video Consultations (Deferred)
**Goal**: Patients and doctors can join a WebRTC session for a confirmed video appointment.

> **Decision required**: See `DECISION_LOG.md` — signalling server approach (simple-peer vs mediasoup).

### Acceptance Criteria (TBD)
- [ ] Confirmed `VIDEO` appointment generates a `video_room_id`.
- [ ] Frontend opens WebRTC session using the room ID.
- [ ] Session is torn down on appointment completion.

---

## Milestone 10 — Integration, Security Review & Demo
**Goal**: Project is demonstrable, tested end-to-end, and documented for the capstone viva.

### Acceptance Criteria
- [ ] Selenium WebDriver E2E tests cover: Register → Login → Book Appointment → View Record → Submit Triage.
- [ ] Spring Boot integration tests cover all critical service paths.
- [ ] API auth/authorization tests: attempt 403 scenarios for every major endpoint.
- [ ] No hardcoded secrets in the codebase (`git grep` clean).
- [ ] Docker Compose production build works.
- [ ] Swagger UI documents all endpoints correctly.
- [ ] `docs/PROGRESS.md` reflects final milestone statuses.
- [ ] `README.md` gives a 5-minute local-setup guide.
- [ ] Demo script prepared for capstone viva.

---

## Dependency Order

```
M1 (foundation) → M2 (auth) → M3 (doctors/appointments)
                            → M4 (EMR)
                                     → M5 (prescriptions)
                                              → M6 (reminders)
                            → M7 (triage)
                            → M8 (emergency)  [PostGIS from M2 infra]
                 → M9 (video) [requires M3 appointment types]
M10 (integration) depends on all prior milestones
```
