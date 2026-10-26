# CURA — Decision Log

Decisions that materially affect architecture, security, cost, or scope are recorded here.
Any decision marked **AWAITING APPROVAL** must be resolved by Ayush before implementation proceeds.

Format: `DL-{n} | Date | Status | Decision`

---

## DL-01 | 2026-10-26 | APPROVED
### Authentication Strategy: JWT vs. Session Cookies

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **JWT (access + refresh)** | Stateless backend, works naturally with React SPA, easy to add to mobile later | Access token cannot be invalidated before expiry (mitigated by short 15-min window + refresh rotation) |
| Session Cookies + CSRF | Server-managed sessions, instant revocation | Requires sticky sessions or shared session store (Redis) in multi-instance deployment; more complex CORS setup |

**Recommendation**: JWT with 15-minute access token + 7-day refresh token stored in the database (enabling revocation on logout).

**Decision**: APPROVED. Implemented in Milestone 2.
**Decided by**: Ayush Singh
**Rationale**: Enables clean decoupled REST API and stateless Spring Security filter chain with database-persisted refresh token rotation.

---

## DL-02 | 2026-10-26 | AWAITING APPROVAL
### File Storage: Local Filesystem vs. S3-Compatible

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **Local filesystem (dev)** | No cost, no external dependency, simple setup | Not suitable for multi-instance deployment; data lost if container is replaced |
| AWS S3 / Cloudflare R2 | Scalable, durable, CDN-friendly | Requires API keys, cost, adds external dependency |
| MinIO (self-hosted S3) | S3-compatible, free, dockerisable | Extra container to manage |

**Recommendation**: Use local filesystem with an abstracted `FileStorageService` interface for Milestone 1–8. Document the MinIO/S3 migration path. Switch only if deployment requires it.

**Awaiting your decision**: Accept recommendation? If you have an AWS free-tier account, S3 is also viable.

---

## DL-03 | 2026-10-26 | AWAITING APPROVAL
### OCR Provider: Gemini Vision vs. Tesseract vs. Google Cloud Vision

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **Gemini Vision API** | Already in stack (from CapAssist experience), structured extraction with prompting, no extra dependency | API cost per call, requires internet, output non-deterministic |
| Tesseract4J | Free, offline, open-source | Poorer accuracy on handwritten prescriptions, needs post-processing |
| Google Cloud Vision | High accuracy, structured results | Separate billing account, additional SDK |

**Recommendation**: Gemini Vision behind the `OcrProvider` interface. Use `MockOcrProvider` in all tests. If OCR quality is insufficient during testing, swap to Cloud Vision without changing the service layer.

**Awaiting your decision**: Accept Gemini? Do you want to evaluate Tesseract first?

---

## DL-04 | 2026-10-26 | APPROVED
### Frontend State Management: Zustand vs. React Context vs. Redux Toolkit

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **Zustand** | Minimal boilerplate, simple API, adequate for this scale | Less familiar if you've only seen Redux |
| React Context + useReducer | No extra library, built-in | Verbose for complex state, re-render performance issues at scale |
| Redux Toolkit | Mature, excellent devtools | Heavy boilerplate for a project of this size |

**Recommendation**: Zustand for auth state (user, tokens, role). Use React Query (TanStack Query) for server state (API data fetching, caching, invalidation). This is the modern React pattern.

**Decision**: APPROVED. Implemented in Milestone 2.
**Decided by**: Ayush Singh
**Rationale**: Zustand provides zero-boilerplate state management for authentication and session persistence, pairing smoothly with TanStack Query.

---

## DL-05 | 2026-10-26 | AWAITING APPROVAL
### WebRTC Signalling (Milestone 9): Simple-Peer vs. MediaSoup vs. Jitsi

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **Simple-Peer + Spring WebSocket** | Lightweight, peer-to-peer, no media server | Fails behind strict NAT; needs TURN server for production |
| MediaSoup | Production-grade SFU, scales to many participants | Complex setup, Node.js SFU process required |
| Jitsi Meet (iframe embed) | Zero implementation effort, fully featured | External dependency, not our own code — weak for capstone demonstration |

**Recommendation**: Simple-Peer with a Spring WebSocket signalling channel for Milestone 9 (1:1 video). Document the limitation. Add a TURN server only if demo environment requires it.

**Awaiting your decision**: Accept Simple-Peer? Defer entire M9 until M1–M8 are solid?

---

## DL-06 | 2026-10-26 | AWAITING APPROVAL
### Java Version: Java 21 LTS vs. Java 24

**Context**: Your CapAssist project already uses Java 24 with `--enable-preview`.
Spring Boot 3.x supports Java 21 (LTS) officially; Java 24 works but is not yet LTS.

**Recommendation**: Use Java 21 LTS for CURA. Reasons:
- Safer for capstone (no preview feature gotchas).
- Better CI/CD compatibility.
- Spring Boot 3.x has official LTS support guarantee for Java 21.
- Virtual threads (Project Loom) are fully available in Java 21.

**Awaiting your decision**: Java 21 or stick with Java 24 (same as CapAssist)?

---

## DL-07 | 2026-10-26 | AWAITING APPROVAL
### PostGIS: Enable From Milestone 1 vs. Only in Milestone 8

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **Enable PostGIS from the start** | Hospitals table uses `GEOGRAPHY` from V2; no migration to add extension later | Requires PostGIS-enabled Docker image from day one |
| Enable only in M8 | Simpler initial setup | Requires a retroactive migration to alter hospitals and add extension — risky |

**Recommendation**: Enable PostGIS from the start by using `postgis/postgis:16-3.4` as the Docker image. The extension is activated in V2 migration (`CREATE EXTENSION IF NOT EXISTS postgis`). No performance cost if unused.

**Awaiting your decision**: Accept? (This is low-risk and strongly recommended.)

---

## DL-08 | 2026-10-26 | AWAITING APPROVAL
### Notification Delivery: Mock Only vs. Real Email via JavaMail

**Options**:
| Option | Pros | Cons |
|--------|------|------|
| **Mock provider only** | Zero infrastructure, zero cost, fully testable | No real notifications in demo |
| JavaMail + Gmail SMTP | Real email delivery in demo | Gmail SMTP requires app password or OAuth; potential spam-filter issues |
| SendGrid API | Reliable, good free tier | Another API key and external dependency |

**Recommendation**: Implement `MockNotificationProvider` first (logs to console + stores in-memory). The interface allows swapping to JavaMail or SendGrid with zero service-layer changes. For the capstone demo, mock notifications are sufficient and clearly labelled.

**Awaiting your decision**: Accept mock-first? Or set up real email from M6?

---

## Resolved Decisions

*(None yet — all are awaiting initial approval.)*

---

## Decision Template

```
## DL-{n} | {Date} | {Status: AWAITING APPROVAL / APPROVED / REJECTED}
### Short Title

**Options**: ...
**Recommendation**: ...
**Decision**: {filled in after approval}
**Decided by**: Ayush Singh
**Rationale**: ...
```
