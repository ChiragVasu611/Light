# DECISIONS.md — All-in-One Lighting

Architecture decision records. Every departure from the concept board or from an
obvious default is recorded here with its reasoning.

**Format:** context → decision → consequences → status.

---

## ADR-001 — Single Gradle module

**Context.** The brief says to use a modular architecture only where it has practical
benefits and to avoid unnecessary complexity for a single-purpose application.

**Decision.** One `:app` module (plus `:benchmark` from Phase 4). Layering is enforced
by package structure and a dependency-direction test (`T-ARCH-01`) rather than by
Gradle module boundaries.

**Consequences.** Faster builds, simpler configuration, no inter-module API ceremony.
We lose compile-time enforcement of layering — replaced by `T-ARCH-01`.
**Revisit when:** Play Asset Delivery is adopted (Phase 5), or a second consumer of the
domain layer appears.

**Status:** Accepted.

---

## ADR-002 — minSdk 24, compileSdk 36, targetSdk 36

**Context.** Filament's AAR declares `minSdkVersion 21` (verified from the 1.77.3
artifact). `setTorchMode` and `TorchCallback` require API 23.

**Decision.** `minSdk 24`, `compileSdk 36`, `targetSdk 36`.

**Consequences.** 24 clears all our API requirements with margin and avoids the
pre-Nougat graphics driver tail, at a negligible device-coverage cost. Flash-strength
APIs (33+) and thermal APIs (29+) are version-gated behind `DeviceCapabilities`.
`targetSdk` must be re-verified against Play's then-current minimum before submission.

**Status:** Accepted.

---

## ADR-003 — Manual DI instead of Hilt

**Context.** The brief says to use dependency injection only where it improves
maintainability. The object graph is roughly a dozen entries.

**Decision.** An `AppContainer` interface with constructor injection and a ViewModel
factory helper. No Hilt, no Dagger, no KSP.

**Consequences.** No annotation processor, no KSP↔Kotlin version coupling, faster
builds, a graph readable in one file. Tests substitute a `FakeAppContainer`. We give
up scoped-graph tooling we do not need.
**Revisit when:** the graph exceeds ~25 entries or needs scopes beyond
process/ViewModel.

**Status:** Accepted.

---

## ADR-004 — The torch is a process singleton whose state comes from the OS

**Context.** `FR-GLOBAL-02` forbids showing a hardware state that is not real. Two
screens display torch state.

**Decision.** `CameraTorchController` is a process-scoped singleton. Its displayed
state is written **only** by `CameraManager.TorchCallback`, never by our own command.
Failures are recorded separately as `lastError`.

**Consequences.** Home and Flashlight cannot disagree. External torch changes
propagate automatically. Rotation cannot toggle the torch. A successful-looking UI for
a failed call becomes structurally impossible rather than a rule someone must remember.
Cost: a brief "Checking…" state on first launch while the callback delivers initial
state.

**Status:** Accepted.

---

## ADR-005 — Window brightness, never system brightness

**Context.** Screen Light needs to control brightness. Two mechanisms exist.

**Decision.** `WindowManager.LayoutParams.screenBrightness` only. Never
`Settings.System.SCREEN_BRIGHTNESS`, never `WRITE_SETTINGS`.

**Consequences.** No permission request, no special-access flow, nothing to restore on
exit (the OS reverts when the window goes away), and no way for a crash to strand the
user with an unusable display. We cannot exceed the system's battery-saver brightness
cap — acceptable, and we do not pretend otherwise. Enforced by `T-SCREEN-02`.

**Status:** Accepted.

---

## ADR-006 — No `CAMERA` permission

**Context.** The concept board shows a camera-permission dialog for the flashlight.

**Decision.** Do not request `CAMERA`. `CameraManager.setTorchMode()` controls the
torch without it.

**Consequences.** One fewer dangerous permission, a cleaner Data Safety declaration,
less user friction, and the permission dialog from the board is removed. Enforced by
`T-PERM-01`.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-007 — Low battery warns, never blocks

**Context.** The board shows a blocking "Low Battery" dialog with an OK button.

**Decision.** A non-blocking `CMP-BANNER` on Home below 5 %. The torch stays fully
functional.

**Consequences.** A user at 3 % battery in the dark is exactly the user who needs a
flashlight most. Blocking them would be the worst possible moment to be unhelpful. The
warning still informs.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-008 — Strobe clamped to 5 Hz behind a one-time warning

