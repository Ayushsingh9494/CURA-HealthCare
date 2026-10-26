# CURA — Database Schema

## Design Principles
- Every table has a surrogate `BIGSERIAL` primary key.
- Timestamps use `TIMESTAMPTZ` (timezone-aware) everywhere.
- Foreign keys are explicit with `REFERENCES`.
- Indexes are created on all foreign key columns and frequently-filtered columns.
- PostGIS `GEOGRAPHY(POINT, 4326)` is used for coordinate columns (WGS-84).
- All schema changes are managed through Flyway migrations (`V{n}__{description}.sql`).

---

## Entity Relationship Summary

```
users ──────────────── user_profiles          (1:1)
users ──────────────── doctors                (1:1, role=DOCTOR)
users ──────────────── responders             (1:1, role=RESPONDER)
hospitals ───────────── doctors               (1:many)
doctors ─────────────── availability_slots    (1:many)
users (patient) ─────── appointments          (1:many)
doctors ─────────────── appointments          (1:many)
availability_slots ───── appointments         (1:1)
users (patient) ─────── medical_records       (1:many)
medical_records ──────── record_attachments   (1:many)
medical_records ──────── prescriptions        (1:many)
prescriptions ────────── prescription_items   (1:many)
users (patient) ─────── medication_reminders  (1:many)
prescription_items ───── medication_reminders (1:many, optional)
users (patient) ─────── triage_sessions       (1:many)
users (patient) ─────── emergency_requests    (1:many)
emergency_requests ───── responder_assignments(1:many)
responders ───────────── responder_assignments(1:many)
users ──────────────── audit_logs             (1:many)
users (patient) ─────── consent_records       (1:many)
users ──────────────── refresh_tokens         (1:many)
```

---

## Full Schema (Flyway Migrations)

### V1__create_users.sql
```sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL
                        CHECK (role IN ('PATIENT','DOCTOR','ADMIN','RESPONDER')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE user_profiles (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    date_of_birth       DATE,
    gender              VARCHAR(20)
                            CHECK (gender IN ('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY')),
    blood_group         VARCHAR(10),
    address             TEXT,
    profile_image_path  VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
```

### V2__create_hospitals_and_doctors.sql
```sql
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE hospitals (
    id                 BIGSERIAL    PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    address            TEXT         NOT NULL,
    location           GEOGRAPHY(POINT, 4326),   -- lng, lat
    phone              VARCHAR(20),
    email              VARCHAR(255),
    emergency_capable  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_hospitals_location ON hospitals USING GIST(location);

CREATE TABLE doctors (
    id                        BIGSERIAL       PRIMARY KEY,
    user_id                   BIGINT          NOT NULL UNIQUE REFERENCES users(id),
    hospital_id               BIGINT          REFERENCES hospitals(id),
    medical_license_no        VARCHAR(100)    NOT NULL UNIQUE,
    specialization            VARCHAR(100)    NOT NULL,
    years_of_experience       INTEGER,
    consultation_fee          NUMERIC(10, 2),
    bio                       TEXT,
    accepts_video_consultation BOOLEAN        NOT NULL DEFAULT FALSE,
    is_available              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doctors_user_id      ON doctors(user_id);
CREATE INDEX idx_doctors_hospital_id  ON doctors(hospital_id);
CREATE INDEX idx_doctors_specialization ON doctors(specialization);
```

### V3__create_appointments.sql
```sql
CREATE TABLE availability_slots (
    id          BIGSERIAL    PRIMARY KEY,
    doctor_id   BIGINT       NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    start_time  TIMESTAMPTZ  NOT NULL,
    end_time    TIMESTAMPTZ  NOT NULL,
    slot_type   VARCHAR(20)  NOT NULL DEFAULT 'IN_PERSON'
                    CHECK (slot_type IN ('IN_PERSON','VIDEO')),
    is_booked   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_slot_times CHECK (end_time > start_time)
);

CREATE INDEX idx_slots_doctor_id   ON availability_slots(doctor_id);
CREATE INDEX idx_slots_start_time  ON availability_slots(start_time);

CREATE TABLE appointments (
    id                BIGSERIAL    PRIMARY KEY,
    patient_id        BIGINT       NOT NULL REFERENCES users(id),
    doctor_id         BIGINT       NOT NULL REFERENCES doctors(id),
    slot_id           BIGINT       NOT NULL UNIQUE REFERENCES availability_slots(id),
    appointment_type  VARCHAR(20)  NOT NULL
                          CHECK (appointment_type IN ('IN_PERSON','VIDEO')),
    status            VARCHAR(30)  NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN
                              ('PENDING','CONFIRMED','CANCELLED','COMPLETED','NO_SHOW')),
    reason            TEXT,
    patient_notes     TEXT,
    doctor_notes      TEXT,
    video_room_id     VARCHAR(255),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor_id  ON appointments(doctor_id);
CREATE INDEX idx_appointments_status     ON appointments(status);
```

