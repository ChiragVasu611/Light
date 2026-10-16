# KNOWN_LIMITATIONS.md — All-in-One Lighting

Device-dependent behaviour, unresolved questions, and deferred features.
Reviewed at every release gate.

---

## 1. Hardware limitations

### LIM-FLASH-STRENGTH — Torch brightness is unavailable on most devices
Flash strength control needs API 33+ **and** a device reporting
`FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1`. Most devices report `1`, including many on
API 33+. **Consequence:** the majority of users will never see a torch brightness
slider; the concept board's always-present control is not implementable as drawn.
**Handling:** the control is hidden, not disabled (`ADR-018`, `T-TORCH-06`).

### LIM-NO-BEAM-ANGLE — Torch beam angle cannot be controlled
No Android API changes the flash LED's beam angle, and the hardware has no moving
optics. **Handling:** control removed from the Flashlight screen (`ADR-017`); beam
angle exists only in the 3D simulator.

### LIM-CAMERA-CONFLICT — Any app holding the camera blocks the torch
A camera app, a video call, or a background service with camera access will cause
`CameraAccessException.CAMERA_IN_USE` or `onTorchModeUnavailable`.
**Handling:** specific message plus a Retry action; never an auto-retry loop
(`T-TORCH-03`). We cannot force-release another app's camera, and will not try.

### LIM-NO-FLASH-DEVICES — Some devices have no flash at all
Many tablets and some budget phones. **Handling:** `uses-feature` is
`required="false"`; the torch UI shows a disabled state with a reason; every other
feature works (`T-HOME-02`, `AC-HOME-02`).

### LIM-BRIGHTNESS-CAP — Screen brightness is capped by the system
Battery saver and thermal management cap maximum brightness. Our window-brightness
request is honoured only up to the cap. **Handling:** no complaint UI; the slider
represents our request, not a guarantee.

### LIM-SCREEN-VS-FLASH — Screen light is dimmer than the camera flash
Physically true and a frequent source of one-star reviews on competing apps.
**Handling:** an explicit in-app explanation (`FR-SCREEN-08`).

### LIM-OEM-BACKGROUND — Aggressive OEM background management
Xiaomi, Oppo, Vivo and some Samsung builds kill foreground services more eagerly than
AOSP. The optional background-torch service may be terminated.
**Handling:** documented; the foreground torch is unaffected. We do **not** ask users
to disable battery optimisation — that is a hostile pattern for a flashlight app.

### LIM-THERMAL-API — Thermal status unavailable below API 29
`PowerManager.getCurrentThermalStatus()` is API 29+. On API 24–28 we cannot detect
throttling. **Handling:** rely on the per-quality frame cap; no thermal banner on
those devices.

### LIM-GLES3 — The simulator requires OpenGL ES 3.0
Devices below that cannot run Filament. **Handling:** the gallery shows an
unsupported state; the rest of the app is fully usable (`NFR-COMPAT-02`, `T-3D-08`).

---

## 2. Rendering limitations

### LIM-NOT-PHYSICAL — The simulation is an approximation
Filament provides direct punctual lighting plus image-based ambient. There is no path
tracing and no multi-bounce global illumination. **Handling:** the app says
"simulated", never "physically accurate"; the gallery's info sheet states this.

### LIM-BEAM-STYLISED — Beam cones are stylised, not volumetric
The visible cone is an additive cone mesh, not ray-marched participating media.
It is a deliberate, affordable approximation of what users expect to see.

### LIM-TUBE-SEGMENTS — The tube light is segmented point lights
A true area light is not used. In Performance mode (3 segments) the illumination on
the ground plane is visibly scalloped rather than smoothly linear. This is the
documented fallback, not a bug.

### LIM-NO-PHOTOMETRY — Lumen values are plausible, not calibrated
Nothing in the simulator should be used as a measurement, and the UI never presents
it as one.

### LIM-CCT-APPROX — Colour temperature is an sRGB approximation
Correlated colour temperature is mapped to sRGB, not spectrally rendered.

### LIM-FILAMENT-NATIVE — Native crashes bypass Kotlin error handling
A misuse of the Filament API (wrong thread, wrong teardown order, R8-stripped JNI
targets) crashes in native code where no `try/catch` helps.
**Handling:** all engine calls on the main thread; a single teardown sequence in
`FilamentEngineHost.release()`; mandatory R8 keep rules; a release-build smoke test
(`T-REL-01`) that would catch a stripped-symbol crash a debug build never shows.

---

## 3. Platform limitations

### LIM-SPLITSCREEN — Full-screen light fills only our window
In split-screen or freeform, the light occupies our window, not the display.
Expected platform behaviour; no workaround attempted.

