# CURA — API Contracts

**Base URL**: `http://localhost:8080/api/v1`
**Auth**: Bearer JWT in `Authorization` header for all protected endpoints.
**Content-Type**: `application/json` (multipart/form-data for file uploads).

All responses follow this envelope:
```json
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "timestamp": "2026-10-26T00:00:00Z"
}
```

Errors:
```json
{
  "success": false,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Doctor with id 99 not found",
  "timestamp": "2026-10-26T00:00:00Z"
}
```

---

## Auth Endpoints

| Method | Path | Auth Required | Roles | Description |
|--------|------|:---:|-------|-------------|
| POST | `/auth/register` | ✗ | — | Register new account |
| POST | `/auth/login` | ✗ | — | Login, receive access + refresh tokens |
| POST | `/auth/refresh` | ✗ | — | Exchange refresh token for new access token |
| POST | `/auth/logout` | ✓ | Any | Revoke refresh token |
| GET  | `/auth/me` | ✓ | Any | Get current user info |

### POST /auth/register
Request:
```json
{
  "email": "patient@example.com",
  "password": "StrongP@ss1",
  "role": "PATIENT",
  "firstName": "Priya",
  "lastName": "Sharma",
  "phone": "+919876543210"
}
```
Response `201`:
```json
{
  "userId": 1,
  "email": "patient@example.com",
  "role": "PATIENT",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### POST /auth/login
Request: `{ "email": "...", "password": "..." }`
Response `200`: Same as register response.

---

## User / Profile Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/users/profile` | ✓ | Any | Get own profile |
| PUT  | `/users/profile` | ✓ | Any | Update own profile |
| GET  | `/users/{id}/profile` | ✓ | ADMIN | Get any user's profile |

---

## Doctor Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/doctors` | ✓ | Any | List doctors (filterable) |
| GET  | `/doctors/{id}` | ✓ | Any | Get doctor detail |
| GET  | `/doctors/{id}/slots` | ✓ | Any | Get available slots |
| POST | `/doctors/{id}/slots` | ✓ | DOCTOR | Create availability slots |
| DELETE | `/doctors/slots/{slotId}` | ✓ | DOCTOR | Remove unbooked slot |
| PUT  | `/doctors/{id}` | ✓ | DOCTOR, ADMIN | Update doctor profile |

### GET /doctors — Query Params
| Param | Type | Example | Description |
|-------|------|---------|-------------|
| `specialization` | string | `Cardiology` | Filter by specialization |
| `hospitalId` | long | `2` | Filter by hospital |
| `acceptsVideo` | boolean | `true` | Filter by video availability |
| `page` | int | `0` | Pagination (0-indexed) |
| `size` | int | `10` | Page size |

---

## Appointment Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/appointments` | ✓ | PATIENT, DOCTOR | List own appointments |
| POST | `/appointments` | ✓ | PATIENT | Book an appointment |
| GET  | `/appointments/{id}` | ✓ | Owner, DOCTOR, ADMIN | Get appointment detail |
| PUT  | `/appointments/{id}/cancel` | ✓ | Owner, DOCTOR | Cancel appointment |
| PUT  | `/appointments/{id}/complete` | ✓ | DOCTOR | Mark completed |
| PUT  | `/appointments/{id}/doctor-notes` | ✓ | DOCTOR | Add doctor notes |

### POST /appointments
Request:
```json
{
  "doctorId": 3,
  "slotId": 17,
  "appointmentType": "IN_PERSON",
  "reason": "Chest pain follow-up"
}
```
Response `201`: Appointment object.

---

## Medical Records Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/records` | ✓ | PATIENT (own), DOCTOR (their cases) | List records |
| POST | `/records` | ✓ | DOCTOR | Create medical record |
| GET  | `/records/{id}` | ✓ | Owner patient, treating DOCTOR, ADMIN | Get record |
| PUT  | `/records/{id}` | ✓ | Treating DOCTOR | Update record |
| POST | `/records/{id}/attachments` | ✓ | DOCTOR, PATIENT (for own uploads) | Upload file |
| GET  | `/records/{id}/attachments` | ✓ | Owner, treating DOCTOR | List attachments |
| GET  | `/records/attachments/{attachmentId}/download` | ✓ | Authorized parties | Download file |

---

## Prescription Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| POST | `/prescriptions` | ✓ | DOCTOR | Create prescription (with optional image) |
| GET  | `/prescriptions/{id}` | ✓ | Owner patient, DOCTOR, ADMIN | Get prescription |
| POST | `/prescriptions/{id}/ocr` | ✓ | DOCTOR | Trigger OCR on uploaded image |
| PUT  | `/prescriptions/{id}/verify` | ✓ | DOCTOR | Verify/flag extracted items |
| GET  | `/prescriptions/{id}/items` | ✓ | Owner, DOCTOR | Get prescription line items |
| PUT  | `/prescriptions/{id}/items/{itemId}` | ✓ | DOCTOR | Correct an extracted item |