**Context.** The board offers Fast/Medium/Slow with no defined rates. Flash rates
roughly 3–30 Hz can provoke photosensitive seizures; ≤ 3 Hz is the common threshold
for general content.

**Decision.** Hard clamp at 5 Hz for every pattern. A one-time, explicit
photosensitivity warning before the first strobe session, acknowledged once per
install. SOS uses fixed ITU-R M.1677 timing.

**Consequences.** Slightly less dramatic than an unclamped strobe. A deliberate
signalling tool justifies 5 Hz over the 3 Hz general-content threshold, but only with
informed consent. Enforced by `T-EMER-02` and `T-EMER-06`.

**Status:** Accepted. **Extends the concept board.**

---

## ADR-009 — No Audio settings group; Permissions screen rewritten

**Context.** The board's Settings shows "Audio — Sounds & Vibration" and
"Permissions — Camera, Storage, Location".

**Decision.** Remove the Audio group entirely. Replace the Permissions row with a
screen explaining that the app requests no runtime permissions.

**Consequences.** The app emits no sound and has no vibration feature, so an Audio
group would be a set of controls over nothing — a direct `FR-GLOBAL-01` violation.
Likewise the app requests none of camera, storage or location. The rewritten screen
turns a dead row into a trust-building one.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-010 — Dark theme only

**Context.** The board is entirely dark. Material 3 would normally imply both themes.

**Decision.** Ship dark-only in v1.0. No light `colorScheme`.

**Consequences.** Halves the design, token and visual-test surface. A lighting tool is
used in the dark; a light theme would be rarely seen and expensive to maintain. Users
who prefer light themes are not blocked from anything. Revisit if user feedback
demands it.

**Status:** Accepted.

---

## ADR-011 — Unshipped tools are hidden, not disabled

**Context.** Party Mode and Music Light appear on the board's More Tools list but are
not in v1.0.

**Decision.** Omit them from the list entirely rather than rendering disabled rows.

**Consequences.** A disabled row invites repeated tapping and implies a near-term
arrival we have not committed to. An absent row is honest. The Smart Lights entry is
the one deliberate exception — it is a *strategic* announcement with a "Coming Soon"
badge and a real informational destination, not a dead control.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-012 — Five 3D objects in v1.0, not six or seven

**Context.** The brief lists seven objects; the board's grid shows six.

**Decision.** Ship Torch, Bulb, Tube Light, Candle, LED Lamp. Defer Screen Light
(as a 3D object) and Emergency Beacon.

**Consequences.** Screen Light as a 3D object would be a flat emissive quad that adds
nothing over the real Screen Light screen. The Emergency Beacon needs an animated
rotating reflector — real asset and animation work, better done once the pipeline is
proven. Saves asset budget and grid noise. The beacon's full spec is retained in
`THREE_D_LIGHTING_SPEC.md` §4 for Phase 5.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-013 — Google Filament 1.77.3, used directly

**Context.** Need a real-time PBR renderer with dynamic lights, IBL and glTF loading.

**Decision.** Filament 1.77.3 (`filament-android`, `gltfio-android`,
`filament-utils-android`), used directly rather than through SceneView.

**Consequences.** Full control of the light rig, scene graph and lifecycle — all of
which this feature depends on. We own more boilerplate and must respect Filament's
main-thread and teardown-ordering constraints. SceneView would add an abstraction we
would fight for custom light parenting, and would couple our Filament version to
theirs.

Verified 2026-10-15: latest stable 1.77.3, AAR `minSdkVersion 21`, arm64-v8a native
payload 7.43 MB across the three artifacts.

**Status:** Accepted.

---

## ADR-014 — One coordinate convention: Y-up, −Z forward, metres

**Context.** Light-to-object synchronisation fails when models disagree about which
way is forward.

**Decision.** Right-handed, Y-up, **−Z forward**, metres, pivot at origin, unit scale.
Every model contains a node named `LIGHT_ANCHOR` at the emission point oriented so its
−Z is the emission direction. A model that does not comply is fixed as an asset, never
compensated for in code.

**Consequences.** Matches glTF 2.0, so no axis fix-up at load. Light attachment becomes
data-driven: `LightRig` looks up `LIGHT_ANCHOR` and parents the light to it. A missing
anchor is a validation error with a recoverable UI state (`T-3D-15`).

**Status:** Accepted.

---

## ADR-015 — Unlimited yaw, clamped pitch

