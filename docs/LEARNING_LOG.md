# CURA — Learning Log

This log tracks concepts introduced at each milestone, viva questions, exercises,
and data-flow walkthroughs. Use it to study before your capstone viva.

---

## M0 — Architecture & Design Thinking

### Five Key Concepts

1. **Modular Monolith vs. Microservices**
   A modular monolith is a single deployable application with strong internal domain boundaries.
   Unlike microservices, there is no network overhead between domains, no distributed transaction
   complexity, and no separate deployment pipelines. For a capstone project, it lets you demonstrate
   clean architecture without operational overhead. The domain boundaries you set now make it
   straightforward to extract a service later if needed.

2. **Domain-Driven Package Structure**
   Organizing by feature (`com.cura.appointment`) rather than layer (`com.cura.service`) keeps
   related code co-located. When you need to change the appointment booking logic, you touch files
   in one package — not scattered across four. This is a well-established Java convention often
   called "packaging by feature."

3. **Provider / Interface Pattern for External Services**
   External dependencies (AI, notifications, OCR) are always accessed through a Java interface.
   Your service code calls `OcrProvider.extract(imageBytes)`, not `GeminiOcrProvider.extract(...)`.
   This means:
   - Unit tests use a `MockOcrProvider` — no real API calls, no cost, deterministic results.
   - You can swap Gemini for Google Cloud Vision by adding a new implementation class.
   - Spring selects the right implementation via `@Profile`.

4. **Why DTOs Must Separate the API from the Database**
   JPA entities are tied to the database schema. If you return them directly from a controller,
   you expose internal structure, risk lazy-loading errors (Jackson serializes a Hibernate proxy),
   and make it hard to change the schema without breaking the API. DTOs are the API contract.
   Entities are the storage contract. They evolve independently.

5. **JWT: Stateless Authentication**
   A JSON Web Token contains a self-describing claim set (user ID, role, expiry) signed with
   a secret key. The server does not store session state — it just validates the signature.
   This works well for a React SPA (token stored in memory, sent as `Authorization: Bearer ...`).
   The short expiry (15 min) limits damage if a token is stolen. Refresh tokens allow silent
   renewal without re-login.

---

### Three Viva-Style Questions

1. *Why did you choose a modular monolith instead of microservices for CURA?*
2. *How does the `OcrProvider` interface make your prescription module more testable?*
3. *What is the difference between an entity and a DTO, and why does it matter for security?*

---

### Code-Reading Exercise

Open [`docs/ARCHITECTURE.md`](ARCHITECTURE.md) and locate the **Layer Responsibilities** section.

**Exercise**: For the `AppointmentService.bookAppointment()` method (which you will implement in M3):
- Which layer validates the incoming JSON request?
- Which layer checks if the slot is already booked?
- Which layer performs the database write?
- Which layer constructs the HTTP response?

Write your answers before asking for the solution.

---

### Data-Flow Walkthrough: Patient books an appointment

```
1. Patient clicks "Book" in React UI
   → appointmentApi.book(doctorId, slotId, reason) [src/api/appointments.ts]
   → Axios sends POST /api/v1/appointments with JWT in Authorization header

2. Request reaches Spring Boot
   → JwtAuthenticationFilter validates token, sets SecurityContext
   → Dispatcher maps to AppointmentController.bookAppointment()

3. Controller layer
   → @Valid on BookAppointmentRequest: checks @NotNull, @NotBlank
   → If validation fails: GlobalExceptionHandler returns 400 with field errors
   → Calls appointmentService.bookAppointment(request, currentUserId)

4. Service layer (AppointmentService)
   → @Transactional begins
   → slotRepository.findByIdWithLock(slotId)  ← SELECT ... FOR UPDATE prevents race condition
   → If slot.isBooked() → throw ConflictException → transaction rolls back → 409 response
   → slot.setIsBooked(true), slot saved
   → new Appointment(...) created and saved
   → AuditAspect fires (Spring AOP) → audit_logs row written
   → @Transactional commits
   → Returns AppointmentResponse DTO

5. Controller maps DTO to ApiResponse<AppointmentResponse> → 201 Created

6. React receives 201 → navigates to "My Appointments" page
   → useAppointments hook refetches the list (React Query cache invalidation)
```

---

## M1 — Foundation

### Key Concepts
1. **Flyway Migrations**: Flyway enforces database schema versioning by tracking executed SQL scripts in `flyway_schema_history`. This eliminates drift across developer environments.
2. **Spring Boot Actuator**: Provides production-ready operational endpoints (`/actuator/health`, `/actuator/info`) without writing custom health check code.
3. **Stateless Security**: CSRF protection is disabled for REST APIs without browser session cookies, and session management is set to `STATELESS`.

---

## M2 — Authentication & JWT Security

### Key Concepts
1. **JWT Architecture & Claims**: Access tokens (15-min lifetime) carry user identity (`sub`), email, and role, cryptographically signed with HMAC-SHA256 (`Jwts.SIG.HS256`).
2. **Refresh Token Rotation**: Refresh tokens (7-day lifetime) are stored in the database. When exchanged, the old refresh token is revoked and a new pair is issued, preventing replay attacks.
3. **OncePerRequestFilter**: `JwtAuthenticationFilter` intercepts requests exactly once, extracts Bearer token, validates it, and injects `UserPrincipal` into Spring's `SecurityContextHolder`.
4. **Zustand Auth Store**: Lightweight state container persisting authentication tokens and user profile without Redux boilerplate.

### Viva-Style Questions
1. *Why do we use short-lived access tokens with database-backed refresh tokens instead of long-lived access tokens?*
2. *Where does `JwtAuthenticationFilter` sit in the Spring Security filter chain, and why before `UsernamePasswordAuthenticationFilter`?*
3. *How does refresh token rotation protect against stolen refresh tokens?*

---

*This log is updated after each milestone. Answers to exercises are not written until you attempt them.*
