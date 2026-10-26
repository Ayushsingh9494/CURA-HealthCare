# CURA — Testing Strategy

## Philosophy
Tests are not written to satisfy a checklist. They are written to:
1. Catch regressions when code changes.
2. Prove that business rules (especially safety-critical ones) behave correctly.
3. Serve as living documentation of what a feature is supposed to do.

Never claim a feature is complete unless its tests pass.

---

## Test Pyramid

```
         ┌──────────────────┐
         │  E2E (Selenium)  │  Few, slow, cover critical user journeys
         ├──────────────────┤
         │  Integration     │  MockMvc + Spring test slices + Testcontainers
         ├──────────────────┤
         │  Unit Tests      │  Many, fast, cover business logic in isolation
         └──────────────────┘
```

---

## Backend Testing

### 1. Unit Tests (JUnit 5 + Mockito)

**Scope**: Service classes. All external dependencies (repositories, providers) are mocked.

**Location**: `src/test/java/com/cura/{module}/service/`

**Rules**:
- Every `*Service` class must have a corresponding `*ServiceTest` class.
- Use `@ExtendWith(MockitoExtension.class)`, not `@SpringBootTest`.
- Test: happy path, not-found, authorization failure, validation edge cases.
- Name tests using the pattern: `methodName_condition_expectedResult`.

**Example**:
```java
@Test
void bookAppointment_whenSlotAlreadyBooked_throwsConflictException() {
    // Arrange
    AvailabilitySlot slot = new AvailabilitySlot();
    slot.setIsBooked(true);
    when(slotRepository.findByIdWithLock(17L)).thenReturn(Optional.of(slot));

    // Act & Assert
    assertThrows(ConflictException.class,
        () -> appointmentService.bookAppointment(request, patientId));
}
```

**Priority tests per module**:

| Module | Critical Tests |
|--------|---------------|
| Auth | Register with duplicate email, login with wrong password, expired token rejection, refresh token rotation |
| Appointment | Double-booking prevention, cancellation of completed appointment rejected, slot ownership |
| EMR | Patient reads own record, patient cannot read another's record, doctor reads own-case record |
| Triage | Every red-flag rule fires correctly, AI suppressed when red flag triggers, no red flag → AI called |
| Emergency | SOS creates with `is_simulated=true`, responder assignment changes status to BUSY |
| Prescription | Original image path unchanged after OCR, low-confidence flagging, unverified items cannot proceed |

---

### 2. Controller / Integration Tests (MockMvc + @WebMvcTest)

**Scope**: Controller layer with mocked service. Tests HTTP behaviour: status codes, request validation, auth enforcement.

**Location**: `src/test/java/com/cura/{module}/controller/`

**Rules**:
- Use `@WebMvcTest(XController.class)` + `@MockBean` for the service.
- Use Spring Security test support to set authenticated principal in tests.
- Test: `200`/`201` happy path, `400` validation errors (missing/invalid fields), `401` no token, `403` wrong role.

**Example**:
```java
@Test
@WithMockUser(roles = "PATIENT")
void bookAppointment_withValidRequest_returns201() throws Exception {
    when(appointmentService.bookAppointment(any(), anyLong()))
        .thenReturn(appointmentResponse);

    mockMvc.perform(post("/api/v1/appointments")
            .contentType(APPLICATION_JSON)
            .content("""
                { "doctorId": 3, "slotId": 17, "appointmentType": "IN_PERSON",
                  "reason": "Follow-up" }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.status").value("PENDING"));
}
```

---

### 3. Full Integration Tests (Spring Boot Test + Testcontainers)

**Scope**: Full application context + real PostgreSQL (via Testcontainers).

**Location**: `src/test/java/com/cura/integration/`

**Rules**:
- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
- Use `@Testcontainers` + `@Container` to spin up a real PostgreSQL instance.
- Flyway migrations run automatically — test DB starts clean.
- Run integration tests in a separate Maven phase (`mvn verify`), not `mvn test`.

