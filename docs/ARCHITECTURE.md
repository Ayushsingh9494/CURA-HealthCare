# CURA — Architecture Document

## Architectural Style: Modular Monolith

CURA uses a **modular monolith**: a single deployable Spring Boot application internally
partitioned into cohesive domain modules with strict boundaries. This allows clean separation
of concerns without the operational overhead of microservices, and makes it straightforward
to demonstrate, test, and defend in a capstone viva.

Migration to microservices is explicitly deferred — it requires owner approval and a
demonstrated need.

---

## High-Level Request/Data Flow

```
Browser (React + Vite)
        │
        │  HTTP/HTTPS (JSON REST)
        │  WebSocket (live updates, future)
        │  WebRTC (video, Milestone 9)
        ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot Application                        │
│                                                                 │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌───────────┐   │
│  │  Auth    │   │  Doctor  │   │  Appt.   │   │   EMR     │   │
│  │  Module  │   │  Module  │   │  Module  │   │  Module   │   │
│  └──────────┘   └──────────┘   └──────────┘   └───────────┘   │
│                                                                 │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌───────────┐   │
│  │  Presc.  │   │  Triage  │   │Emergency │   │  Notif.   │   │
│  │  Module  │   │  Module  │   │  Module  │   │  Module   │   │
│  └──────────┘   └──────────┘   └──────────┘   └───────────┘   │
│                                                                 │
│  ┌──────────────────┐   ┌──────────────────────────────────┐   │
│  │  Audit Module    │   │  Common (exceptions, security,   │   │
│  │                  │   │  OpenAPI config, CORS, DTOs)     │   │
│  └──────────────────┘   └──────────────────────────────────┘   │
│                                                                 │
│  Spring Security filter chain ─ JWT validation on every req    │
└─────────────────────────────────────────────────────────────────┘
        │                          │                    │
        ▼                          ▼                    ▼
 PostgreSQL + PostGIS        Gemini API          File System
 (primary data store)    (OCR + Triage AI,      (prescription
                          behind interface)      images, reports)
```

---

## Layer Responsibilities Within Each Module

```
Controller    Receives HTTP request, validates input (Bean Validation),
              delegates to service, maps response DTO. Never contains business logic.

Service       Owns business logic, orchestrates repositories, manages transactions
              (@Transactional), calls external providers. Throws typed exceptions.

Repository    Spring Data JPA interface. Custom queries via @Query (JPQL or native SQL).
              Never called directly from controllers.

Entity        JPA-mapped table. Never returned from REST controllers.

DTO           Plain Java record or class for request/response. Keeps API shape
              independent of database schema.

Provider      Interface for external services (AI, notifications, OCR, video).
              Production and mock implementations are interchangeable.
```

---

## Backend Package Structure

```
com.cura
├── CuraApplication.java
│
├── auth/
│   ├── controller/    AuthController.java
│   ├── service/       AuthService.java
│   ├── security/      JwtTokenProvider.java  JwtAuthenticationFilter.java
│   ├── entity/        RefreshToken.java
│   └── dto/           LoginRequest.java  RegisterRequest.java  TokenResponse.java
│
├── user/
│   ├── controller/    UserController.java
│   ├── service/       UserService.java
│   ├── repository/    UserRepository.java  UserProfileRepository.java
│   ├── entity/        User.java  UserProfile.java
│   └── dto/           UserProfileRequest.java  UserProfileResponse.java
│
├── doctor/
│   ├── controller/    DoctorController.java
│   ├── service/       DoctorService.java
│   ├── repository/    DoctorRepository.java  HospitalRepository.java
│   ├── entity/        Doctor.java  Hospital.java
│   └── dto/
│
├── appointment/
│   ├── controller/    AppointmentController.java
│   ├── service/       AppointmentService.java  SlotService.java
│   ├── repository/    AppointmentRepository.java  AvailabilitySlotRepository.java
│   ├── entity/        Appointment.java  AvailabilitySlot.java
│   └── dto/
│
├── emr/
│   ├── controller/    MedicalRecordController.java
│   ├── service/       MedicalRecordService.java  AttachmentService.java
│   ├── repository/    MedicalRecordRepository.java  RecordAttachmentRepository.java
│   ├── entity/        MedicalRecord.java  RecordAttachment.java
│   └── dto/
│
├── prescription/
│   ├── controller/    PrescriptionController.java
│   ├── service/       PrescriptionService.java
│   ├── repository/    PrescriptionRepository.java  PrescriptionItemRepository.java
│   ├── entity/        Prescription.java  PrescriptionItem.java
│   ├── ocr/           OcrProvider.java (interface)
│   │                  GeminiOcrProvider.java
│   │                  MockOcrProvider.java
│   └── dto/
│
├── medication/
│   ├── controller/    MedicationReminderController.java
│   ├── service/       MedicationReminderService.java
│   ├── repository/    MedicationReminderRepository.java
│   ├── entity/        MedicationReminder.java
│   └── dto/
│
├── triage/
│   ├── controller/    TriageController.java
│   ├── service/       TriageService.java
│   ├── rules/         RedFlagRuleEngine.java  RedFlagRule.java (interface)
│   │                  ChestPainRule.java  StrokeSymptomRule.java
│   │                  BreathingDifficultyRule.java  ...
│   ├── ai/            TriageAiProvider.java (interface)
│   │                  GeminiTriageProvider.java
│   │                  MockTriageProvider.java
│   ├── repository/    TriageSessionRepository.java
│   ├── entity/        TriageSession.java
│   └── dto/
│
├── emergency/
│   ├── controller/    EmergencyController.java  ResponderController.java
│   ├── service/       EmergencyService.java  ResponderAssignmentService.java
│   ├── repository/    EmergencyRequestRepository.java  ResponderRepository.java
│   │                  ResponderAssignmentRepository.java
│   ├── entity/        EmergencyRequest.java  Responder.java  ResponderAssignment.java
│   └── dto/
│
├── notification/
│   ├── service/       NotificationService.java
│   ├── provider/      NotificationProvider.java (interface)
│   │                  MockNotificationProvider.java
│   │                  (EmailNotificationProvider.java — future)
│   └── dto/           NotificationEvent.java
│
├── audit/
│   ├── aspect/        AuditAspect.java  (Spring AOP — intercepts service methods)
│   ├── service/       AuditService.java
│   ├── repository/    AuditLogRepository.java
│   └── entity/        AuditLog.java
│
└── common/
    ├── config/        SecurityConfig.java  CorsConfig.java  OpenApiConfig.java
    │                  FileStorageConfig.java
    ├── dto/           ApiResponse.java  PagedResponse.java  ErrorResponse.java
    ├── exception/     GlobalExceptionHandler.java  CuraException.java
    │                  ResourceNotFoundException.java  AccessDeniedException.java
    │                  ConflictException.java  ValidationException.java
    └── util/          DateTimeUtil.java  FileUtil.java
```

