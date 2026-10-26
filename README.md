<div align="center">

# CURA

### Connected Healthcare. Responsible AI. Coordinated Emergency Support.

**An AI-assisted digital health platform for telehealth, clinical triage, electronic medical records, prescription processing, and emergency first-responder coordination.**

![Status](https://img.shields.io/badge/status-in%20development-orange)
![Frontend](https://img.shields.io/badge/frontend-React%20%2B%20TypeScript-61DAFB?logo=react&logoColor=black)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Database](https://img.shields.io/badge/database-PostgreSQL%20%2B%20PostGIS-4169E1?logo=postgresql&logoColor=white)
![License](https://img.shields.io/badge/license-not%20specified-lightgrey)

</div>

---

> **Important — educational prototype only.** CURA is a B.Tech CSE capstone project designed to use synthetic patient data and simulated external integrations. It is **not a validated clinical system** and must not be used for real patient care. AI-generated guidance is not a medical diagnosis, and the prototype must not be represented as contacting real emergency services unless an authorized, verified integration is actually in place.

## Table of Contents

- [Overview](#overview)
- [Problem and Vision](#problem-and-vision)
- [Core Modules](#core-modules)
- [User Roles](#user-roles)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [How a Request Flows](#how-a-request-flows)
- [Data Model](#data-model)
- [API Overview](#api-overview)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Development Workflow](#development-workflow)
- [Testing Strategy](#testing-strategy)
- [Security and Safety](#security-and-safety)
- [Project Status and Roadmap](#project-status-and-roadmap)
- [Repository Documentation](#repository-documentation)
- [Contributing](#contributing)
- [Team and Acknowledgements](#team-and-acknowledgements)
- [License](#license)

## Overview

| Item | Details |
|---|---|
| Project | **CURA** — AI-assisted digital health platform |
| Project type | B.Tech CSE capstone / educational prototype |
| Architecture | Modular monolith |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Backend | Java, Spring Boot REST API |
| Database | PostgreSQL with PostGIS |
| AI integrations | Provider abstraction for optional AI-assisted triage and prescription OCR |
| Safety boundary | Synthetic data and simulated integrations; not for real patient care |

**CURA** is an interoperable digital health platform that brings several healthcare workflows into one modular application. It combines doctor discovery and appointments, telehealth, electronic medical records (EMR), prescription OCR, medication reminders, AI-assisted symptom triage, and emergency responder coordination.

The project is being developed as a capstone with two goals:

1. Build a demonstrable full-stack application using a maintainable architecture.
2. Apply AI responsibly while learning and documenting the engineering decisions behind the system.

CURA is designed as a **modular monolith**: one deployable Spring Boot backend organized into distinct domain modules, with a React single-page application and PostgreSQL as the primary data store. This keeps module boundaries clear without introducing the operational overhead of microservices prematurely.

## Problem and Vision

Healthcare workflows can be fragmented across appointments, clinical records, prescriptions, symptom assessment, and emergency coordination. CURA explores how these workflows could be connected through a single application with role-aware access and clearly defined data flows.

The intended experience is to help users:

- Discover doctors and manage appointments.
- Access authorized medical records and supporting documents.
- Digitize prescription information while keeping human verification in the loop.
- Receive structured, safety-first symptom triage guidance.
- Demonstrate an emergency SOS and responder-coordination workflow in a controlled simulation.

CURA is an educational engineering prototype, not a claim of clinical effectiveness or production readiness.

## Core Modules

| Module | Purpose | Important boundary |
|---|---|---|
| Doctor discovery and appointments | Search doctors, inspect availability, book or cancel appointments | Prevent double-booking with database constraints |
| Telehealth | Video consultations | WebRTC is planned for a later milestone |
| Electronic medical records (EMR) | Store and retrieve clinical notes and attachments | Enforce ownership and case-level access |
| Prescription processing | Extract medicine details from prescription images | Human verification is required |
| Generic medicine comparison | Compare generic alternatives for cost transparency | Never silently change a prescription |
| Medication reminders | Manage reminders linked to prescription items | Notification delivery is initially mocked |
| Symptom triage | Collect symptoms and provide structured guidance | Deterministic red-flag checks; AI is optional and not diagnostic |
| Emergency coordination | SOS workflow and nearby responder matching | Simulated dispatch unless a verified integration is implemented |
| Administration and auditing | Manage users, hospitals, consent records, and audit logs | Enforce role and ownership checks |

### Module Details

| Module | Intended capabilities | Notes |
|---|---|---|
| Doctor discovery and appointments | Search and filter doctors, view availability, book or cancel slots, prevent double-booking | Booking consistency is supported by a unique appointment-slot constraint in the proposed schema |
| Telehealth | Video consultation appointments | WebRTC is planned for a later milestone |
| Electronic medical records | Create clinical records, upload lab reports and imaging, retrieve records with access controls | Uses patient/doctor relationships and attachment metadata |
| Prescription processing | Upload prescription images, extract structured medicine details, flag uncertainty, verify extracted items | OCR output requires appropriate human review |
| Generic medicine comparison | Map brand names to generic alternatives for cost transparency | Matching must not silently substitute or modify a prescription |
| Medication reminders | Manage scheduled reminders linked to prescription items | Notification delivery is initially designed to be mocked or simulated |
| Symptom intake and triage | Collect structured symptoms, apply deterministic red-flag rules, optionally request AI guidance | Red-flag escalation takes precedence over AI-generated guidance |
| Emergency SOS and responder coordination | Record SOS requests, find nearby available responders, manage assignments and location updates | Emergency dispatch and external notifications are simulated by default |
| Administration and auditing | Manage users and hospitals, review audit logs, record consent | Access is controlled by user roles and ownership checks |

## User Roles

| Role | Intended permissions |
|---|---|
| `PATIENT` | Manage appointments, view their own records and prescriptions, submit symptom intake, manage reminders, and trigger simulated SOS requests |
| `DOCTOR` | Manage availability, view records for authorized cases, create clinical notes, and verify prescription extraction |
| `ADMIN` | Manage users and hospitals, review audit logs, and oversee administrative workflows |
| `RESPONDER` | Update availability and location, view assigned emergency requests, and update assignment status |

Role checks alone are not sufficient for sensitive data: the application must also enforce ownership and case-level access rules.

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Frontend | React 18, TypeScript, Vite | Single-page web application |
| Styling and UI | Tailwind CSS | Responsive interface and reusable styling |
| Frontend routing | React Router v6 | Client-side navigation |
| Frontend state | Zustand | Shared state, including authentication state |
| Backend | Java 24, Spring Boot 3.x | REST API and modular business logic |
| API security | Spring Security, JWT access and refresh tokens | Authentication and authorization |
| Persistence | Spring Data JPA, Hibernate | Relational persistence |
| Database | PostgreSQL 16 | Primary application data store |
| Geospatial capabilities | PostGIS | Location-based responder search |
| Database migrations | Flyway | Versioned schema changes |
| API documentation | OpenAPI 3, Swagger UI | API exploration and documentation |
| AI integration | Gemini API behind provider interfaces | Optional AI-assisted features |
| File storage | Local filesystem for development; S3-compatible storage planned | Uploaded documents and attachments |
| Video consultations | WebRTC planned | Future video consultation support |
| Backend testing | JUnit 5, Mockito, MockMvc | Unit and API testing |
| End-to-end testing | Selenium WebDriver | Browser workflow testing |
| Build tools | Maven, npm | Dependency management and builds |
| Deployment | Docker and Docker Compose | Reproducible development and deployment environments |

> **Note:** This table describes the intended technology stack from the project documentation. Verify actual versions and dependencies against the repository's build files.

## Architecture

CURA follows a **modular monolith architecture**. The backend is intended to be one Spring Boot application with clearly separated domain modules. Each module should own its business rules and interact with other modules through defined interfaces rather than uncontrolled cross-module access.

### High-Level Architecture

```mermaid
flowchart TB
    U[Users: Patient, Doctor, Admin, Responder]
    FE[React + TypeScript Frontend]
    API[Spring Boot REST API]
    SEC[Spring Security + JWT]
    SVC[Application Services]
    DB[(PostgreSQL + PostGIS)]
    FILES[File Storage]
    AI[AI Provider Interface]
    OCR[Prescription OCR Provider]
    TRIAGE[Triage AI Provider]
    NOTIFY[Notification Provider]

    U --> FE
    FE -->|HTTPS / JSON| API
    API --> SEC
    SEC --> SVC
    SVC --> DB
    SVC --> FILES
    SVC --> AI
    AI --> OCR
    AI --> TRIAGE
    SVC --> NOTIFY