### LIM-PROCESS-DEATH-TORCH — The OS turns the torch off on process death
We cannot and should not prevent this. On relaunch we read the real state from the
torch callback rather than assuming anything.

### LIM-NO-DEEPLINKS — No external deep links in v1.0
Only `LAUNCHER` is declared. In-app route arguments are not `intent-filter` deep
links. Quick-settings tiles, widgets and app shortcuts are deferred.

### LIM-NO-BACKUP — App data is not backed up
`allowBackup="false"` (`ADR-019`). Users changing devices re-pick their preferences.

---

### Found and fixed on real hardware

| Date | Finding | Resolution |
|---|---|---|
| 2026-10-16 | The Medium-width rule in `UI_DESIGN_SYSTEM.md` §8 (cap single-column content at 560 dp and centre it) had never been implemented. On a 621 dp tablet, list rows stretched edge to edge with a long dead run to the trailing chevron. | `ScreenContent` added and applied to every single-column screen; `ScreenContentTest` keeps the rule enforced. |
| 2026-10-16 | Onboarding copy read "Use your **phone's** flashlight" — wrong on a tablet, and doubly so on one with no flash unit. | Copy made device-neutral. A capability-aware variant is a Phase 2 concern, once `DeviceCapabilities` exists. |
| 2026-10-16 | `ComingSoonBadge` cleared its semantics unconditionally, so TalkBack never announced "Coming soon" on the Smart Light screen. | Announced by default; `AppRow` opts out because it folds the text into its own label. |
| 2026-10-16 | White on the primary blue measured 3.89:1, below the 4.5:1 body minimum, on the fill behind every button label. | `Blue500` darkened `#2E7DF7` → `#1F6FE8` (4.67:1). |

## 4. Open questions

Each has a recommended default so work is never blocked, but each needs a product
decision before the phase that depends on it.

### OQ-01 — Is billing in scope? *(blocks Phase 5)*
The board shows Go Pro (₹299) and an asset store (₹99/₹149); the brief says not to add
payment complexity to the first release unless explicitly approved, and forbids
hard-coded prices.
**Recommended default:** no billing in v1.0. Ship the free core, measure retention,
then decide. If approved, prices come from `ProductDetails`, never from a literal.
**Owner:** product. **Needed by:** start of Phase 5.

### OQ-02 — Does Music Light ship, and is `RECORD_AUDIO` acceptable?
Music-reactive lighting needs microphone access, which is a dangerous permission, a
Data Safety disclosure, and a meaningful trust cost for a flashlight app.
**Recommended default:** do not ship it. The trust cost exceeds the feature's value.
If it ships, it must be a clearly separate, opt-in feature that requests the
permission only at the moment of use and works without it everywhere else.
**Owner:** product. **Needed by:** Phase 5.

### OQ-03 — Does Party Mode ship without music reactivity?
Without audio input it is a colour-cycling animation.
**Recommended default:** defer. Revisit after launch feedback.
**Owner:** product. **Needed by:** Phase 5.

### OQ-04 — Is 5 Hz the right strobe clamp?
3 Hz is the common photosensitivity threshold for general content; 5 Hz is more useful
for signalling. We chose 5 Hz plus an explicit one-time warning (`ADR-008`).
**Recommended default:** keep 5 Hz with the warning. If any reviewer or regulator
objects, drop to 3 Hz — the code reads the limit from one constant.
**Owner:** product + legal. **Needed by:** Phase 2 exit.

### OQ-05 — Should the background-torch foreground service ship at all?
It is the app's only service and the only reason `POST_NOTIFICATIONS` would ever be
requested.
**Recommended default:** ship it as an opt-in, default-off setting. Users genuinely
want a torch that survives switching apps.
**Owner:** product. **Needed by:** Phase 2.

### OQ-06 — Filament OpenGL backend or Vulkan?
**Recommended default:** OpenGL ES for the widest driver compatibility in v1.0.
Revisit only if profiling shows a GL-specific bottleneck.
**Owner:** engineering. **Needed by:** Phase 3.

### OQ-07 — Can `gltfio` be dropped to save ~3 MB?
`gltfio-android` is the largest single native artifact (3.09 MB on arm64). Loading
pre-converted `.filamesh` assets with `filament-utils` alone would avoid it, at the
cost of a build-time conversion pipeline and losing glTF's material fidelity.
**Recommended default:** keep `gltfio`. Only revisit if the measured download size
exceeds `NFR-SIZE-01` — **measure before optimising**.
**Owner:** engineering. **Needed by:** Phase 5 size review.

### OQ-08 — Which smart-light ecosystem is the first integration?
Options compared in `SMART_LIGHT_FUTURE_SPEC.md` §2.
**Recommended default:** Philips Hue (local HTTP, well documented, testable) as the
first adapter; Matter as the strategic target.
**Must not be decided without owning the physical hardware.**
**Owner:** product + engineering. **Needed by:** Phase 6.

