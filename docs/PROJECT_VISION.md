# PROJECT_VISION.md — All-in-One Lighting

## 1. One-line vision

A single, trustworthy Android app that turns a phone into every kind of light the
user needs — and lets them explore how real light sources behave in an interactive
3D space.

## 2. The problem

The Android flashlight category is saturated with apps that are either (a) a single
torch button wrapped in advertising, or (b) "light" apps that fake functionality —
showing a brightness slider that does nothing, claiming features the hardware does
not support, or presenting a glowing picture as though it were a simulation.

Two unmet needs:

1. **Practical:** people want one reliable place for torch, screen light, emergency
   signalling and ambient light, that tells the truth about what their device can do.
2. **Exploratory:** students, photographers, lighting hobbyists, interior designers
   and curious users want to *understand* light — how a narrow beam differs from an
   omnidirectional bulb, what warm vs. cool actually looks like on a surface, how a
   tube light spreads along its length. Nothing in this category offers that.

## 3. Product pillars

| Pillar | What it means | Non-negotiable rule |
|---|---|---|
| **Honest hardware** | The UI reflects the *actual* result of every hardware operation | Never render an "on" state for an operation that failed |
| **Real simulation** | The 3D view computes lighting, it does not paint pictures of it | A glow sprite is never a substitute for a dynamic light |
| **Immediate utility** | The torch is reachable and usable within seconds of cold start | No onboarding, ad, or permission gate between launch and the torch |
| **Calm visual identity** | Dark, premium, restrained; glow is accent, not decoration | Design tokens only; no arbitrary colors or spacings |
| **Respectful of the device** | Battery, thermals and memory are treated as finite | No rendering while the simulator is not visible |

## 4. Target audience

**Primary — "the practical user"**
Needs a light *now*: a power cut, a dropped key, a dark footpath, signalling for
help. Success means one tap from launch, and a clear explanation when their device
cannot do something.

**Secondary — "the explorer"**
Curious about lighting: a photography student comparing beam spread, a design
student judging colour temperature on a surface, someone choosing bulbs for a room.
Success means the 3D simulator behaves plausibly enough to teach something.

**Tertiary — "the future smart-home owner"**
Already owns or plans to own smart bulbs. Success in v1.0 means an honest
"Coming Soon" that sets expectations without pretending to control anything.

## 5. Scope

### In scope for v1.0 (launch)

- Real camera-torch control with full failure handling
- Full-screen screen light: white, colour wheel, presets, brightness
- Emergency lighting: SOS / Strobe / Blink / Steady, with the output mechanism
  (screen or torch) explicitly selectable and labelled
- Candle Light and Night Light ambient modes
- 3D lighting simulator with **five** shipped objects: Torch, Bulb, Tube Light,
  Candle, LED Lamp
- Dark and Studio 3D environments; Performance / Balanced / High Quality modes
- Onboarding, Home dashboard, More Tools, Settings, About
- Smart Light section, clearly marked **Coming Soon**, non-interactive beyond
  informational content

### Explicitly out of scope for v1.0

| Item | Why | Where tracked |
|---|---|---|
| Smart-light control | No target ecosystem selected; needs real hardware to verify | [SMART_LIGHT_FUTURE_SPEC.md](SMART_LIGHT_FUTURE_SPEC.md), Phase 6 |
| In-app purchase / Go Pro | Concept board shows it; product owner has not approved billing for v1.0 | `OQ-01`, Phase 5 |
| 3D Asset Store | Depends on billing and on a hosted asset CDN | `OQ-01`, Phase 5 |
| Music-reactive lighting | Needs `RECORD_AUDIO`, which is disproportionate to the benefit and harms Play Store positioning | `OQ-02`, Phase 5+ |
| Party Mode | Low value without music reactivity; revisit after v1.0 | Phase 5 |
| Screen Light and Emergency Beacon as 3D objects | Reduce initial asset budget; the concept board shows six grid tiles, we ship five | `ADR-012` |
| Accounts, cloud sync, analytics | No feature requires them | [DATA_PRIVACY_AND_SECURITY.md](DATA_PRIVACY_AND_SECURITY.md) |

### Deliberately never in scope

- Any claim that the app guarantees rescue, visibility, or emergency communication
- Any background execution that keeps the torch on without a visible, user-initiated session
- Collecting personal data, contacts, location, or advertising identifiers

## 6. Design principles

1. **Truth over polish.** If a capability is missing, say so plainly in the UI with
   a reason. A disabled control with an explanation beats an enabled control that lies.
2. **One owner per resource.** The torch, the screen light, the emergency scheduler
   and the 3D renderer each have exactly one owning component. Controls read that
   owner's state; they never hold a parallel copy.
3. **Hardware is asynchronous and fallible.** Every hardware call is modelled as a
   request that may fail, be revoked, or be pre-empted by another app.
4. **Simulation is honest about being an approximation.** We say "simulated", we
   never say "physically accurate".
5. **Tokens, not magic numbers.** Every colour, spacing, radius and duration comes
   from the design system.
6. **Testable by construction.** Hardware and rendering sit behind interfaces so
   fakes can drive every state in automated tests.

## 7. Success criteria

### Product

| ID | Criterion | Measurement |
|---|---|---|
| SC-P1 | Torch reachable in ≤ 2 taps from cold start | Manual flow audit + UI test |
| SC-P2 | Every hardware failure path produces a specific, actionable message | Fault-injection UI tests, `T-TORCH-*` |
| SC-P3 | Simulator teaches a distinction: beam vs. omni vs. linear emission is visually obvious | Side-by-side device capture review at Phase 3 exit |
| SC-P4 | No control in the shipped app is non-functional without being labelled unavailable or Coming Soon | `AC-GLOBAL-01`, audited at release gate |

### Engineering

| ID | Criterion | Target | Status |
|---|---|---|---|
| SC-E1 | Cold start to interactive Home | ≤ 1200 ms on a mid-tier device | [Provisional] until profiled |
| SC-E2 | Simulator sustained frame rate, Balanced mode | ≥ 55 fps on a mid-tier device | [Provisional] |
| SC-E3 | Crash-free sessions | ≥ 99.5 % | Measured post-launch |
| SC-E4 | Unit + ViewModel line coverage of `:core` and `:feature` logic | ≥ 70 % | Enforced at Phase 4 exit |
| SC-E5 | Release download size (arm64-v8a split) | ≤ 28 MB | [Provisional] — see [PERFORMANCE_AND_APP_SIZE.md](PERFORMANCE_AND_APP_SIZE.md) |

All numeric targets above are **[Provisional]** until measured on the reference
device set defined in [TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md) §9.

## 8. Non-goals that are easy to drift into

- Becoming a camera app because we hold a `CameraManager`
- Adding widgets, quick-settings tiles or shortcuts before the core is stable
  (good ideas; Phase 5+)
- Physically-based global illumination — Filament gives us image-based lighting and
  dynamic punctual lights, which is an approximation and will be described as one
- A plugin system for 3D models before there is a second source of models

## 9. Definition of "done" for the product

v1.0 ships when every requirement in [PRODUCT_REQUIREMENTS.md](PRODUCT_REQUIREMENTS.md)
marked *Launch* has: an implementation, a passing test, a satisfied acceptance
criterion, and a row in [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) — and when
the release gates in [TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md) §10 are
all green on real devices.