---

## Frontend Folder Structure

```
frontend/
├── index.html
├── vite.config.ts          (proxies /api → http://localhost:8080)
├── tailwind.config.ts
├── tsconfig.json
└── src/
    ├── main.tsx
    ├── App.tsx              (routes)
    │
    ├── api/
    │   ├── client.ts        (Axios instance with JWT interceptor + refresh logic)
    │   ├── auth.ts
    │   ├── doctors.ts
    │   ├── appointments.ts
    │   ├── records.ts
    │   ├── prescriptions.ts
    │   ├── triage.ts
    │   └── emergency.ts
    │
    ├── components/
    │   ├── common/          Button  Input  Modal  Table  Badge  Spinner  Alert
    │   ├── layout/          Header  Sidebar  ProtectedRoute  RoleGate
    │   └── domain/          DoctorCard  AppointmentSlot  RecordRow
    │                        TriageForm  EmergencyBanner  PrescriptionViewer
    │
    ├── pages/
    │   ├── auth/            LoginPage.tsx  RegisterPage.tsx
    │   ├── dashboard/       PatientDashboard.tsx  DoctorDashboard.tsx
    │   ├── doctors/         DoctorListPage.tsx  DoctorDetailPage.tsx
    │   ├── appointments/    AppointmentBookPage.tsx  MyAppointmentsPage.tsx
    │   ├── records/         MyRecordsPage.tsx  RecordDetailPage.tsx
    │   ├── prescriptions/   PrescriptionUploadPage.tsx  PrescriptionDetailPage.tsx
    │   ├── triage/          TriageWizardPage.tsx  TriageResultPage.tsx
    │   └── emergency/       EmergencySOSPage.tsx  EmergencyStatusPage.tsx
    │
    ├── hooks/
    │   ├── useAuth.ts
    │   ├── useDoctors.ts
    │   ├── useAppointments.ts
    │   └── useEmergency.ts
    │
    ├── store/
    │   └── authStore.ts     (Zustand — lightweight, no Redux boilerplate)
    │
    ├── types/
    │   ├── auth.ts
    │   ├── doctor.ts
    │   ├── appointment.ts
    │   ├── record.ts
    │   ├── prescription.ts
    │   ├── triage.ts
    │   └── emergency.ts
    │
    └── utils/
        ├── formatters.ts    (dates, phone numbers, currency)
        └── validators.ts    (client-side pre-validation helpers)
```

---

## Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Auth strategy | JWT (access + refresh) | Stateless; works with React SPA without session cookies |
| State management | Zustand | Simpler than Redux; sufficient for this scale |
| Provider pattern | Interface per external service | Allows MockOcrProvider, MockTriageProvider in tests |
| Triage safety | Deterministic rules run first, AI is supplementary | AI cannot override red-flag escalation |
| Geospatial | PostGIS GEOGRAPHY type | Native spatial indexing for responder proximity queries |
| File storage | Local filesystem (dev) | See Decision Log — S3 migration path is documented |
| Audit | Spring AOP aspect | Non-invasive; service methods don't manually call audit |

---

## Security Architecture

```
Request → JwtAuthenticationFilter (extracts + validates JWT)
        → Spring Security FilterChain
        → @PreAuthorize on controller methods (role checks)
        → Service layer (ownership checks — e.g. patient can only see own records)
        → GlobalExceptionHandler (maps exceptions to structured error DTOs)
```

Access token: 15 minutes.
Refresh token: 7 days, stored in DB, rotated on use, revocable.

---

## AI Provider Pattern

```java
// Interface — in prescription/ocr/ or triage/ai/
public interface OcrProvider {
    OcrResult extractFromImage(byte[] imageBytes);
}

// Production implementation
@Profile("!test")
@Service
public class GeminiOcrProvider implements OcrProvider { ... }

// Test / mock implementation
@Profile("test")
@Service
public class MockOcrProvider implements OcrProvider { ... }
```

Same pattern for `TriageAiProvider` and `NotificationProvider`.
Production vs. mock selection is controlled by Spring profiles, not `if` branches.
