# PRODUCT_REQUIREMENTS.md — All-in-One Lighting

Every requirement has an ID, a release phase, an owner module, and at least one
acceptance criterion (`AC-*`) with a matching test (`T-*`) in
[TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md).

**Release column:** `L` = Launch (v1.0) · `P5` = Phase 5 · `P6` = Phase 6 · `D` = Deferred

---

## 1. Global requirements

| ID | Requirement | Release |
|---|---|---|
| FR-GLOBAL-01 | Every interactive control either performs its documented action, or is visibly disabled with a reason, or is labelled **Coming Soon**. No control may be a no-op. | L |
| FR-GLOBAL-02 | The app never displays a hardware "on" state unless the corresponding hardware operation returned success. | L |
| FR-GLOBAL-03 | All user-visible strings live in `strings.xml`. No hard-coded user-facing text in composables, and no text baked into images. | L |
| FR-GLOBAL-04 | The app functions fully offline. No screen in v1.0 requires network access. | L |
| FR-GLOBAL-05 | Back navigation from any screen returns to the logical parent and never leaves an active light running without a visible owner. | L |
| FR-GLOBAL-06 | Any active light (torch, screen light, emergency pattern) is stopped when its owning screen is destroyed, unless the user explicitly opted into persistence for that mode. | L |
| FR-GLOBAL-07 | The app supports system font scaling to 200 % and display sizes up to "Largest" without clipping interactive content. | L |

### Acceptance

- **AC-GLOBAL-01** — A release-gate audit enumerates every interactive control in the app; each is classified `functional`, `disabled-with-reason`, or `coming-soon`. Zero controls classify as `no-op`. → `T-GLOBAL-01`
- **AC-GLOBAL-02** — With a fake torch controller that always fails, toggling the torch leaves the UI in an `Unavailable` or `Error` state and never in `On`. → `T-TORCH-04`
- **AC-GLOBAL-03** — A lint rule / test fails the build if a hard-coded string literal is passed to `Text(...)` outside preview code. → `T-GLOBAL-02`
- **AC-GLOBAL-04** — With airplane mode on, every v1.0 screen renders its normal content. → `T-GLOBAL-03`

---

## 2. Onboarding

### Requirements

| ID | Requirement | Release |
|---|---|---|
| FR-ONB-01 | A splash screen uses the Android 12+ `SplashScreen` API, with no artificial delay. It is dismissed as soon as the first frame of the destination is ready. | L |
| FR-ONB-02 | On first launch only, three onboarding pages are shown: Practical Lighting, 3D Light Sources, Coming Soon. | L |
| FR-ONB-03 | Onboarding is horizontally swipeable, shows a page indicator, and offers **Skip** on pages 1–2 and **Get Started** on page 3. | L |
| FR-ONB-04 | Completing or skipping onboarding persists a flag; subsequent launches go straight to Home. | L |
| FR-ONB-05 | Onboarding requests **no** permissions. | L |
| FR-ONB-06 | Onboarding can be replayed from Settings → General → *Show introduction again*. | L |

### Edge cases

- Process death mid-onboarding → the flag is not yet set, onboarding restarts from page 1. Acceptable; no partial-progress persistence.
- User rotates device mid-onboarding → current page index is retained via `rememberSaveable`.
- App data cleared → onboarding shows again. Expected.

### Acceptance

- **AC-ONB-01** — First launch on a clean install lands on onboarding page 1. → `T-ONB-01`
- **AC-ONB-02** — After tapping *Get Started*, the app relaunch lands directly on Home. → `T-ONB-02`
- **AC-ONB-03** — After tapping *Skip* on page 1, the app lands on Home and relaunch does not show onboarding. → `T-ONB-03`
- **AC-ONB-04** — No permission dialog appears at any point during onboarding. → `T-ONB-04`
- **AC-ONB-05** — Time from process start to first onboarding frame contains no `Thread.sleep`, `delay`, or timed splash hold. Verified by code audit and by cold-start trace. → `T-PERF-01`

---

## 3. Home dashboard

