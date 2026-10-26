# CURA — Project Overview

## What is CURA?

**CURA** is an interoperable digital health platform built as a B.Tech CSE capstone project.
It combines telehealth, AI-assisted clinical triage, electronic medical records, prescription
processing, and emergency first-responder coordination into a single modular application.

> **⚠ Educational Prototype Disclaimer**
> CURA uses synthetic patient data and simulated external integrations. It is not a
> validated clinical system and must not be used for real patient care.

---

## Core Modules

| # | Module | Capability |
|---|--------|-----------|
| 1 | Doctor Discovery & Appointments | Search doctors, view availability, book / cancel slots, prevent double-booking |
| 2 | Telehealth | Video consultation appointments (WebRTC, Milestone 9) |
| 3 | Electronic Medical Records | Create records, upload lab reports and imaging, access-controlled retrieval |
| 4 | Prescription OCR | Upload prescription image → AI-extracted structured items → human verification |
| 5 | Generic Medicine Comparison | Map brand names to generics for cost transparency |
| 6 | Medication Reminders | Scheduled reminders tied to prescription items, mock notification delivery |
| 7 | Symptom Intake & Triage | Structured symptom collection, deterministic red-flag detection, optional AI guidance |
| 8 | Emergency SOS | One-tap SOS, PostGIS proximity responder search, simulated dispatch notifications |

---

## Stakeholder Roles

| Role | Description |
|------|-------------|
| `PATIENT` | Books appointments, views own records, submits triage, triggers SOS |
| `DOCTOR` | Views patient records (on their cases), writes clinical notes, verifies prescriptions |
| `ADMIN` | Manages doctors, hospitals, users, and system configuration |
| `RESPONDER` | Receives and accepts emergency assignments, updates location |

---

## Technology Summary

```
Frontend          React 18 + TypeScript + Vite + Tailwind CSS + React Router v6
Backend           Java 24 + Spring Boot 3.x (modular monolith)
Database          PostgreSQL 16 + PostGIS (geospatial for emergency module)
Auth              Spring Security + JWT (access + refresh tokens)
Migrations        Flyway
ORM               Spring Data JPA + Hibernate
API Docs          OpenAPI 3 / Swagger UI
AI Integration    Google Gemini API (behind replaceable interface)
File Storage      Local filesystem (dev); S3-compatible (production — see Decision Log)
Video             WebRTC (Milestone 9, deferred)
Build             Maven (backend), npm / Vite (frontend)
Containers        Docker + Docker Compose
Testing           JUnit 5, Mockito, MockMvc, Selenium WebDriver
```

---

## Project Goals for Capstone

1. Demonstrate full-stack Java + React engineering skills.
2. Apply AI responsibly — with disclaimers, fallbacks, and provider abstraction.
3. Implement real-world concerns: auth, validation, transactions, audit, access control.
4. Produce a working, demonstrable system that can be explained and defended in a viva.
5. Maintain professional documentation and a clean Git history.
