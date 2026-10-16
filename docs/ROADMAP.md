# ROADMAP.md — All-in-One Lighting

Gated phases. **A phase does not start until the previous phase's exit criteria are
met and evidenced.** Evidence means command output, test reports, and measurements —
not assertions.

| Phase | Theme | Status |
|---|---|---|
| 0 | Repository and documentation | ✅ Complete |
| 1 | Application foundation | ▶ In progress — build, design system, navigation, onboarding, settings done and verified on device |
| 2 | Core practical lighting | Not started |
| 3 | Initial 3D simulator | Not started |
| 4 | Quality and reliability | Not started |
| 5 | Optional content and release prep | Not started |
| 6 | Smart-light integration | Not started |

---

## Phase 0 — Repository and documentation ✅

**Prerequisites:** none.

**Tasks**
- [x] Inspect the environment: JDK, Gradle, Android SDK, build tools
- [x] Verify dependency versions against Maven Central / Google Maven
- [x] Verify Filament's current Android availability, minSdk and native payload size
- [x] Review the supplied UI concept board
- [x] Write the 17-document specification package
- [x] Record decisions, assumptions and open questions

**Deliverables:** `docs/` complete; `DECISIONS.md` and `KNOWN_LIMITATIONS.md` seeded.

**Exit criteria**
- [x] All 17 documents exist with actionable content
- [x] Every screen has states and acceptance criteria
- [x] Every requirement maps to a test in the traceability matrix
- [x] Practical hardware lighting and simulated 3D lighting are architecturally distinct
- [x] Smart-light work is planned and deferred
- [x] Assumptions and open questions are listed

---

## Phase 1 — Application foundation ▶

**Goal:** a building, testable, navigable app shell with the design system in place
and no feature logic. Nothing in this phase touches hardware or Filament.

**Prerequisites:** Phase 0 exit criteria met. JDK 17 and Android SDK present.

### Tasks

| # | Task | Deliverable |
|---|---|---|
| 1.1 | Gradle project skeleton: wrapper 9.8.1, settings, version catalog, `:app` module, `local.properties` guidance, `.gitignore` | `./gradlew assembleDebug` succeeds |
| 1.2 | Manifest, `Application`, `MainActivity` with edge-to-edge, splash screen API | App launches to a blank themed screen |
| 1.3 | Design system: `Color`, `Type`, `Shape`, `Spacing`, `Radius`, `Motion`, `Elevation`, `AppTheme` | Tokens match `UI_DESIGN_SYSTEM.md` §2–4 |
| 1.4 | Core: `AppResult`, `AppError`, `AppLogger`, `DispatcherProvider`, `TimeSource` | Unit-tested |
| 1.5 | `AppContainer` + `DefaultAppContainer` + ViewModel factory helper | `FakeAppContainer` available to tests |
| 1.6 | `PreferencesRepository` over DataStore with corruption handler and defaults | Unit-tested with a fake |
| 1.7 | Reusable components: `CMP-CARD-HERO`, `CMP-CARD-TOOL`, `CMP-ROW`, `CMP-SEGMENTED`, `CMP-CHIP`, `CMP-SLIDER`, `CMP-POWER`, `CMP-BANNER`, `CMP-BADGE`, `CMP-LOADING`, `CMP-EMPTY`, `CMP-ERROR`, `CMP-DIALOG`, `CMP-BOTTOMNAV` | Each with a preview and a UI test |
| 1.8 | Navigation: typed routes, `NavHost`, bottom-nav with per-tab back stacks | `T-NAV-01/02` pass |
| 1.9 | Screen scaffolding: every screen in the graph exists, renders a titled placeholder, and is reachable | Navigation tests pass |
| 1.10 | Onboarding implemented end to end (it needs no hardware) | `T-ONB-01..06` pass |
| 1.11 | Settings, About, Licences, Smart Light screens (static content) | `T-SET-*`, `T-SMART-*` pass |
| 1.12 | Static checks: `T-ARCH-01`, `T-DS-02`, `T-GLOBAL-02/04`, `T-DEP-01`, `T-LOG-01`, `T-PERM-01` | All pass in CI |
| 1.13 | String resources for everything built so far | `T-GLOBAL-02` passes |

### Tests added in Phase 1

`T-ONB-01..06`, `T-NAV-01..03`, `T-SET-01..05`, `T-SMART-01..03`, `T-DS-01`,
`T-DS-02`, `T-ARCH-01`, `T-GLOBAL-02`, `T-GLOBAL-03`, `T-GLOBAL-04`, `T-DEP-01`,
`T-LOG-01`, `T-PERM-01`, `T-PERM-02`, `T-A11Y-01..03` (for components built).