| ID | Requirement | Release |
|---|---|---|
| FR-HOME-01 | Home shows a primary **Phone Flashlight** card with a power control that toggles the real torch in place, without navigating away. | L |
| FR-HOME-02 | The flashlight card's supporting line reflects live state: `Ready to use` / `On` / `Checking…` / `Not supported on this device` / `Camera in use by another app`. | L |
| FR-HOME-03 | A **Lighting Tools** 2×2 grid links to Screen Light, Emergency, Candle Light, More Tools. | L |
| FR-HOME-04 | A wide **3D Simulator** row links to the simulator gallery. | L |
| FR-HOME-05 | A **Smart Lights** entry is present and visually marked *Coming Soon*; tapping it opens the informational Smart Light screen, which contains no device-control affordances. | L |
| FR-HOME-06 | A bottom navigation bar provides Home / 3D / Tools / Settings. | L |
| FR-HOME-07 | If the device reports no flash unit, the flashlight card renders in a disabled style with the reason, and its power control is not actuable. | L |

### Edge cases

- Torch is turned off by another app or by the system → the card must reflect `Off` within one `CameraManager.TorchCallback` dispatch. The UI never holds a stale `On`.
- Torch becomes unavailable while `On` (camera pre-empted) → card drops to `Camera in use by another app` and does not retry in a loop.

### Acceptance