### V4__create_emr.sql
```sql
CREATE TABLE medical_records (
    id               BIGSERIAL    PRIMARY KEY,
    patient_id       BIGINT       NOT NULL REFERENCES users(id),
    doctor_id        BIGINT       REFERENCES doctors(id),
    appointment_id   BIGINT       REFERENCES appointments(id),
    diagnosis        TEXT,
    clinical_notes   TEXT,
    follow_up_date   DATE,
    is_confidential  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_records_patient_id ON medical_records(patient_id);
CREATE INDEX idx_records_doctor_id  ON medical_records(doctor_id);

CREATE TABLE record_attachments (
    id                BIGSERIAL     PRIMARY KEY,
    record_id         BIGINT        NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    original_filename VARCHAR(255)  NOT NULL,
    stored_filename   VARCHAR(255)  NOT NULL,
    file_path         VARCHAR(500)  NOT NULL,
    content_type      VARCHAR(100),
    file_size_bytes   BIGINT,
    attachment_type   VARCHAR(50)
                          CHECK (attachment_type IN ('LAB_REPORT','IMAGING','OTHER')),
    uploaded_by       BIGINT        REFERENCES users(id),
    uploaded_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_record_id ON record_attachments(record_id);
```

### V5__create_prescriptions.sql
```sql
CREATE TABLE prescriptions (
    id                   BIGSERIAL    PRIMARY KEY,
    record_id            BIGINT       NOT NULL REFERENCES medical_records(id),
    original_image_path  VARCHAR(500),
    ocr_status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING'
                             CHECK (ocr_status IN
                                 ('PENDING','PROCESSING','COMPLETED','FAILED')),
    ocr_confidence       NUMERIC(5, 2),   -- 0.00 to 100.00
    ocr_raw_text         TEXT,
    verification_status  VARCHAR(30)  NOT NULL DEFAULT 'UNVERIFIED'
                             CHECK (verification_status IN
                                 ('UNVERIFIED','VERIFIED','FLAGGED')),
    verified_by          BIGINT       REFERENCES users(id),
    verified_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prescriptions_record_id ON prescriptions(record_id);

CREATE TABLE prescription_items (
    id                   BIGSERIAL    PRIMARY KEY,
    prescription_id      BIGINT       NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_name        VARCHAR(255) NOT NULL,
    generic_name         VARCHAR(255),
    dosage               VARCHAR(100),
    frequency            VARCHAR(100),
    duration             VARCHAR(100),
    instructions         TEXT,
    is_confidence_flagged BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_presc_items_prescription_id ON prescription_items(prescription_id);
```

### V6__create_medication_reminders.sql
```sql
CREATE TABLE medication_reminders (
    id                   BIGSERIAL    PRIMARY KEY,
    patient_id           BIGINT       NOT NULL REFERENCES users(id),
    prescription_item_id BIGINT       REFERENCES prescription_items(id),
    medicine_name        VARCHAR(255) NOT NULL,
    reminder_time        TIME         NOT NULL,
    days_of_week         VARCHAR(50),   -- e.g. 'MON,WED,FRI'
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    start_date           DATE         NOT NULL,
    end_date             DATE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reminders_patient_id ON medication_reminders(patient_id);
```

### V7__create_triage.sql
```sql
CREATE TABLE triage_sessions (
    id                    BIGSERIAL    PRIMARY KEY,
    patient_id            BIGINT       NOT NULL REFERENCES users(id),
    symptoms_json         JSONB        NOT NULL,
    red_flags_triggered   JSONB,       -- array of triggered rule names
    severity              VARCHAR(30)  NOT NULL
                              CHECK (severity IN
                                  ('LOW','MODERATE','HIGH','CRITICAL','EMERGENCY')),
    deterministic_outcome VARCHAR(100) NOT NULL,
    ai_guidance           TEXT,
    ai_provider_used      VARCHAR(50),
    ai_confidence         NUMERIC(5, 2),
    ai_disclaimer         TEXT         NOT NULL
                              DEFAULT 'This is AI-assisted guidance and NOT a validated medical diagnosis.',
    recommendation        VARCHAR(50)  NOT NULL
                              CHECK (recommendation IN
                                  ('SELF_CARE','SEE_DOCTOR','URGENT_CARE','EMERGENCY')),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_triage_patient_id  ON triage_sessions(patient_id);
CREATE INDEX idx_triage_severity    ON triage_sessions(severity);
CREATE INDEX idx_triage_created_at  ON triage_sessions(created_at);
```