### Exit criteria — status 2026-10-16

- [x] `./gradlew assembleDebug` succeeds
- [x] `./gradlew assembleRelease` succeeds with R8
- [x] `./gradlew testDebugUnitTest` — **26 tests, 0 failures**
- [x] `./gradlew connectedDebugAndroidTest` — **11 tests, 0 failures, on a real device** (OPPO CPH2127, Android 12)
- [x] `./gradlew lintDebug` — zero errors
- [x] Design tokens match `UI_DESIGN_SYSTEM.md`; `T-DS-01` passes
- [x] Onboarding works end to end and persists its flag (confirmed by hand on device: relaunch skips onboarding)
- [x] No hardware or Filament code exists yet
- [x] Traceability matrix updated
- [ ] **Every screen in the navigation graph is reachable and renders** — partial, see below

**Remaining before Phase 1 can be declared complete:**

| # | Item |
|---|---|
| 1.8b | Route arguments not yet wired: `SCREEN_LIGHT?mode=`, `EMERGENCY?pattern=`, `SIM_VIEWER/{objectId}` |
| 1.9b | Home is an honest placeholder; its real layout (hero card + 2×2 grid + rows) lands with torch control in Phase 2 |
| 1.7b | Components written but not yet exercised by a UI test: `HeroCard`, `ToolCard`, `SegmentedControl`, `PowerToggle`, `AppBanner`, `LoadingState` |
| 1.11b | Display settings and Licences screens are placeholders |
| — | Onboarding illustrations not yet added (pages render text-only, leaving a visible gap) |

Nothing above blocks Phase 2 design work, but Phase 1 is **not** signed off until
1.8b and 1.7b are done.

**Dependencies:** none beyond the toolchain.
**Risk:** AGP 9.4.1 is a recent major release; DSL changes may require adjustment.
Mitigated by validating the build in task 1.1 before any other work.

---

## Phase 2 — Core practical lighting

**Goal:** real torch, real screen light, real emergency patterns, with every failure
path handled.

**Prerequisites:** Phase 1 exit criteria met.

### Tasks

| # | Task | Deliverable |
|---|---|---|
| 2.1 | `DeviceCapabilities` + `AndroidDeviceCaps` (flash, strength, GL ES, RAM) | Unit-tested via fake |
| 2.2 | `TorchController` interface + `CameraTorchController` with `TorchCallback` as the state source | `T-TORCH-01..09` |
| 2.3 | `FakeTorchController` with scriptable states and failures | Used by all torch tests |
| 2.4 | Home flashlight card wired to real state, all 7 status variants | `T-HOME-01..03` |
| 2.5 | `SCR-FLASHLIGHT` with conditional strength slider and all error states | `T-TORCH-02..08` |
| 2.6 | `ScreenLightController` + `WindowScreenLight` (window brightness, keep-screen-on) | `T-SCREEN-02/06` |
| 2.7 | `SCR-SCREENLIGHT`: colour wheel, presets, white ramp, brightness, auto-hide | `T-SCREEN-01..08` |
| 2.8 | `SCR-CANDLE` with bounded flicker | `T-CANDLE-01/02` |
| 2.9 | `PatternScheduler` with injected `TimeSource`; SOS/Strobe/Blink/Steady | `T-EMER-01/02/08` |
| 2.10 | `SCR-EMERGENCY`: output selector, speed, STOP, photosensitivity gate, disclaimer | `T-EMER-03..07/09` |
| 2.11 | `SCR-TOOLS` with the five launch rows and deep-link arguments | `T-TOOLS-01/02` |
| 2.12 | Lifecycle wiring for every light; process-death behaviour | `T-LIFE-01/02/04` |
| 2.13 | Optional background-torch foreground service + `POST_NOTIFICATIONS` gate | `T-PERM-03` |

### Exit criteria

- [ ] Torch verified to produce physical light on ≥ 2 real devices
- [ ] Torch verified to show the unavailable state on a device with no flash
- [ ] All `T-TORCH-*`, `T-SCREEN-*`, `T-EMER-*`, `T-CANDLE-*`, `T-TOOLS-*` pass
- [ ] `T-GLOBAL-01` control audit passes for all Phase 2 screens
- [ ] No permission is requested beyond the approved table (`T-PERM-01`)
- [ ] SOS timing verified against the documented table
- [ ] Strobe rate verified ≤ 5 Hz
- [ ] Build, unit, instrumented, lint all green — output recorded