---

## Medication Reminder Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/reminders` | ✓ | PATIENT | List own reminders |
| POST | `/reminders` | ✓ | PATIENT | Create reminder |
| PUT  | `/reminders/{id}` | ✓ | Owner | Update reminder |
| DELETE | `/reminders/{id}` | ✓ | Owner | Deactivate reminder |

---

## Triage Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| POST | `/triage/sessions` | ✓ | PATIENT | Submit symptom intake and run triage |
| GET  | `/triage/sessions/{id}` | ✓ | Owner, DOCTOR, ADMIN | Get triage session result |
| GET  | `/triage/sessions` | ✓ | PATIENT (own), ADMIN | List triage sessions |

### POST /triage/sessions
Request:
```json
{
  "symptoms": [
    { "name": "chest_pain", "severity": "HIGH", "durationHours": 2 },
    { "name": "shortness_of_breath", "severity": "MODERATE", "durationHours": 2 }
  ],
  "patientAge": 52,
  "knownConditions": ["hypertension"],
  "requestAiGuidance": true
}
```
Response `200`:
```json
{
  "sessionId": 7,
  "severity": "EMERGENCY",
  "redFlagsTriggered": ["CHEST_PAIN_ACUTE", "BREATHING_DIFFICULTY"],
  "deterministicOutcome": "Call emergency services immediately",
  "recommendation": "EMERGENCY",
  "aiGuidance": null,
  "aiDisclaimer": "This is AI-assisted guidance and NOT a validated medical diagnosis.",
  "note": "Red flags detected — AI guidance suppressed in favour of emergency escalation."
}
```

---

## Emergency Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| POST | `/emergency/sos` | ✓ | PATIENT | Trigger SOS |
| GET  | `/emergency/{id}` | ✓ | Owner, RESPONDER, ADMIN | Get emergency status |
| PUT  | `/emergency/{id}/location` | ✓ | Owner PATIENT | Update patient location |
| PUT  | `/emergency/{id}/cancel` | ✓ | Owner PATIENT | Cancel SOS |
| GET  | `/emergency/active` | ✓ | ADMIN | List all active emergencies |

### POST /emergency/sos
Request:
```json
{
  "latitude": 28.6139,
  "longitude": 77.2090,
  "description": "Unconscious person",
  "isSimulated": true
}
```
Response `201`:
```json
{
  "emergencyId": 12,
  "status": "ACTIVE",
  "isSimulated": true,
  "message": "Emergency recorded. Searching for nearby responders.",
  "disclaimer": "This is a simulated emergency. No real emergency services have been notified."
}
```

## Responder Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/responders/nearby` | ✓ | ADMIN | Find nearby available responders |
| PUT  | `/responders/location` | ✓ | RESPONDER | Update own location |
| PUT  | `/responders/status` | ✓ | RESPONDER | Update own status |
| GET  | `/responders/assignments` | ✓ | RESPONDER | View own assignments |
| PUT  | `/responders/assignments/{id}/status` | ✓ | RESPONDER | Update assignment status |

---

## Admin Endpoints

| Method | Path | Auth | Roles | Description |
|--------|------|:---:|-------|-------------|
| GET  | `/admin/users` | ✓ | ADMIN | List all users |
| PUT  | `/admin/users/{id}/deactivate` | ✓ | ADMIN | Deactivate user |
| POST | `/admin/hospitals` | ✓ | ADMIN | Create hospital |
| GET  | `/admin/hospitals` | ✓ | ADMIN | List hospitals |
| GET  | `/admin/audit-logs` | ✓ | ADMIN | View audit logs |

---

## HTTP Status Code Usage

| Code | Meaning in CURA |
|------|----------------|
| 200 | Successful GET, PUT |
| 201 | Successful POST (resource created) |
| 204 | Successful DELETE (no body) |
| 400 | Validation failure — body has field-level errors |
| 401 | Missing or invalid JWT |
| 403 | Valid JWT but insufficient role/ownership |
| 404 | Resource does not exist |
| 409 | Conflict (e.g. slot already booked, email already registered) |
| 422 | Business rule violation (e.g. cancelling an already-completed appointment) |
| 500 | Unexpected server error — never leaks stack trace |

---

*This document is updated after each milestone. Last updated: Milestone 0 (pre-implementation).*