- **AC-HOME-01** — Tapping the flashlight card's power control on a device with a flash unit turns the physical torch on, and the card shows `On`. → `T-TORCH-01`
- **AC-HOME-02** — With a fake capability source reporting no flash unit, the card is disabled, states the reason, and emits no torch calls when tapped. → `T-HOME-02`
- **AC-HOME-03** — An external torch-off event (simulated via the fake controller's callback) updates the card to `Off` without user interaction. → `T-TORCH-05`
- **AC-HOME-04** — Each of the six navigation targets on Home opens its documented destination. → `T-NAV-01`

---

## 4. Phone Flashlight (`SCR-FLASHLIGHT`)

| ID | Requirement | Release |
|---|---|---|
| FR-TORCH-01 | The app toggles the device camera torch using `CameraManager.setTorchMode()` on the first camera reporting `FLASH_INFO_AVAILABLE == true`. | L |
| FR-TORCH-02 | Torch state displayed in the UI is sourced from `CameraManager.TorchCallback`, not from the app's own last command. | L |
| FR-TORCH-03 | Torch brightness control is exposed **only** when `CameraManager.getCameraCharacteristics()` reports `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1` and the device is API 33+. Otherwise the control is hidden entirely — not disabled, not shown at 100 %. | L |
| FR-TORCH-04 | When a brightness control is shown, it maps 1..`maxLevel` and uses `CameraManager.turnOnTorchWithStrengthLevel()`. | L |
| FR-TORCH-05 | The "Beam Mode" control from the concept board (Narrow / Medium / Wide) is **not implemented for the hardware torch** — no Android API controls flash beam angle. It is removed from this screen. | L |
| FR-TORCH-06 | The torch is turned off when the app moves to the background, unless the user has enabled *Keep torch on in background* in Settings. | L |
| FR-TORCH-07 | Failures are surfaced with a cause-specific message, never a generic one. Causes: no flash unit, permission denied, `CameraAccessException` (camera in use), disabled by device policy, unknown. | L |
| FR-TORCH-08 | `CAMERA` permission is **not** requested for torch control. `setTorchMode` does not require it. The app requests no camera permission in v1.0. | L |

> **Note on FR-TORCH-08 and the concept board.** The board shows a camera-permission
> dialog ("Camera access is needed to control your device flashlight"). That dialog is
> **removed**: requesting `CAMERA` for torch-only use is unnecessary, harms Play Store
> review, and `setTorchMode()` works without it. See `ADR-006`.

### Edge cases

| Situation | Behaviour |
|---|---|
| `CameraAccessException.CAMERA_IN_USE` | Show `Camera in use by another app`, offer Retry. Do not auto-retry. |
| `CameraAccessException.MAX_CAMERAS_IN_USE` | Same handling as above. |
| `CameraAccessException.CAMERA_DISABLED` | Show `Flashlight is disabled by a device policy`. No retry. |
| Device has flash but `setTorchMode` throws `IllegalArgumentException` | Treat as unsupported; log; show unavailable state. |
| App backgrounded with torch on, user has opted into background torch | Torch stays on; a persistent foreground-service notification is shown with a Stop action. [Recommended] |
| Low battery (< 5 %) | Show a non-blocking warning; do **not** prevent the torch. The concept board's blocking "Low Battery" dialog is downgraded to an inline banner — blocking a flashlight at low battery is the opposite of useful. See `ADR-007`. |

### Acceptance

- **AC-TORCH-01** — On a device with a flash unit, toggling on produces light and the UI reads `On`. → `T-TORCH-01`
- **AC-TORCH-02** — With `FakeTorchController` reporting `NoFlashUnit`, the screen shows an explanatory unavailable state and the toggle is not actuable. → `T-TORCH-02`
- **AC-TORCH-03** — With `FakeTorchController` throwing `CameraInUse`, the UI shows the camera-conflict message, offers Retry, and remains `Off`. → `T-TORCH-03`
- **AC-TORCH-04** — With a controller that always fails, the UI never reaches `On`. → `T-TORCH-04`
- **AC-TORCH-05** — An unsolicited `onTorchModeChanged(false)` callback moves the UI from `On` to `Off`. → `T-TORCH-05`
- **AC-TORCH-06** — With `maxStrengthLevel == 1`, no brightness slider is composed. With `maxStrengthLevel == 5`, a slider is composed with 5 steps. → `T-TORCH-06`
- **AC-TORCH-07** — `onStop()` with background-torch disabled issues exactly one `setTorchMode(false)`. → `T-TORCH-07`

---

## 5. Screen Light (`SCR-SCREENLIGHT`)

| ID | Requirement | Release |
|---|---|---|
| FR-SCREEN-01 | A full-screen surface of a user-chosen colour, drawn edge-to-edge including under cutouts. | L |
| FR-SCREEN-02 | `White` / `Colour` segmented mode. White mode offers a colour-temperature-like warm→cool ramp; Colour mode offers an HSV wheel. | L |
| FR-SCREEN-03 | A row of fixed colour presets (red, orange, yellow, green, cyan, blue, violet, white). | L |
| FR-SCREEN-04 | A brightness slider that drives `WindowManager.LayoutParams.screenBrightness` for this window only. It never writes `Settings.System.SCREEN_BRIGHTNESS`. | L |
| FR-SCREEN-05 | Because brightness is window-scoped, no `WRITE_SETTINGS` permission is requested and no system setting is modified — therefore nothing needs restoring on exit. | L |
| FR-SCREEN-06 | The screen is kept awake via `FLAG_KEEP_SCREEN_ON` while this screen is resumed, and the flag is cleared on pause. | L |
| FR-SCREEN-07 | Controls auto-hide after 4 s of no interaction and reappear on any tap. A single tap toggles controls; it does not change the colour. | L |
| FR-SCREEN-08 | The screen displays, at least once per session, that the light comes from the display and not the camera flash. | L |
| FR-SCREEN-09 | Last-used colour, mode and brightness persist across sessions. | L |
| FR-SCREEN-10 | Rotation, multi-window, and returning from background preserve colour, mode and brightness. | L |

> **Design note on FR-SCREEN-04/05.** The concept board does not distinguish window
> brightness from system brightness. We use window brightness exclusively. It needs no
> permission, is automatically reverted by the OS when the window goes away, and cannot
> strand the user with a dark or blinding device. See `ADR-005`.

### Edge cases

- Device is in battery-saver with brightness capped → the window request is honoured only up to the system cap. We do not claim a brightness we cannot reach; the slider still represents our request.
- Multi-window / split-screen → full-screen light occupies only our window. Acceptable; no special handling.
- Cutout → `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS` so the light fills the cutout area.

### Acceptance

- **AC-SCREEN-01** — Selecting a preset changes the full-screen surface colour to exactly that `Color` value. → `T-SCREEN-01`
- **AC-SCREEN-02** — The screen never calls `Settings.System.putInt` for brightness; verified by a test that fails if the symbol appears in the feature module. → `T-SCREEN-02`
- **AC-SCREEN-03** — Rotating the device preserves colour, mode and brightness. → `T-SCREEN-03`
- **AC-SCREEN-04** — After relaunching the app, Screen Light opens with the last-used colour and brightness. → `T-SCREEN-04`
- **AC-SCREEN-05** — Controls hide after 4 s and a tap restores them without altering the colour. → `T-SCREEN-05`
- **AC-SCREEN-06** — `FLAG_KEEP_SCREEN_ON` is set on resume and cleared on pause. → `T-SCREEN-06`

---

## 6. Emergency Light (`SCR-EMERGENCY`)

| ID | Requirement | Release |
|---|---|---|
| FR-EMER-01 | Four patterns: **SOS** (· · · — — — · · ·, ITU-R M.1677 timing), **Strobe**, **Blink**, **Steady**. | L |
| FR-EMER-02 | An explicit **Output** selector: `Screen`, `Torch`, or `Both`. The current output is always labelled on screen. `Torch` and `Both` are unavailable when no flash unit exists. | L |
| FR-EMER-03 | Speed presets Slow / Medium / Fast apply to Strobe and Blink only. SOS timing is fixed by standard; Steady has no timing. The speed control is hidden for SOS and Steady. | L |
| FR-EMER-04 | A large, always-visible **STOP** control immediately halts the pattern and restores the torch to off and the screen to the app's normal theme. | L |
| FR-EMER-05 | Maximum flash frequency is clamped to **5 Hz**. A photosensitivity warning is shown before the first strobe session and is acknowledged once per install. | L |
| FR-EMER-06 | No pattern starts automatically. The user must choose a pattern and press start. | L |
| FR-EMER-07 | The pattern stops when the screen is backgrounded. A running emergency pattern is not a background task in v1.0. | L |
| FR-EMER-08 | The screen carries a persistent disclaimer that the app does not guarantee visibility, rescue, or any emergency communication, and is not a substitute for emergency services. | L |
| FR-EMER-09 | Pattern timing uses a monotonic clock and is independent of display frame rate. | L |
| FR-EMER-10 | Brightness control applies to the Screen output only. It is hidden when Output is `Torch`. | L |

> **Safety note on FR-EMER-05.** The concept board's "Fast" preset is unspecified.
> Flash rates in the 3–30 Hz band can provoke photosensitive seizures; the widely-used
> threshold for safe general content is ≤ 3 Hz. We clamp to 5 Hz for a deliberate
> signalling tool and gate it behind an explicit one-time warning. See `ADR-008` and
> `OQ-04`.

### Timing table [Recommended]

| Pattern | Slow | Medium | Fast |
|---|---|---|---|
| Strobe (on/off ms) | 250 / 250 (2 Hz) | 125 / 125 (4 Hz) | 100 / 100 (5 Hz) |
| Blink (on/off ms) | 1000 / 1000 | 500 / 500 | 250 / 250 |
| SOS | fixed: dot 200 ms, dash 600 ms, intra-gap 200 ms, letter-gap 600 ms, word-gap 1400 ms | — | — |
| Steady | continuous | — | — |

### Acceptance

- **AC-EMER-01** — SOS emits the exact sequence 3 short, 3 long, 3 short with the timing above, verified against a virtual clock. → `T-EMER-01`
- **AC-EMER-02** — No speed preset produces a period shorter than 200 ms (5 Hz). → `T-EMER-02`
- **AC-EMER-03** — STOP halts the pattern within one tick and issues `setTorchMode(false)` exactly once. → `T-EMER-03`
- **AC-EMER-04** — With no flash unit, `Torch` and `Both` outputs are disabled with a reason and `Screen` is pre-selected. → `T-EMER-04`
- **AC-EMER-05** — Backgrounding the screen stops the pattern and the torch. → `T-EMER-05`
- **AC-EMER-06** — The photosensitivity warning is shown before the first strobe and not again after acknowledgement. → `T-EMER-06`
- **AC-EMER-07** — The disclaimer text is present in the composed hierarchy whenever the screen is displayed. → `T-EMER-07`

---

## 7. More Tools

| Tool | Release | Mechanism | Notes |
|---|---|---|---|
| Colour Light | L | Screen | Opens Screen Light in Colour mode |
| Night Light | L | Screen | Low-brightness warm amber; separate persisted preset |
| SOS Light | L | Screen/Torch | Deep link into Emergency with SOS pre-selected |
| Strobe Light | L | Screen/Torch | Deep link into Emergency with Strobe pre-selected |
| Candle Light | L | Screen | Animated warm flicker on the display; amplitude bounded, see FR-CANDLE-* |
| Party Mode | D | Screen | Not in v1.0. Row hidden, not shown disabled. |
| Music Light | D | Screen + mic | Not in v1.0. Requires `RECORD_AUDIO`; see `OQ-02`. Row hidden. |

| ID | Requirement | Release |
|---|---|---|
| FR-TOOLS-01 | The More Tools list shows only tools shipped in the current build. Unshipped tools are **omitted**, not rendered as disabled rows. | L |
| FR-CANDLE-01 | Candle Light renders a warm flicker whose luminance varies smoothly by no more than ±12 % around its setpoint, with no change faster than 4 Hz. | L |
| FR-CANDLE-02 | Candle Light offers an *Animation off* option that renders a steady warm colour. | L |
| FR-NIGHT-01 | Night Light defaults to a warm amber at ≤ 15 % window brightness and persists its own brightness separately from Screen Light. | L |

### Acceptance

- **AC-TOOLS-01** — The More Tools list contains exactly the rows marked `L` and no others. → `T-TOOLS-01`
- **AC-TOOLS-02** — Tapping *SOS Light* opens Emergency with pattern = SOS already selected. → `T-TOOLS-02`
- **AC-CANDLE-01** — Measured luminance of the candle surface stays within ±12 % of setpoint over a 30 s sample. → `T-CANDLE-01`

---

## 8. 3D Simulator

Full detail in [THREE_D_LIGHTING_SPEC.md](THREE_D_LIGHTING_SPEC.md). Product-level requirements:

| ID | Requirement | Release |
|---|---|---|
| FR-3D-01 | A gallery lists the available light-source objects with thumbnail, name and category filter chips (All / Portable / Household / Decor). | L |
| FR-3D-02 | v1.0 ships five objects: Torch, Bulb, Tube Light, Candle, LED Lamp. | L |
| FR-3D-03 | A selected object can be rotated freely by dragging; horizontal drag yaws around the scene's up axis, vertical drag pitches around the camera right axis. | L |
| FR-3D-04 | The simulated light emitted by an object is a child of that object's transform, so rotating the object rotates its light in lockstep. | L |
| FR-3D-05 | A torch emits a spot light from its lens, directed along the model's forward axis. Rotating the torch rotates the beam. | L |
| FR-3D-06 | A bulb emits a point light at the filament position. It never renders as a directional beam. | L |
| FR-3D-07 | A tube light emits from an elongated source approximated by multiple point lights along its axis. | L |
| FR-3D-08 | A candle emits a warm point light at the flame, with bounded flicker, and the flame mesh stays attached to the candle transform. | L |
| FR-3D-09 | Two environments: Dark and Studio. The environment affects scene lighting, not just the background. | L |
| FR-3D-10 | Surfaces in the scene visibly respond to the object's light: a ground plane and backdrop receive illumination and, where quality allows, shadows. | L |
| FR-3D-11 | Three quality modes: Performance, Balanced, High Quality, with a documented effect list per mode and automatic initial selection based on device class. | L |
| FR-3D-12 | A reset control restores the object's default orientation and camera. | L |
| FR-3D-13 | Rendering stops and GPU/native resources are released when the simulator is not visible. | L |
| FR-3D-14 | A failed model load shows a recoverable error with a Retry action and never leaves a blank screen. | L |
| FR-3D-15 | Per-object settings (brightness, beam angle, colour temperature, flame animation) persist per object. | L |
| FR-3D-16 | Controls are object-aware: controls that do not apply to the selected object are not rendered. | L |
| FR-3D-17 | Each object's light type and cone match its actual emitting geometry: the LED lamp's wide panel emits a wide spot, not a narrow beam. | L |
| FR-3D-18 | All simulator animation advances by real elapsed time, never by a fixed per-frame increment, so behaviour is identical at 30, 60 and 120 fps. | L |
| FR-ASSET-LICENCE | Every 3D model, texture and HDRI has its source, author, licence and modifications recorded in `docs/licences/assets.md` before it is committed. An asset with no recorded provenance is not merged. | L |

### Acceptance

- **AC-3D-01** — After yawing the torch 90°, the beam's world-space direction has rotated by 90° about the up axis (asserted on the computed transform, ±1°). → `T-3D-01`
- **AC-3D-02** — The bulb's light entity is a point light at every orientation; no spot-light component is ever created for `OBJ-BULB`. → `T-3D-02`
- **AC-3D-03** — Switching Dark ↔ Studio leaves brightness, beam angle, colour temperature, selected object and rotation unchanged. → `T-3D-03`
- **AC-3D-04** — Leaving the simulator destroys the Filament `View`, `Scene`, `Renderer`, `SwapChain` and all loaded asset resources; a repeated enter/leave cycle 20× shows no monotonic native-memory growth. → `T-3D-04`
- **AC-3D-05** — A corrupted/missing `.glb` produces an error state with Retry; tapping Retry re-attempts the load. → `T-3D-05`
- **AC-3D-06** — Reset returns the object quaternion to identity and the camera to its default pose. → `T-3D-06`
- **AC-3D-07** — For `OBJ-CANDLE`, the Beam Angle control is absent from the composition; for `OBJ-TORCH`, the Flame Animation control is absent. → `T-3D-07`
- **AC-3D-08** — `OBJ-LED-LAMP` creates a spot light with the documented 35°/55° cone; no object creates a light type other than the one its spec names. → `T-3D-14`
- **AC-3D-09** — Advancing a virtual clock by the same wall-clock duration produces the same animation phase whether frames arrive at 30 or 120 fps. → `T-3D-16`
- **AC-ASSET-01** — Every file under `assets/models/`, `assets/thumbs/` and `assets/env/` has a row in `docs/licences/assets.md`. → release gate 14

---

## 9. Settings & About

| ID | Requirement | Release |
|---|---|---|
| FR-SET-01 | Settings groups: General, Display, Permissions, About. **Audio is omitted** in v1.0 — the app produces no sound and has no audio feature. | L |
| FR-SET-02 | General: *Show introduction again*, *Keep torch on in background* (default off). | L |
| FR-SET-03 | Display: default 3D environment, default quality mode, *Reduce motion* (honours the system animation scale and can be forced on). | L |
| FR-SET-04 | Permissions: a read-only summary of which permissions the app holds and why, with a link to system app settings. Since v1.0 requests no runtime permissions, this screen explains that. | L |
| FR-SET-05 | About: app name, version name and code, Privacy Policy, Open-source licences, Help & Support. | L |
| FR-SET-06 | An **Open-source licences** entry is present and lists Filament and all third-party licences. | L |

> The concept board shows an **Audio (Sounds & Vibration)** row and a **Permissions
> (Camera, Storage, Location)** row. v1.0 has no audio, and requests no camera, storage
> or location permission. Showing those rows would be `FR-GLOBAL-01` violations. See `ADR-009`.

### Acceptance

- **AC-SET-01** — Settings contains no Audio group. → `T-SET-01`
- **AC-SET-02** — Toggling *Show introduction again* and relaunching shows onboarding. → `T-SET-02`
- **AC-SET-03** — The About screen's version string equals `BuildConfig.VERSION_NAME`. → `T-SET-03`
- **AC-SET-04** — The licences screen renders a non-empty list including Filament. → `T-SET-04`

---

## 10. Smart Lights (informational, v1.0)

| ID | Requirement | Release |
|---|---|---|
| FR-SMART-01 | A single informational screen describes planned smart-light integration and is labelled **Coming Soon** on every entry point. | L |
| FR-SMART-02 | The screen contains **no** discovery, pairing, on/off, brightness or colour controls — not even disabled ones. | L |
| FR-SMART-03 | The screen requests no permissions and makes no network calls. | L |
| FR-SMART-04 | The domain interfaces (`SmartLightProvider`, `SmartLightDevice`, `DeviceCapabilities`) exist in code with no shipping implementation, so Phase 6 can add an adapter without redesign. | L |
| FR-SMART-05 | Real discovery, pairing and control. | P6 |

### Acceptance

- **AC-SMART-01** — The Smart Light screen composition contains no `Switch`, `Slider` or control with a device-control semantic. → `T-SMART-01`
- **AC-SMART-02** — Every entry point to the screen carries a "Coming Soon" label in its accessibility text. → `T-SMART-02`
- **AC-SMART-03** — No network permission is declared in the merged manifest for v1.0. → `T-SMART-03`

---

## 11. Monetization

| ID | Requirement | Release |
|---|---|---|
| FR-MON-01 | v1.0 contains **no** billing integration, no price strings, and no purchase UI. | L |
| FR-MON-02 | If billing is approved, Google Play Billing is integrated with server-free local entitlement caching, purchase acknowledgement within 3 days, restore-purchases, and explicit handling of pending, cancelled and network-failure states. | P5 |
| FR-MON-03 | No price is hard-coded; all prices come from `ProductDetails` returned by Play Billing. | P5 |

> The concept board shows a *Go Pro* screen at ₹299 and an asset store at ₹99/₹149.
> These are **not** implemented in v1.0 — hard-coded prices are forbidden by the brief,
> and the brief also says not to add payment complexity to the first release unless
> explicitly approved. Tracked as `OQ-01`.

### Acceptance

- **AC-MON-01** — The v1.0 release build contains no dependency on `com.android.billingclient` and no currency symbol in any user-facing string. → `T-MON-01`

---

## 12. Nonfunctional requirements

| ID | Requirement | Target | Status |
|---|---|---|---|
| NFR-PERF-01 | Cold start to interactive Home | ≤ 1200 ms p50, mid-tier | [Provisional] |
| NFR-PERF-02 | Screen-to-screen navigation | ≤ 150 ms to first frame | [Provisional] |
| NFR-PERF-03 | 3D model load (cold, from assets) | ≤ 1500 ms | [Provisional] |
| NFR-PERF-04 | Simulator frame rate, Balanced, mid-tier | ≥ 55 fps sustained over 60 s | [Provisional] |
| NFR-PERF-05 | No jank frames > 2 % during simulator rotation | measured by `JankStats` | [Provisional] |
| NFR-MEM-01 | Peak PSS with simulator active | ≤ 320 MB mid-tier | [Provisional] |
| NFR-MEM-02 | No monotonic native memory growth over 20 simulator enter/exit cycles | 0 growth beyond allocator noise | Enforced by `T-3D-04` |
| NFR-SIZE-01 | arm64-v8a app-bundle download size | ≤ 28 MB | [Provisional] |
| NFR-SIZE-02 | Installed size | ≤ 75 MB | [Provisional] |
| NFR-BAT-01 | Screen Light at 50 % brightness draws no measurable CPU beyond compositing | no busy loop; verified by code audit + Battery Historian | [Provisional] |
| NFR-BAT-02 | No work scheduled while the app is backgrounded in v1.0 | zero `WorkManager`/`AlarmManager`/`JobScheduler` usage | Enforced by `T-GLOBAL-04` |
| NFR-REL-01 | Crash-free sessions | ≥ 99.5 % | Post-launch |
| NFR-A11Y-01 | All interactive targets ≥ 48 × 48 dp | 100 % | `T-A11Y-01` |
| NFR-A11Y-02 | Text contrast ≥ 4.5:1 (body), ≥ 3:1 (large) | 100 % of tokens | `T-A11Y-02` |
| NFR-A11Y-03 | Every non-decorative element has a content description or merged semantic label | 100 % | `T-A11Y-03` |
| NFR-A11Y-04 | Animations are reduced or disabled when `Settings.Global.ANIMATOR_DURATION_SCALE == 0` | 100 % | `T-A11Y-04` |
| NFR-COMPAT-01 | minSdk 24, targetSdk 36, compileSdk 36 | — | `ADR-002` |
| NFR-COMPAT-02 | The app installs and reaches Home on a device without OpenGL ES 3.0; the simulator is disabled with an explanation | — | `T-3D-08` |

---

## 13. User stories

| ID | As a… | I want… | So that… | Satisfied by |
|---|---|---|---|---|
| US-01 | person in a power cut | to turn on the torch in one tap | I can see immediately | FR-HOME-01 |
| US-02 | person whose phone has no flash | to be told plainly that it can't | I stop tapping a dead button | FR-HOME-07, FR-TORCH-07 |
| US-03 | person photographing at night | a coloured full-screen light | I can add a colour cast cheaply | FR-SCREEN-01..03 |
| US-04 | hiker needing help | a standard SOS signal | the pattern is recognisable | FR-EMER-01 |
| US-05 | photosensitive user | not to be flashed without warning | I am not harmed by the app | FR-EMER-05 |
| US-06 | photography student | to compare a narrow beam to a broad bulb | I understand lighting choices | FR-3D-05, FR-3D-06 |
| US-07 | design student | to see warm vs. cool light on a real surface | I can judge colour temperature | FR-3D-09, FR-3D-10 |
| US-08 | user on a cheap phone | the simulator not to overheat my device | the app stays usable | FR-3D-11, FR-3D-13 |
| US-09 | smart-bulb owner | to know whether this app will control my lights | I can plan | FR-SMART-01, FR-SMART-02 |
| US-10 | returning user | my last colour and brightness remembered | I don't reconfigure every time | FR-SCREEN-09, FR-3D-15 |
| US-11 | screen-reader user | every control announced meaningfully | I can use the app | NFR-A11Y-03 |
| US-12 | privacy-conscious user | the app not to ask for permissions it doesn't need | I can trust it | FR-TORCH-08, FR-SMART-03 |

---

## 14. Cross-cutting edge cases

| Scenario | Required behaviour | Test |
|---|---|---|
| Process death while Screen Light is active | On restore, the app returns to Screen Light with persisted colour/brightness; the light is **not** auto-reactivated as a surprise — the screen simply re-renders, which is the light. | `T-LIFE-01` |
| Process death while an emergency pattern runs | The pattern does **not** resume automatically. The screen restores in a stopped state. | `T-LIFE-02` |
| Process death while in the simulator | The last object, rotation, and settings are restored; the model reloads with a visible loading state. | `T-LIFE-03` |
| Incoming call during strobe | Pattern stops (FR-EMER-07); torch off. | `T-LIFE-04` |
| Another app grabs the camera while torch is on | UI reflects `Off` plus the conflict reason within one callback. | `T-TORCH-05` |
| Device enters battery saver during simulation | Quality mode drops to Performance and a one-line notice is shown. [Recommended] | `T-3D-09` |
| Thermal status reaches `THERMAL_STATUS_SEVERE` | Simulator drops to Performance and caps to 30 fps; a notice is shown. | `T-3D-10` |
| `onTrimMemory(TRIM_MEMORY_RUNNING_CRITICAL)` while simulating | Release non-essential GPU resources; keep the current model. | `T-3D-11` |
| Font scale 200 % on Home | No card text is clipped; grid reflows to a single column below 360 dp effective width. | `T-A11Y-05` |
| RTL locale | Layout mirrors; the colour wheel and 3D gestures do **not** mirror. | `T-A11Y-06` |