**Dependencies:** Phase 1 components and navigation.
**Risk:** device-specific torch behaviour. Mitigated by fakes for logic plus the
real-device matrix for hardware.

---

## Phase 3 — Initial 3D simulator

**Goal:** Filament integrated, five objects, correct light synchronisation, two
environments, three quality modes.

**Prerequisites:** Phase 2 exit criteria met. Approved 3D assets with recorded
licences.

### Tasks

| # | Task | Deliverable |
|---|---|---|
| 3.1 | Add Filament 1.77.3 (3 artifacts); R8 keep rules; lazy native load | `assembleRelease` + `T-REL-01` |
| 3.2 | Pure math: `Quaternion`, `Mat4`, `OrbitController`, `LightRig.worldDirectionOf` | `T-3D-01/06/13/16` (JVM, no GPU) |
| 3.3 | `FakeRenderer` recording scene-graph ops | Enables state-holder tests |
| 3.4 | `FilamentEngineHost`: create/destroy in the documented order | `T-3D-04/17` |
| 3.5 | `ModelLoader` via `gltfio`, IO off-main, handoff on-main, `LIGHT_ANCHOR` validation | `T-3D-05/15` |
| 3.6 | `SceneBuilder`: ground plane, backdrop, exposure, IBL/skybox per environment | `T-3D-03` |
| 3.7 | `LightRig`: spot/point/segmented-linear creation parented to `LIGHT_ANCHOR` | `T-3D-02/14` |
| 3.8 | Assets: 5 `.glb`, thumbnails, 2 IBL sets, beam + flame materials, `catalog.json` | Licences recorded |
| 3.9 | `SCR-SIM-GALLERY` with filters and unsupported-device state | `T-3D-08` |
| 3.10 | `SCR-SIM-VIEWER`: viewport, gestures, object-aware controls, reset | `T-3D-07/13` |
| 3.11 | `QualityProfile` + `DeviceClassifier` + automatic degradation | `T-3D-09/10/11/12` |
| 3.12 | Beam cone visualisation | visual review |
| 3.13 | Per-object persistence with range coercion | `T-3D-18`, `T-LIFE-03` |
| 3.14 | Viewport accessibility actions | `T-A11Y-07` |

### Exit criteria

- [ ] All five objects load, rotate, and illuminate the scene on a real device
- [ ] `T-3D-01` proves beam direction follows torch rotation
- [ ] `T-3D-02` proves the bulb never becomes a spot
- [ ] `T-3D-03` proves environment switching preserves settings
- [ ] `T-3D-04` proves no native growth over 20 cycles
- [ ] `T-REL-01` passes on a **release** build on a real device
- [ ] Simulator unavailable state verified on a device/emulator without GL ES 3.0
- [ ] Frame rate measured on Low and Mid devices; result recorded even if below target
- [ ] All asset licences recorded in `docs/licences/assets.md`

**Dependencies:** approved assets. **This phase cannot start without them** — a
placeholder cube does not validate `LIGHT_ANCHOR` conventions or triangle budgets.
**Risk:** Filament native crashes, R8 stripping. Mitigated by strict teardown
ordering, keep rules, and a release-build smoke test from day one of the phase.

---

## Phase 4 — Quality and reliability

**Goal:** make it fast, accessible, and trustworthy on real hardware.

**Prerequisites:** Phase 3 exit criteria met.

### Tasks

| # | Task | Deliverable |
|---|---|---|
| 4.1 | `:benchmark` module; `StartupBenchmark`, `FrameTimingBenchmark`, model-load benchmark | `T-PERF-01..03` |
| 4.2 | Baseline Profile generation and wiring | measured startup improvement |
| 4.3 | Profile and fix recomposition hotspots | before/after counts recorded |
| 4.4 | Memory profiling; fix any native growth | `dumpsys meminfo` recorded |
| 4.5 | Battery session via Battery Historian | no wakelocks/background work |
| 4.6 | Full accessibility sweep: TalkBack, font scale 200 %, RTL, reduce motion | `T-A11Y-01..07` |
| 4.7 | Real-device matrix execution (≥ 4 devices incl. one with no flash) | signed checklist |
| 4.8 | Lifecycle and process-death hardening | `T-LIFE-01..04` |
| 4.9 | Coverage to the §6 targets | coverage report |
| 4.10 | Replace every `[Provisional]` with a measurement or an explicit deviation | updated docs |