**Context.** The brief asks how users reach a full 360° view without unexpected flips.

**Decision.** Horizontal drag yaws about +Y with no limit (wraps through 360°).
Vertical drag pitches about the camera right axis, clamped to ±85° (±35° for the
candle). Rotation is stored as a clamped Euler pair and converted to a quaternion each
update.

**Consequences.** The user reaches every azimuth without ever crossing a pole, where
yaw and roll degenerate and the object appears to flip. Storing Euler and converting
once avoids drift from accumulated quaternion multiplication and makes the clamp
trivial. The candle's tighter clamp prevents its billboard flame from revealing itself.

**Status:** Accepted.

---

## ADR-016 — No billing, no asset store, no prices in v1.0

**Context.** The board shows a Go Pro screen at ₹299 and an asset store at ₹99/₹149.
The brief forbids hard-coded prices and says not to add payment complexity to the first
release unless explicitly approved.

**Decision.** No billing dependency, no purchase UI, no price string in v1.0.

**Consequences.** Removes an entire category of failure (purchase verification,
restore, pending state, entitlement drift) from the first release. If billing is
approved in Phase 5, prices come from `ProductDetails`, never from a literal.
Enforced by `T-MON-01`. Tracked as `OQ-01`.

**Status:** Accepted pending `OQ-01`. **Deviates from the concept board.**

---

## ADR-017 — Flashlight "Beam Mode" removed

**Context.** The board's Flashlight screen shows Narrow/Medium/Wide.

**Decision.** Remove it. Android exposes no API to change the flash LED's beam angle,
and the hardware has no moving optics.

**Consequences.** The control would have been decorative — a direct `FR-GLOBAL-01`
violation. Beam angle survives in the 3D simulator, where it is a genuine property of a
simulated spot light.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-018 — Torch brightness hidden, not disabled, when unsupported

**Context.** Flash strength needs API 33+ **and** a device reporting
`FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1`. Most devices report 1.

**Decision.** Compose the slider only when both conditions hold. Otherwise it is
absent from the layout.

**Consequences.** A disabled slider implies the feature exists and is temporarily
unavailable; absence is truthful. Most users will never see it, so the board's
always-present brightness control is not implementable as drawn. Enforced by
`T-TORCH-06`; limitation recorded as `LIM-FLASH-STRENGTH`.

**Status:** Accepted. **Deviates from the concept board.**

---

## ADR-019 — `allowBackup="false"`

**Context.** The app stores only trivial preferences.

**Decision.** Disable Android auto-backup.

**Consequences.** Removes a class of data-at-rest and restore-correctness questions for
zero meaningful user cost — a user changing phones re-picks a colour.

**Status:** Accepted.

---

## ADR-020 — Errors use banners and full-screen states, never snackbars

**Context.** Material offers snackbars for transient messages.

**Decision.** Errors render as `CMP-BANNER` (inline, persistent) or `CMP-ERROR`
(full-screen, recoverable). Snackbars are reserved for transient *confirmations*.

**Consequences.** A snackbar can be missed, auto-dismisses, and cannot hold a
persistent state like "camera in use". Every error stays visible until its cause is
resolved, and carries exactly one primary recovery action.

**Status:** Accepted.

---

## Deviations summary

Changes from the supplied concept board, with their ADR:

| Board element | Change | ADR |
|---|---|---|
| Camera permission dialog | Removed | ADR-006 |
| Flashlight "Beam Mode" | Removed | ADR-017 |
| Flashlight brightness always shown | Shown only when hardware supports it | ADR-018 |
| Low Battery blocking dialog | Downgraded to a banner | ADR-007 |
| Settings → Audio | Removed | ADR-009 |
| Settings → Permissions (Camera/Storage/Location) | Rewritten as a "no permissions" screen | ADR-009 |
| Go Pro ₹299 | Not in v1.0 | ADR-016 |
| 3D Asset Store ₹99/₹149 | Not in v1.0 | ADR-016 |
| Party Mode, Music Light rows | Omitted, not disabled | ADR-011 |
| 6 objects in the 3D grid | 5 in v1.0 | ADR-012 |
| Emergency: no output mechanism shown | Explicit Screen/Torch/Both selector added | `FR-EMER-02` |
| Emergency "Fast" unspecified | Clamped to 5 Hz + warning | ADR-008 |
| Onboarding text inside images | Real `Text` composables | `FR-GLOBAL-03` |