### V8__create_emergency.sql
```sql
CREATE TABLE emergency_requests (
    id                BIGSERIAL    PRIMARY KEY,
    patient_id        BIGINT       NOT NULL REFERENCES users(id),
    description       TEXT,
    patient_location  GEOGRAPHY(POINT, 4326),
    address_text      TEXT,
    status            VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE','ASSIGNED','RESOLVED','CANCELLED')),
    is_simulated      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_emergency_patient_id ON emergency_requests(patient_id);
CREATE INDEX idx_emergency_status     ON emergency_requests(status);
CREATE INDEX idx_emergency_location
    ON emergency_requests USING GIST(patient_location);

CREATE TABLE responders (
    id                   BIGSERIAL    PRIMARY KEY,
    user_id              BIGINT       NOT NULL UNIQUE REFERENCES users(id),
    vehicle_type         VARCHAR(50)
                             CHECK (vehicle_type IN ('AMBULANCE','FIRST_RESPONDER')),
    vehicle_registration VARCHAR(100),
    current_location     GEOGRAPHY(POINT, 4326),
    status               VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE'
                             CHECK (status IN ('AVAILABLE','BUSY','OFFLINE')),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_responders_status   ON responders(status);
CREATE INDEX idx_responders_location ON responders USING GIST(current_location);

CREATE TABLE responder_assignments (
    id            BIGSERIAL    PRIMARY KEY,
    emergency_id  BIGINT       NOT NULL REFERENCES emergency_requests(id),
    responder_id  BIGINT       NOT NULL REFERENCES responders(id),
    assigned_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    eta_minutes   INTEGER,
    status        VARCHAR(30)  NOT NULL DEFAULT 'ASSIGNED'
                      CHECK (status IN ('ASSIGNED','EN_ROUTE','ARRIVED','COMPLETED')),
    notes         TEXT
);

CREATE INDEX idx_assignments_emergency_id ON responder_assignments(emergency_id);
CREATE INDEX idx_assignments_responder_id ON responder_assignments(responder_id);
```

### V9__create_audit_and_consent.sql
```sql
CREATE TABLE audit_logs (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       REFERENCES users(id),
    action           VARCHAR(100) NOT NULL,
    resource_type    VARCHAR(100),
    resource_id      BIGINT,
    ip_address       INET,
    user_agent       TEXT,
    request_method   VARCHAR(10),
    request_path     VARCHAR(500),
    additional_data  JSONB,
    timestamp        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user_id   ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_action    ON audit_logs(action);

CREATE TABLE consent_records (
    id            BIGSERIAL    PRIMARY KEY,
    patient_id    BIGINT       NOT NULL REFERENCES users(id),
    consent_type  VARCHAR(100) NOT NULL
                      CHECK (consent_type IN
                          ('DATA_SHARING','TELEHEALTH','RESEARCH','EMERGENCY_ACCESS')),
    granted       BOOLEAN      NOT NULL,
    granted_at    TIMESTAMPTZ,
    revoked_at    TIMESTAMPTZ,
    ip_address    INET,
    notes         TEXT
);

CREATE INDEX idx_consent_patient_id ON consent_records(patient_id);
```

---

## Key Constraints Explained

| Constraint | Table | Why |
|-----------|-------|-----|
| `UNIQUE` on `slot_id` in `appointments` | appointments | One slot → at most one appointment (prevents double-booking) |
| `CHECK (end_time > start_time)` | availability_slots | Prevents nonsensical slots |
| `UNIQUE` on `user_id` in `doctors` | doctors | One doctor profile per user account |
| `DEFAULT TRUE` on `is_simulated` | emergency_requests | Safety: all emergencies are simulated unless explicitly set otherwise |
| `DEFAULT` disclaimer on `ai_disclaimer` | triage_sessions | AI guidance always carries mandatory disclaimer |
| `GIST` indexes on `GEOGRAPHY` columns | hospitals, responders, emergency_requests | Enables fast ST_DWithin proximity queries |

---

## Geospatial Query Pattern (Milestone 8)

```sql
-- Find available responders within 10 km of an emergency location
SELECT r.*,
       ST_Distance(r.current_location, e.patient_location) AS distance_metres
FROM   responders r,
       emergency_requests e
WHERE  e.id = :emergencyId
  AND  r.status = 'AVAILABLE'
  AND  ST_DWithin(r.current_location, e.patient_location, 10000)
ORDER  BY distance_metres
LIMIT  5;
```
