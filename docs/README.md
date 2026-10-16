# All-in-One Lighting — Documentation Index

Specification package for the **All-in-One Lighting** Android application.
This directory is the single source of truth for what the app does, how it is built,
and how it is verified. Implementation must follow these documents; deviations are
recorded in [DECISIONS.md](DECISIONS.md).

**Status:** Phase 0 complete (documentation). Phase 1 (application foundation) in progress.
**Last updated:** 2026-10-16

---

## How to read this package

| If you are… | Start with |
|---|---|
| A coding agent about to implement | [CLAUDE_CODE_IMPLEMENTATION_GUIDE.md](CLAUDE_CODE_IMPLEMENTATION_GUIDE.md) → [ROADMAP.md](ROADMAP.md) → the relevant spec |
| Reviewing scope | [PROJECT_VISION.md](PROJECT_VISION.md) → [PRODUCT_REQUIREMENTS.md](PRODUCT_REQUIREMENTS.md) |
| Building a screen | [SCREEN_SPECIFICATION.md](SCREEN_SPECIFICATION.md) + [UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) |
| Building the 3D simulator | [THREE_D_LIGHTING_SPEC.md](THREE_D_LIGHTING_SPEC.md) + [PERFORMANCE_AND_APP_SIZE.md](PERFORMANCE_AND_APP_SIZE.md) |
| Touching hardware | [ANDROID_HARDWARE_SPEC.md](ANDROID_HARDWARE_SPEC.md) + [DATA_PRIVACY_AND_SECURITY.md](DATA_PRIVACY_AND_SECURITY.md) |
| Verifying work | [TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md) + [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) |

---

## Documents

| # | Document | Covers |
|---|---|---|
| 1 | [PROJECT_VISION.md](PROJECT_VISION.md) | Product vision, audience, scope boundaries, principles, success criteria |
| 2 | [PRODUCT_REQUIREMENTS.md](PRODUCT_REQUIREMENTS.md) | `FR-*` / `NFR-*` requirements, user stories, edge cases, acceptance criteria |
| 3 | [SCREEN_SPECIFICATION.md](SCREEN_SPECIFICATION.md) | `SCR-*` screens: layout, states, interactions, navigation, errors |
| 4 | [UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) | Color, type, spacing, components, a11y, responsive rules |
| 5 | [TECHNICAL_ARCHITECTURE.md](TECHNICAL_ARCHITECTURE.md) | Layers, modules, state ownership, data flow, error model |
| 6 | [THREE_D_LIGHTING_SPEC.md](THREE_D_LIGHTING_SPEC.md) | Renderer, objects, pivots, light sync, environments, quality modes |
| 7 | [ANDROID_HARDWARE_SPEC.md](ANDROID_HARDWARE_SPEC.md) | Torch, screen brightness, permissions, device variance, failure modes |
| 8 | [SMART_LIGHT_FUTURE_SPEC.md](SMART_LIGHT_FUTURE_SPEC.md) | Deferred smart-light interfaces, protocol comparison, security |
| 9 | [DATA_PRIVACY_AND_SECURITY.md](DATA_PRIVACY_AND_SECURITY.md) | Permission matrix, data minimization, storage, network posture |
| 10 | [TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md) | `T-*` test inventory, fakes strategy, release gates |
| 11 | [PERFORMANCE_AND_APP_SIZE.md](PERFORMANCE_AND_APP_SIZE.md) | Budgets, measurement procedure, asset optimization |
| 12 | [ROADMAP.md](ROADMAP.md) | Phases 0–6 with prerequisites, deliverables, exit criteria |
| 13 | [CLAUDE_CODE_IMPLEMENTATION_GUIDE.md](CLAUDE_CODE_IMPLEMENTATION_GUIDE.md) | Agent rules, conventions, validation commands, reporting format |
| 14 | [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) | Requirement → module → screen → test → acceptance criterion |
| — | [DECISIONS.md](DECISIONS.md) | `ADR-*` architectural decisions and rationale |
| — | [KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md) | Device-dependent behavior, open questions, deferred features |

---

## Identifier conventions

Used consistently across every document so the traceability matrix resolves.

| Prefix | Meaning | Example |
|---|---|---|
| `FR-` | Functional requirement | `FR-TORCH-01` |
| `NFR-` | Nonfunctional requirement | `NFR-PERF-02` |
| `SCR-` | Screen | `SCR-HOME` |
| `CMP-` | Design-system component | `CMP-SEGMENTED` |
| `OBJ-` | 3D object | `OBJ-TORCH` |
| `AC-` | Acceptance criterion | `AC-TORCH-01` |
| `T-` | Test case | `T-TORCH-01` |
| `ADR-` | Architecture decision record | `ADR-004` |
| `LIM-` | Known limitation | `LIM-FLASH-STRENGTH` |
| `OQ-` | Open question | `OQ-03` |

## Decision labels

Every material statement in this package carries one of these labels where its
status is not self-evident:

- **[Confirmed]** — stated by the product owner; implement as written.
- **[Recommended]** — our engineering recommendation; implement unless overruled.
- **[Provisional]** — assumption made to unblock work; must be validated before release.
- **[Open]** — unresolved; tracked in [KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md) with a recommended default.
- **[Deferred]** — intentionally out of scope for v1.0.

## Visual reference

The UI concept board was supplied by the product owner and reviewed in full before
this package was written. A durable copy belongs at
`docs/reference/all_in_one_lighting_ui.png`. Everything derived from it is captured
in text in [UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) and
[SCREEN_SPECIFICATION.md](SCREEN_SPECIFICATION.md), so implementation does not
depend on the image file being present.