### OQ-09 — Local-network permission requirements on the then-current target SDK
Android's handling of local-network access is evolving. Phase 6 must verify current
requirements rather than assuming today's rules.
**Recommended default:** verify at the start of Phase 6, before designing the pairing
flow. **Owner:** engineering. **Needed by:** Phase 6.

### OQ-10 — Which locales ship at launch?
Affects font subsetting, string budget and RTL testing.
**Recommended default:** English only at launch; the architecture is
localisation-ready (`FR-GLOBAL-03`, no text in images) and RTL is tested
(`T-A11Y-06`). **Owner:** product. **Needed by:** Phase 5.

### OQ-11 — Who produces the 3D assets? *(blocks Phase 3)*
Five models must satisfy the `LIGHT_ANCHOR` convention (`ADR-014`), triangle budgets,
and recorded licences. Commissioned, purchased, or sourced CC0 — all are viable, none
is decided.
**Recommended default:** source CC0 base meshes and adapt them to the convention,
recording provenance per `THREE_D_LIGHTING_SPEC.md` §10.
**Owner:** product. **Needed by:** start of Phase 3. **This is the hardest external
dependency in the plan.**

---

## 5. Deferred features

| Feature | Deferred to | Why |
|---|---|---|
| Smart-light control | Phase 6 | Needs a chosen ecosystem and real hardware (`OQ-08`) |
| In-app purchases / Go Pro | Phase 5, if `OQ-01` approves | Brief forbids payment complexity in v1.0 |
| 3D asset store | Phase 5+ | Depends on billing and hosted assets |
| Music Light | Deferred | `RECORD_AUDIO` trust cost (`OQ-02`) |
| Party Mode | Deferred | Low value without music reactivity (`OQ-03`) |
| Emergency Beacon 3D object | Phase 5 | Needs animated rotating reflector (`ADR-012`) |
| Screen Light 3D object | Indefinite | Adds nothing over the real Screen Light screen |
| Light theme | Indefinite | `ADR-010` |
| Quick-settings tile, widget, app shortcuts | Phase 5+ | Core stability first |
| Deep links | Phase 5+ | No use case yet |
| Analytics / crash SDK | Not planned | Play vitals preferred (`DATA_PRIVACY_AND_SECURITY.md` §8) |
| Cloud sync, accounts | Not planned | No feature needs them |
| Play Asset Delivery | Phase 5, if size demands | Measure first (`OQ-07`) |

---

## 6. Unverified claims

Everything here is a target or a projection until measured. Tracked so none of it is
mistaken for fact.

| Claim | Status | Verified by |
|---|---|---|
| Cold start ≤ 1200 ms (Mid) | `[Provisional]` | `T-PERF-01` |
| ≥ 55 fps Balanced (Mid) | `[Provisional]` | `T-PERF-02` |
| Model load ≤ 1500 ms | `[Provisional]` | `T-PERF-03` |
| Peak PSS ≤ 320 MB | `[Provisional]` | `dumpsys meminfo` |
| Download ≤ 28 MB | `[Provisional]` | `bundletool get-size` |
| Installed ≤ 75 MB | `[Provisional]` | device measurement |
| Filament native payload 7.43 MB (arm64) | **Verified 2026-10-15** | read from the published AARs |
| Filament 1.77.3 is current stable, `minSdk 21` | **Verified 2026-10-15** | Maven Central metadata + AAR manifest |
| Dependency versions in `TECHNICAL_ARCHITECTURE.md` §1 | **Verified 2026-10-15** | Maven metadata queries |
| Contrast ratios in `UI_DESIGN_SYSTEM.md` §2.4 | Computed, not yet asserted | `T-A11Y-02` |
| Triangle budgets per object | Target | asset review at Phase 3 |

---

## 7. Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| 3D assets unavailable or non-conforming | Medium | **Blocks Phase 3** | `OQ-11` resolved early; `LIGHT_ANCHOR` validation at load |
| AGP 9.x / Gradle 9.x friction | Medium | Delays Phase 1 | Build validated as task 1.1 before any feature work |
| Filament release-only crash via R8 | Medium | Ships broken | Keep rules from day one; `T-REL-01` on a release build |
| Native memory leak in the simulator | Medium | Device degradation | Single teardown path; `T-3D-04` over 20 cycles |
| Download size exceeds budget | Medium | Store friction | Levers listed in order; `OQ-07` as the large lever |
| Device-specific torch behaviour | High | User-visible bugs | Fakes for logic; real-device matrix for hardware |
| Scope creep from the concept board's unshipped screens | Medium | Schedule | Deviations fixed in `ADR-*`; `FR-GLOBAL-01` audited at every gate |