**Priority integration tests**:
- Full auth flow: register → login → refresh → protected resource → logout.
- Appointment flow: create slot → book → attempt double-book → cancel.
- Triage flow: submit symptoms with red flag → verify EMERGENCY outcome.
- Emergency SOS flow: POST SOS → verify responder assigned → verify status updates.

---

### 4. API / Security Tests

**Goal**: Prove that authorization rules are enforced at every endpoint, not just assumed.

**Tests to include for every module**:
```
[ ] Unauthenticated request → 401
[ ] Authenticated but wrong role → 403
[ ] Authenticated, correct role, wrong resource ownership → 403
[ ] Authenticated, correct role, correct ownership → 200/201
```

---

## Frontend Testing

### Component Tests (Vitest + React Testing Library)
**Location**: `frontend/src/__tests__/`

**Priority tests**:
- `LoginPage`: form submission, error display, redirect on success.
- `ProtectedRoute`: redirects unauthenticated user.
- `TriageWizardPage`: red-flag warning shown when severity is EMERGENCY.
- `PrescriptionViewer`: flagged items are visually highlighted.
- `EmergencyBanner`: disclaimer text is present and readable.

### API Client Tests (MSW — Mock Service Worker)
Mock the backend and test that the API client handles errors correctly (401 → token refresh, 403 → redirect to error page, 500 → toast notification).

---

## End-to-End Tests (Selenium WebDriver)

**Location**: `e2e/`

**Scope**: Critical user journeys that prove the whole system works together.

**Priority E2E Tests**:

| ID | Journey |
|----|---------|
| E2E-01 | Patient: Register → Login → View Doctor List → Book Appointment → View Booking |
| E2E-02 | Patient: Login → Submit Triage Symptoms → View Result with Severity |
| E2E-03 | Patient: Login → Upload Prescription → Trigger OCR → View Extracted Items |
| E2E-04 | Doctor: Login → View Appointments → Add Notes → Verify Prescription |
| E2E-05 | Patient: Login → Trigger SOS → View Emergency Status with Disclaimer |
| E2E-06 | Admin: Login → View Audit Logs |

**Tooling**: Selenium WebDriver (ChromeDriver), ChromeDriver managed via WebDriverManager, JUnit 5 for test runner.

---

## Test Profiles

| Profile | Database | External Services | Use Case |
|---------|----------|------------------|----------|
| `test` | H2 in-memory or Testcontainers PG | All mocked | Unit + MockMvc tests |
| `integration` | Testcontainers PostgreSQL | All mocked | Full integration tests |
| `e2e` | Local Docker PG | Mocks + real Gemini (optional) | Selenium E2E tests |
| `default` (dev) | Local Docker PG | Real Gemini | Developer runtime |

**`application-test.yml`** switches:
```yaml
cura:
  ai:
    provider: mock      # activates MockTriageProvider, MockOcrProvider
  notification:
    provider: mock      # activates MockNotificationProvider
  file-storage:
    provider: local
```

---

## What We Do NOT Test
- Gemini API internal behaviour — we test that our code calls the interface correctly.
- PostgreSQL internals — Testcontainers gives us a real DB to run migrations against.
- Production deployment environment — documented separately in README.

---

## Running Tests

```bash
# Backend unit tests only (fast, no DB required)
cd backend && mvn test

# Backend integration tests (requires Docker for Testcontainers)
cd backend && mvn verify -Pintegration

# Frontend component tests
cd frontend && npm run test

# E2E tests (requires full stack running)
cd e2e && mvn test -Pe2e
```

---

## Coverage Goals (per module, at capstone demo)

| Module | Unit Test Coverage | Integration Test | E2E Coverage |
|--------|:-----------------:|:----------------:|:------------:|
| Auth | ≥ 90% | ✓ | ✓ (E2E-01) |
| Appointment | ≥ 85% | ✓ | ✓ (E2E-01) |
| EMR | ≥ 80% | ✓ | ✓ (E2E-04) |
| Triage | ≥ 95% | ✓ | ✓ (E2E-02) |
| Emergency | ≥ 85% | ✓ | ✓ (E2E-05) |
| Prescription | ≥ 80% | ✓ | ✓ (E2E-03) |

*Coverage is measured by JaCoCo and reported after `mvn verify`.*