### Exit criteria

- [ ] Coverage ≥ 70 % overall, ≥ 90 % in `domain` and `render` math
- [ ] All performance targets measured; deviations documented in `KNOWN_LIMITATIONS.md`
- [ ] Device matrix complete and signed off
- [ ] Zero lint errors; zero static-check violations
- [ ] `T-GLOBAL-01` control audit: zero no-op controls across the whole app
- [ ] Crash-free across the full manual matrix

---

## Phase 5 — Optional content and release preparation

**Prerequisites:** Phase 4 exit criteria met.

### Tasks

| # | Task | Gate |
|---|---|---|
| 5.1 | Measure release download and installed size (`PERFORMANCE_AND_APP_SIZE.md` §7) | `T-REL-02` |
| 5.2 | Apply size levers if over budget; re-measure | recorded |
| 5.3 | **Decide `OQ-01`** (billing) with the product owner | ADR |
| 5.4 | If approved: Play Billing, entitlements, restore, pending/cancelled/network states | tests |
| 5.5 | **Decide `OQ-02`** (Music Light / `RECORD_AUDIO`) | ADR |
| 5.6 | Optional extra 3D objects (emergency beacon, lantern…) if approved | licences recorded |
| 5.7 | Evaluate Play Asset Delivery if size demands it | ADR |
| 5.8 | Signing config, release notes, store listing, Data Safety form | review |
| 5.9 | Privacy policy final review against actual behaviour | review |
| 5.10 | Pre-launch report on Play Console; internal testing track | report |

### Exit criteria

- [ ] All 16 release gates in `TESTING_AND_ACCEPTANCE.md` §10 green
- [ ] Download size measured and within budget, or a documented deviation
- [ ] Data Safety declaration matches actual behaviour
- [ ] No `[Open]` question blocks launch

---

## Phase 6 — Smart-light integration

**Prerequisites:** v1.0 shipped and stable (crash-free ≥ 99.5 %), plus all five entry
criteria in [SMART_LIGHT_FUTURE_SPEC.md](SMART_LIGHT_FUTURE_SPEC.md) §10 — including
**physically owning the target hardware**.

### Tasks

| # | Task |
|---|---|
| 6.1 | Resolve `OQ-08`: choose the ecosystem, with hardware in hand |
| 6.2 | Resolve `OQ-09`: verify local-network/permission requirements on the current target SDK |
| 6.3 | Implement exactly one `SmartLightProvider` adapter |
| 6.4 | Keystore-backed credential storage |
| 6.5 | Discovery, pairing, connection-state and capability-driven UI |
| 6.6 | Reconnection with backoff; failure states |
| 6.7 | Fake-driven tests + `MockWebServer` + golden payloads |
| 6.8 | Manual hardware checklist |
| 6.9 | Privacy policy + Data Safety update |
| 6.10 | Replace "Coming Soon" only when the adapter genuinely works |

### Exit criteria

- [ ] One ecosystem controlled end to end on real hardware
- [ ] Every capability the UI exposes is backed by `DeviceCapabilities`
- [ ] No credential in logs, source, or plaintext storage
- [ ] Privacy and Data Safety updated **before** release
- [ ] "Coming Soon" removed only from the paths that actually work

---

## Cross-phase definition of done

A task is done when **all** of these hold:

1. Code implements the documented behaviour — no placeholder, no stub presented as working.
2. Tests added or updated, and **executed**, with output reported.
3. `assembleDebug` succeeds.
4. Relevant test command succeeds — actual output, not a claim.
5. Lint produces no new errors.
6. `TRACEABILITY_MATRIX.md` updated.
7. Any deviation from spec recorded in `DECISIONS.md`.
8. Any new limitation recorded in `KNOWN_LIMITATIONS.md`.
9. The task report ([CLAUDE_CODE_IMPLEMENTATION_GUIDE.md](CLAUDE_CODE_IMPLEMENTATION_GUIDE.md) §6) is produced.

---

## Dependency graph

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6
             │            │           │                        │
             │            │           └── needs approved 3D assets + licences
             │            └── needs real devices with and without flash
             └── needs JDK 17 + Android SDK (verified present)
```

Phase 3 is the only phase with an external blocker (assets). If assets are delayed,
Phase 4's non-3D work (accessibility, startup profiling, device matrix for Phase 2
features) can proceed in parallel — but Phase 3 cannot be declared complete with
placeholder geometry.
