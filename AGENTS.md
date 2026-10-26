# CURA — Agent Instructions

## Project Identity
CURA is a B.Tech CSE capstone project — an interoperable telehealth, AI-assisted clinical triage,
electronic medical record, prescription-processing, and emergency first-responder platform.

This is an **educational prototype**. It uses synthetic data and simulated integrations.
It must never be presented as a validated clinical system.

---

## Non-Negotiable Rules for Every Agent

### Technology Stack
- **Backend**: Java 24, Spring Boot 3.x, Spring Web, Spring Security (JWT), Spring Data JPA,
  Hibernate, Bean Validation, Flyway, Maven, OpenAPI/Swagger.
- **Frontend**: React 18+, TypeScript, Vite, Tailwind CSS, React Router v6.
- **Database**: PostgreSQL 16+; PostGIS activated for the emergency module.
- **AI**: Gemini API behind a replaceable provider interface. Never hardcode provider logic.
- **Testing**: JUnit 5, Mockito, Spring Boot test slice annotations, MockMvc, Selenium WebDriver.
- **DevOps**: Docker, Docker Compose, `.env` for secrets (never hardcoded).

Do **not** switch to FastAPI, Node/Express, Next.js, or microservices without owner approval.
Do **not** introduce Redis, Kafka, or Kubernetes without a demonstrated requirement and approval.

### Before Making Any Change
1. Read the relevant section of `docs/ARCHITECTURE.md`.
2. Check `docs/DECISION_LOG.md` for prior decisions on that topic.
3. Check `docs/PROGRESS.md` to understand what is already implemented.
4. Never overwrite or delete existing working code without explicit instruction.

### Code Quality
- Organize by **domain/feature**, not by layer. Package root: `com.cura`.
- Keep controllers thin — delegate all logic to services.
- Validate at the controller boundary using Bean Validation annotations.
- Use `@Transactional` at the service layer, not controller or repository.
- Never expose internal entity objects in REST responses — use DTOs.
- Every public service method must have a Javadoc comment.
- Use `record` types for immutable DTOs where appropriate (Java 24 supports this).

### Security
- Every protected endpoint must require authentication via Spring Security.
- Role-based access (`PATIENT`, `DOCTOR`, `ADMIN`, `RESPONDER`) using `@PreAuthorize`.
- Never log PHI (personally identifiable health information).
- Never hardcode secrets. Read from environment variables / `application.yml` property references.
- Never expose stack traces in API responses — return structured error DTOs.

### Database / Migrations
- Every schema change **must** have a Flyway migration script in
  `src/main/resources/db/migration/` following the naming convention `V{n}__{description}.sql`.
- Never use `spring.jpa.hibernate.ddl-auto=create` or `update` in production config.
- Add appropriate indexes on foreign keys and frequently-queried columns.

### Testing
- Any new service class must have a corresponding unit test.
- Any new controller endpoint must have a MockMvc integration test.
- Never claim a feature is "complete" unless tests pass and are committed.

### Healthcare Safety
- Emergency red-flag rules must be **deterministic** — no AI involvement in that path.
- Every AI-generated triage output must carry the disclaimer:
  `"This is AI-assisted guidance and NOT a validated medical diagnosis."`
- Never silently modify a prescription or medication entry.
- Distinguish simulated emergency operations from real ones with `is_simulated = true`.
- Preserve all original prescription images; never replace them.

### Documentation
After each milestone, update:
- `docs/PROGRESS.md` (what changed, what status)
- `docs/DECISION_LOG.md` (any new decisions made)
- `docs/LEARNING_LOG.md` (concepts introduced)
- Relevant section of `docs/API_CONTRACTS.md` if endpoints changed

---

## Feature Status Convention
Use exactly these statuses when reporting:
- **Implemented and tested**
- **Implemented but not fully tested**
- **Partially implemented**
- **Mocked or simulated**
- **Planned**
- **Blocked** — state the blocker explicitly

---

## Commit Convention
Format: `[MODULE] short imperative description`

Examples:
- `[AUTH] Add JWT refresh token rotation`
- `[TRIAGE] Implement deterministic red-flag rule engine`
- `[EMR] Add Flyway migration V5 for record attachments`

---

## Owner
Ayush Singh — B.Tech CSE capstone student.
Approval required before starting any new milestone or making architectural changes.
