# SCREEN_SPECIFICATION.md — All-in-One Lighting

Every screen, every state. Components referenced as `CMP-*` are defined in
[UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md). Requirements as `FR-*` are in
[PRODUCT_REQUIREMENTS.md](PRODUCT_REQUIREMENTS.md).

**Screen state contract.** Every screen exposes exactly one
`StateFlow<XxxUiState>` from its ViewModel. Loading / content / empty / error are
*variants of that one type*, never independent booleans.

---

## 0. Navigation graph

```
NavHost(start = Splash)
│
├── SCR-SPLASH ──────► (first run)  SCR-ONBOARDING   [popUpTo Splash, inclusive]
│                └────► (returning)  SCR-HOME        [popUpTo Splash, inclusive]
│
├── SCR-ONBOARDING ──► SCR-HOME                      [popUpTo Onboarding, inclusive]
│
└── Bottom-nav graph (4 top-level destinations, each its own back stack)
    ├── SCR-HOME
    │   ├── SCR-FLASHLIGHT
    │   ├── SCR-SCREENLIGHT      (full-screen, hides bottom nav)
    │   ├── SCR-EMERGENCY        (full-screen, hides bottom nav)
    │   ├── SCR-CANDLE           (full-screen, hides bottom nav)
    │   └── SCR-SMARTLIGHT
    ├── SCR-SIM-GALLERY
    │   └── SCR-SIM-VIEWER/{objectId}   (hides bottom nav)
    ├── SCR-TOOLS
    │   ├── → SCR-SCREENLIGHT?mode=color
    │   ├── → SCR-SCREENLIGHT?mode=night
    │   ├── → SCR-EMERGENCY?pattern=sos
    │   ├── → SCR-EMERGENCY?pattern=strobe
    │   └── → SCR-CANDLE
    └── SCR-SETTINGS
        ├── SCR-SETTINGS-DISPLAY
        ├── SCR-SETTINGS-PERMISSIONS
        ├── SCR-ABOUT
        └── SCR-LICENCES
```

**Back behaviour**
- Top-level destinations: back from Home exits the app; back from 3D/Tools/Settings
  returns to Home (single-destination back stack behaviour, `saveState = true`).
- Full-screen light screens: back stops the active light, then pops.
- `SCR-SIM-VIEWER`: back releases renderer resources, then pops to the gallery.

**Deep links** — none in v1.0 (`[Deferred]`). The `?mode=` / `?pattern=` arguments
above are in-app route arguments, not `android:host` deep links. No `intent-filter`
beyond `LAUNCHER` is declared.

---

## SCR-SPLASH — Splash

**Purpose.** Hand off from the system splash to the correct destination with no
perceptible delay.

**Implementation.** `androidx.core:core-splashscreen`. The Activity installs the
splash screen and holds `setKeepOnScreenCondition { !viewModel.isReady.value }`.
`isReady` flips as soon as the onboarding flag has been read from DataStore —
typically one disk read, single-digit milliseconds.

**There is no timed hold, animation gate, or minimum display duration** (`FR-ONB-01`).

**States**

| State | Shown | Exit |
|---|---|---|
| `Resolving` | System splash (windowBackground `Ink900`, app icon) | — |
| `Resolved(firstRun = true)` | — | navigate `SCR-ONBOARDING` |
| `Resolved(firstRun = false)` | — | navigate `SCR-HOME` |
| `Resolved` after DataStore read failure | — | treat as `firstRun = true`; log; never block |

**Accessibility.** Nothing focusable; the system splash is not announced.

**Acceptance.** `AC-ONB-05`. **Tests.** `T-ONB-01`, `T-PERF-01`.

---

## SCR-ONBOARDING — Onboarding (3 pages)

**Purpose.** Explain the three pillars once, request nothing.

**Layout.** `HorizontalPager(pageCount = 3)` filling the screen.
Each page: illustration (top 45 %), headline `headlineMedium`, body `bodyLarge`
`Slate300`, both centred, horizontal padding `xxl`.
Fixed bottom bar: page indicator (3 dots, active = 8 dp `Blue500`, inactive = 6 dp
`Ink500`), then the primary button.

| Page | Headline | Body | Primary | Secondary |
|---|---|---|---|---|
| 1 | Practical lighting for everyday life | Use your phone's flashlight, a full-screen light, and emergency signals. | Next | Skip |
| 2 | Explore realistic 3D light sources | Rotate, adjust, and see how different lights actually behave. | Next | Skip |
| 3 | Ready when it matters | Send an SOS signal, use a strobe, or light your way in an emergency — with your flash or your screen. | Get started | — |

**Interactions**
- Swipe horizontally, or tap Next, to advance.
- Skip (pages 1–2, `TextButton`, `Slate200`, top-right) → complete onboarding → Home.
- Get started → complete onboarding → Home.
- System back on page 2/3 → previous page. On page 1 → exits the app (does **not**
  complete onboarding).

**State** — `OnboardingUiState(page: Int)`, page held in `rememberSaveable`.
Completion writes `onboardingCompleted = true` to DataStore *before* navigating; if
the write fails, navigation still proceeds and the failure is logged (worst case the
user sees onboarding once more).

**Illustrations.** `WebP`, ≤ 120 KB each, no embedded text (`FR-GLOBAL-03`),
`contentDescription = null` (decorative — the headline carries the meaning).

**Accessibility.** Page indicator is `clearAndSetSemantics {}`; the pager exposes
`"Page X of 3"` via `stateDescription`. Headline has `semantics { heading() }`.

**Error / empty / loading.** None — content is static and local.

**Acceptance.** `AC-ONB-01..04`. **Tests.** `T-ONB-01..04`.

---

## SCR-HOME — Home dashboard

**Purpose.** One tap to light; a clear map of everything else.

**Layout** (vertical scroll, horizontal padding `lg`)

1. **Top bar** — title "All-in-One Lighting" `titleLarge`; trailing settings icon
   (24 dp) → `SCR-SETTINGS`.
2. **`CMP-CARD-HERO` — Phone Flashlight.** Title "Phone flashlight", supporting
   line = live status, trailing `CMP-POWER`.
3. **`CMP-BANNER`** — conditional (low battery, thermal, camera conflict).
4. **Section heading** "Lighting tools" `titleMedium`, `semantics { heading() }`.
5. **2×2 grid of `CMP-CARD-TOOL`** — Screen Light / Emergency / Candle Light /
   More Tools.
6. **`CMP-ROW` — 3D Simulator**, "Explore 3D light sources", chevron.
7. **`CMP-ROW` — Smart Lights**, muted accent, `CMP-BADGE` "COMING SOON".
8. **`CMP-BOTTOMNAV`**, Home active.

**Flashlight card status line (`FR-HOME-02`)**

| Torch state | Line | Power control |
|---|---|---|
| `Unknown` (initial) | "Checking…" | disabled, no spinner inside the button |
| `Available(on = false)` | "Ready to use" | enabled, off |
| `Available(on = true)` | "On" | enabled, on, cyan ring |
| `Unsupported` | "Not supported on this device" | unavailable style |
| `Conflict` | "Camera is in use by another app" | enabled → tapping retries |
| `PolicyDisabled` | "Disabled by a device policy" | unavailable style |
| `Error(cause)` | cause-specific sentence | enabled → tapping retries |

The status line is also the card's `stateDescription`, so TalkBack announces
state changes without the user re-focusing.

**Interactions**

| Target | Action |
|---|---|
| Power control | `toggleTorch()`. Optimistic UI is **forbidden** — the card changes only when the torch callback or the failure result arrives. |
| Hero card body (not the power control) | → `SCR-FLASHLIGHT` |
| Screen Light card | → `SCR-SCREENLIGHT` |
| Emergency card | → `SCR-EMERGENCY` |
| Candle card | → `SCR-CANDLE` |
| More Tools card | → `SCR-TOOLS` (switches bottom-nav tab) |
| 3D Simulator row | → `SCR-SIM-GALLERY` (switches tab) |
| Smart Lights row | → `SCR-SMARTLIGHT` |

**States**
- *Loading*: only the flashlight status is ever "loading" ("Checking…"); the rest of
  Home is static and renders immediately. There is no full-screen Home spinner.
- *Empty*: not applicable — Home has fixed content.
- *Error*: never full-screen. Hardware problems render in the hero card's status line
  plus, for transient conditions, a `CMP-BANNER`.
- *Permission*: not applicable — Home requests nothing.

**Acceptance.** `AC-HOME-01..04`. **Tests.** `T-HOME-01..03`, `T-NAV-01`, `T-TORCH-01/02/05`.

---

## SCR-FLASHLIGHT — Phone flashlight detail

**Purpose.** A focused torch screen with the device's *actual* capabilities, and
nothing more.

**Layout**
1. Top bar with back arrow, title "Flashlight".
2. Hero visual — a torch illustration whose glow intensity reflects the on/off state.
   Decorative, `contentDescription = null`.
3. Large `CMP-POWER` (80 dp variant), centred, with the state word beneath in
   `titleMedium`.
4. **Conditional** `CMP-SLIDER` "Brightness" — composed **only** when
   `capabilities.maxTorchStrength > 1` and `Build.VERSION.SDK_INT >= 33`
   (`FR-TORCH-03`). Steps = `maxTorchStrength`.
5. `CMP-BANNER` for the active failure cause, when any.
6. Explanatory `bodyMedium` footnote: "This uses your device's camera flash."

**Explicitly absent**: the concept board's *Beam Mode* segmented control
(`FR-TORCH-05`). Android exposes no beam-angle control for the flash LED, so the
control would be decorative.

**States**

| State | Rendering |
|---|---|
| `Checking` | Power control disabled, label "Checking…" |
| `Off` / `On` | Normal; label "Off"/"On" |
| `Unsupported` | `CMP-ERROR` replaces the control area: icon, "Flashlight not available", "This device doesn't have a camera flash.", no action button |
| `Conflict` | Power control enabled + `CMP-BANNER` error "Camera is in use by another app. Close it and try again." with a **Retry** text action |
| `PolicyDisabled` | `CMP-ERROR`, "Your device's administrator has disabled the flashlight." |

**Lifecycle (`FR-TORCH-06`)**
- `ON_START`: subscribe to the torch state flow.
- `ON_STOP`: if *Keep torch on in background* is off → `setTorchMode(false)`;
  unsubscribe.
- Rotation does not toggle the torch — the controller is a process-scoped singleton,
  not a ViewModel-scoped object.

**Accessibility.** Power control `Role.Switch` with `stateDescription`. The brightness
slider announces "Brightness, N of M". `CMP-ERROR` body has `liveRegion = Polite`.

**Acceptance.** `AC-TORCH-01..07`. **Tests.** `T-TORCH-01..07`.

---

## SCR-SCREENLIGHT — Screen light

**Purpose.** Turn the display itself into a controllable light.

Route argument `mode` ∈ `{white, color, night}` (default `white`) lets More Tools
deep-link into a variant (`FR-TOOLS-*`).

**Layout** — a full-bleed `Box`:
- **Light surface**: `Modifier.fillMaxSize().background(currentColor)`, drawn behind
  the status and navigation bars. Colour changes cross-fade over `motionSlow`.
- **Control overlay** (auto-hiding, `FR-SCREEN-07`), anchored bottom, inside a
  `CMP-SHEET`-styled scrim panel so controls stay legible on any light colour:
  1. `CMP-SEGMENTED` — White | Colour
  2. **White mode**: a warm↔cool `CMP-SLIDER` (2000 K–6500 K mapped to an sRGB ramp)
     **Colour mode**: `CMP-COLORWHEEL` + *Advanced* expander with Hue/Saturation sliders
  3. `CMP-SWATCH` preset row
  4. `CMP-SLIDER` — Brightness
  5. One-time `CMP-BANNER` (info): "This light comes from your screen, not the camera
     flash." Dismissible; shown once per session (`FR-SCREEN-08`).
- **Back affordance**: a 48 dp back button, top-left, inside the overlay.

**Interactions**

| Gesture | Result |
|---|---|
| Single tap on the light surface | Toggle overlay visibility. **Does not change colour.** |
| Any interaction | Resets the 4 s auto-hide timer |
| Drag on the wheel | Live colour update; pointer claimed so the overlay does not scroll |
| System back | Pop. The window brightness override and `KEEP_SCREEN_ON` are released automatically with the window. |

**Brightness (`FR-SCREEN-04/05`)**
- Sets `window.attributes.screenBrightness` in `0.01f..1.00f` for *this window only*.
- **Never** writes `Settings.System.SCREEN_BRIGHTNESS`; **never** requests
  `WRITE_SETTINGS`. Because the override is window-scoped, the OS restores the user's
  brightness when the screen is left — there is nothing to restore manually.
- On `ON_PAUSE` the attribute is set to `BRIGHTNESS_OVERRIDE_NONE`.

**Screen-on (`FR-SCREEN-06`).** `FLAG_KEEP_SCREEN_ON` added on `ON_RESUME`, cleared
on `ON_PAUSE`. No `WakeLock`, no `WAKE_LOCK` permission.

**Cutout.** Window flag `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS` so the colour fills
the notch region.

**Persistence (`FR-SCREEN-09`).** `mode`, `color`, `brightness` per variant
(`white`/`color`/`night` keep separate values) in DataStore, written debounced at
400 ms so dragging a slider does not thrash disk.

**States**

| State | Rendering |
|---|---|
| `Loading` | Never visible in practice; if preferences have not loaded, render the default white at 70 % rather than a spinner |
| `Content` | As above |
| `Error` | Not applicable — no operation here can fail. A preference read failure falls back to defaults silently. |

**Orientation / multi-window.** Colour and brightness survive both; the overlay
reflows. In split-screen the light fills only this window — acceptable, documented in
[KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md).

**Accessibility.** The light surface is `clearAndSetSemantics {}`. Overlay auto-hide
is **disabled** when a touch-exploration service is active (`AccessibilityManager
.isTouchExplorationEnabled`) — controls that vanish are unusable with TalkBack.

**Acceptance.** `AC-SCREEN-01..06`. **Tests.** `T-SCREEN-01..06`.

---

## SCR-CANDLE — Candle light

A constrained variant of Screen Light.

**Layout.** Full-bleed warm amber surface with a bounded luminance flicker, plus an
overlay containing: `CMP-SLIDER` Brightness, `CMP-SEGMENTED` Flame animation
(Off | Low | High), and the back affordance.

**Flicker (`FR-CANDLE-01`).** Luminance = `setpoint × (1 + a·n(t))` where `n(t)` is
value noise sampled at 4 Hz and smoothed, `a = 0.06` (Low) or `0.12` (High).
Hard bound: the composed multiplier is `coerceIn(0.88f, 1.12f)` and the colour is
recomputed at most 4 times per second, interpolated between samples. This is a
deliberate anti-discomfort constraint — an unbounded `Random.nextFloat()` flicker is
forbidden.

**Reduce motion.** Animation forced to Off; a steady amber is rendered.

**Acceptance.** `AC-CANDLE-01`. **Tests.** `T-CANDLE-01`.

---

## SCR-EMERGENCY — Emergency light

**Purpose.** Produce recognisable signalling patterns, honestly and safely.

Route argument `pattern` ∈ `{sos, strobe, blink, steady}` (default `sos`).

**Layout**
1. Top bar, back arrow, title "Emergency light".
2. **Output selector** `CMP-SEGMENTED` — Screen | Torch | Both. The current output is
   additionally stated in words beneath: "Using the screen" / "Using the camera
   flash" / "Using both". (`FR-EMER-02`)
3. **Pattern selector** — four `CMP-CHIP`-styled large buttons: SOS, Strobe, Blink,
   Steady. Selected uses `Red500` fill (this is the one screen where red is a
   selection colour, matching the board).
4. **Speed** `CMP-SEGMENTED` Slow | Medium | Fast — composed **only** for Strobe and
   Blink (`FR-EMER-03`).
5. **Brightness** `CMP-SLIDER` — composed only when output includes Screen
   (`FR-EMER-10`).
6. **START / STOP** — a full-width 64 dp pill. Idle: `Blue500` "Start". Running:
   `Red500` "Stop", and it remains on screen and reachable at all times
   (`FR-EMER-04`).
7. **Disclaimer** `bodyMedium` `Slate300`, always present (`FR-EMER-08`):
   *"This is a signalling aid. It does not contact emergency services and does not
   guarantee you will be seen. Call your local emergency number for help."*

**When running**, the whole background becomes the signalling surface (if output
includes Screen) and controls sit in a scrim panel; the STOP button is never covered.

**Photosensitivity gate (`FR-EMER-05`).** The first time the user starts Strobe (any
speed) on this install, a `CMP-DIALOG` appears: title "Flashing light warning", body
"This mode flashes rapidly. Flashing light can trigger seizures in people with
photosensitive epilepsy.", actions **Continue** / **Cancel**. Acknowledgement is
persisted. Max rate is clamped to 5 Hz regardless.

**Timing (`FR-EMER-09`).** The scheduler is a coroutine on a `Default` dispatcher
driving a `kotlinx.coroutines.delay`-based loop against `TimeSource.Monotonic`, with
each step's deadline computed from the pattern start time so errors do not accumulate.
It is **not** driven by `withFrameNanos` and does not depend on display refresh rate.
The scheduler is injected with a `TimeSource` so tests can use a virtual clock.

**States**

| State | Rendering |
|---|---|
| `Idle` | Controls enabled, button reads "Start" |
| `Running(pattern, output, speed)` | Signalling active, button reads "Stop", pattern/output/speed locked (changing them stops first, then restarts) |
| `TorchUnavailable` | Torch and Both segments disabled with a reason line; Screen pre-selected (`FR-EMER-04` acceptance) |
| `Error` | `CMP-BANNER` error with the torch failure cause; pattern falls back to Screen output and continues rather than stopping dead |

**Lifecycle (`FR-EMER-07`).** `ON_STOP` → `stop()`; torch off; screen restored.
Process death → restores in `Idle` (`T-LIFE-02`).

**Accessibility.** The STOP button has `contentDescription = "Stop emergency light"`
and is the first element in the focus order while running. Pattern buttons are a
`selectableGroup()`. The running state is announced via a polite live region.

**Acceptance.** `AC-EMER-01..07`. **Tests.** `T-EMER-01..07`, `T-LIFE-02`, `T-LIFE-04`.

---

## SCR-TOOLS — More tools

**Layout.** Title "More tools", then a vertical list of `CMP-ROW`s with 8 dp gaps.

**v1.0 rows (in order)** — only shipped tools appear (`FR-TOOLS-01`):

| Row | Subtitle | Destination |
|---|---|---|
| Colour light | Full-screen colour | `SCR-SCREENLIGHT?mode=color` |
| Night light | Soft and gentle | `SCR-SCREENLIGHT?mode=night` |
| SOS light | Emergency signal | `SCR-EMERGENCY?pattern=sos` |
| Strobe light | Flashing light | `SCR-EMERGENCY?pattern=strobe` |
| Candle light | Warm ambience | `SCR-CANDLE` |

**Party Mode and Music Light are not rendered at all** — not as disabled rows. A
disabled row invites repeated tapping and implies a near-term arrival we have not
committed to. They are absent until implemented (`ADR-011`).

**States.** No loading, empty or error state — content is a static local list.

**Acceptance.** `AC-TOOLS-01..02`. **Tests.** `T-TOOLS-01..02`.

---

## SCR-SIM-GALLERY — 3D simulator gallery

**Layout**
1. Top bar "3D simulator"; trailing info icon → a `CMP-SHEET` explaining that the
   view is a **simulation**, not a measurement.
2. `CMP-CHIP` filter row: All · Portable · Household · Decor.
3. `LazyVerticalGrid`, 2 columns (3/4 on larger widths), cells = thumbnail (1:1,
   `rLg`) + name `titleMedium` + category `labelMedium` `Slate300`.
4. `CMP-BOTTOMNAV`, 3D active.

**Catalog (v1.0)**

| Object | Category | Id |
|---|---|---|
| Torch | Portable | `obj_torch` |
| Bulb | Household | `obj_bulb` |
| Tube light | Household | `obj_tube` |
| Candle | Decor | `obj_candle` |
| LED lamp | Household | `obj_led_lamp` |

**Thumbnails.** Pre-rendered `WebP` in `assets/thumbs/`, 512×512, ≤ 40 KB each.
Generated offline from the models — **not** rendered at runtime, which would require
spinning up a renderer just to populate a grid.

**States**

| State | Rendering |
|---|---|
| `Loading` | Catalog is a local file; loads in < 10 ms. A 150 ms-delayed `CMP-LOADING` avoids a flash of spinner. |
| `Content` | Grid |
| `Empty` (filter yields nothing) | `CMP-EMPTY`: "No light sources in this category", action "Show all" |
| `Error` (catalog unreadable) | `CMP-ERROR`: "Couldn't load the light sources.", action "Retry" |
| `Unsupported` (no OpenGL ES 3.0) | `CMP-ERROR` covering the grid: "3D simulation isn't supported on this device", body naming the requirement; the rest of the app stays usable (`NFR-COMPAT-02`) |

**Acceptance.** `AC-3D-*` gallery portions. **Tests.** `T-3D-08`, `T-NAV-02`.

---

## SCR-SIM-VIEWER/{objectId} — 3D simulator viewer

Full behaviour in [THREE_D_LIGHTING_SPEC.md](THREE_D_LIGHTING_SPEC.md); this section
covers UI structure and states.

**Layout** — portrait, Compact:
- **Viewport** (top ~58 % of height): a `SurfaceView` hosting Filament, drawn
  edge-to-edge behind the status bar.
  - Overlay top-left: back arrow (48 dp, scrimmed circle).
  - Overlay top-right: reset-rotation icon, then quality-mode icon → `CMP-SHEET`.
  - Overlay bottom-centre: a **360°** rotation affordance arc (matching the board) —
    decorative, indicating the object is draggable. It has
    `contentDescription = null`; the viewport itself carries the usable semantics.
- **Control panel** (below, scrollable):
  1. `CMP-SLIDER` Brightness — always present.
  2. Object-specific control (`FR-3D-16`):
     - Torch → `CMP-SEGMENTED` Beam angle: Narrow | Medium | Wide
     - Bulb / Tube / LED lamp → `CMP-SEGMENTED` Colour temperature: Warm | Neutral | Cool
     - Candle → `CMP-SEGMENTED` Flame animation: Off | Low | High
  3. `CMP-SEGMENTED` Environment: Dark | Studio.
  4. For colour-capable sources (LED lamp) → `CMP-SWATCH` colour row.
- Landscape: viewport left 60 %, controls scroll right 40 %.

**Gestures (`FR-3D-03`)**

| Gesture | Effect |
|---|---|
| One-finger drag, horizontal | Yaw the object about the scene up axis (+X drag → +yaw) |
| One-finger drag, vertical | Pitch about the camera right axis, clamped to ±85° to prevent gimbal flip |
| Two-finger pinch | Dolly the camera within `[0.6×, 2.5×]` of the default distance |
| Double tap | Reset orientation and camera (same as the reset button) |
| Drag inside the viewport | Claims the pointer so the control panel does not scroll |

Horizontal yaw is unlimited (full 360°); vertical pitch is clamped — this is the
documented answer to "how do users reach a full 360° view without unexpected flips":
yaw gives the full sphere of azimuth, clamped pitch prevents the pole flip.

**States**

| State | Rendering |
|---|---|
| `Loading` | Viewport shows `CMP-LOADING` with a determinate bar and "Loading model…"; controls are composed but disabled |
| `Ready` | Live render; controls enabled |
| `Error(AssetMissing / AssetCorrupt / RendererInit)` | `CMP-ERROR` fills the viewport: cause-specific title and body, **Retry** primary action, "Back to gallery" secondary (`FR-3D-14`) |
| `Degraded` | Rendering continues at Performance quality with a `CMP-BANNER` warning explaining why (thermal, battery saver, or low memory) |

**Lifecycle (`FR-3D-13`)**
- `ON_RESUME` → start the `Choreographer` render loop.
- `ON_PAUSE` → stop the loop immediately. No frames are rendered while not visible.
- `ON_DESTROY` / leaving the screen → destroy `Renderer`, `View`, `Scene`,
  `SwapChain`, camera, lights, and release the loaded asset, in that order, then
  `Engine.destroy()`.
- `onTrimMemory(RUNNING_CRITICAL)` → drop the IBL/skybox and reflections, keep the model.

**Persistence (`FR-3D-15`).** Per-object: brightness, beam angle, colour temperature,
flame animation, colour. Global: environment, quality mode, last-opened object.
Rotation is **transient** — it is kept across rotation and process death via
`SavedStateHandle` but is not written to DataStore; a user returning days later gets
the default orientation.

**Accessibility.** The viewport is a single focusable node with
`contentDescription = "<Object> 3D view. Double tap and drag to rotate."` and custom
accessibility actions: "Rotate left 15°", "Rotate right 15°", "Reset view". Rotation
by drag is impossible with TalkBack, so these actions are the accessible path
(`NFR-A11Y-03`).

**Acceptance.** `AC-3D-01..07`. **Tests.** `T-3D-01..11`.

---

## SCR-SMARTLIGHT — Smart lights (Coming soon)

**Purpose.** Set expectations honestly.

**Layout.** Top bar "Smart lights". Centred illustration. `CMP-BADGE` "COMING SOON".
Title "Smart light integration is on the way" `titleLarge`. Body: what is planned
(discover bulbs on your network, on/off, brightness, colour, scenes) stated in the
future tense. A bulleted list of planned capabilities. A single **outlined** button
"Learn more" that opens an in-app `CMP-SHEET` with more detail — **not** a web link,
since v1.0 declares no `INTERNET` permission.

**Hard constraints (`FR-SMART-02`, `FR-SMART-03`)**
- No `Switch`, no `Slider`, no device list, no "Scan" button — not even disabled ones.
- No network call, no permission request, no Bluetooth or mDNS usage.
- A test asserts the composition contains no node with `Role.Switch` or a
  `ProgressBarRangeInfo` (`T-SMART-01`).

**States.** Static content only.

**Acceptance.** `AC-SMART-01..03`. **Tests.** `T-SMART-01..03`.

---

## SCR-SETTINGS — Settings

**Layout.** Title "Settings", then grouped `CMP-ROW`s.

| Group | Row | Control / destination |
|---|---|---|
| General | Show introduction again | Action → resets the flag, navigates to `SCR-ONBOARDING` |
| General | Keep torch on in background | `Switch`, default **off** |
| Display | Display (3D environment, quality, motion) | → `SCR-SETTINGS-DISPLAY` (`ADR-025`) |
| Privacy | Permissions | → `SCR-SETTINGS-PERMISSIONS` |
| About | About this app | → `SCR-ABOUT` |

**No Audio group** (`FR-SET-01`) — the app emits no sound and has no vibration
feature, so the board's "Audio — Sounds & Vibration" row would be a dead control.

**Keep torch on in background.** When enabled, leaving a torch screen with the torch
on starts a foreground service with a `CMP`-styled notification carrying a **Stop**
action. The toggle's supporting text states this plainly: "A notification will stay in
your status bar while the torch is on." This is the only foreground service in the app.
[Recommended]

**States.** Content only; preference reads fall back to defaults on failure.

**Acceptance.** `AC-SET-01..02`. **Tests.** `T-SET-01..02`.

---

## SCR-SETTINGS-DISPLAY — Display settings

Two `CMP-SEGMENTED` groups: Default environment (Dark | Studio) and Default quality
(Performance | Balanced | High quality), each with a `bodyMedium` explanation of what
the setting changes. Below quality, a `CMP-BANNER` info noting that the app may
temporarily lower quality when the device gets hot or enters battery saver
(`T-3D-09`, `T-3D-10`).

**Quality not yet chosen.** Until the user picks a mode, no segment is selected and a
line explains that the simulator picks one for the device the first time it opens
(`FR-3D-11`). A default is never shown as if it had been chosen.

**Motion group.** A `CMP-ROW` switch, *Reduce motion*. Effective reduce-motion is
"system animator scale is 0 **or** this switch is on" (`ADR-025`). When the system has
animations removed, the switch shows on, is disabled, and its supporting text says
"Animations are turned off in your device settings". The effective value drives
`LocalMotion`/`LocalReduceMotion` app-wide.

**Phase 1–2 note.** Until the 3D gallery ships (task 3.9), an info banner states that
the simulator isn't in this build yet and that the choices are saved for when it is.

**States.** `Loading` (renders nothing beyond the title — the store resolves in
milliseconds and a spinner would only flash) → `Content`. Values come only from the
store; a failed write leaves the previous value showing.

**Tests.** `T-SET-06`, `T-A11Y-01`, `T-A11Y-03`.

---

## SCR-SETTINGS-PERMISSIONS — Permissions

v1.0 requests **no runtime permissions**. This screen says so, explicitly, and
explains the two things a user might expect to see:

- "Camera — not requested. Your flashlight is controlled without camera access."
- "Location, storage, microphone — not requested. This app doesn't use them."

Then a single outlined button "Open system app settings" launching
`ACTION_APPLICATION_DETAILS_SETTINGS` for the app, so a user who wants to verify can.

**If a future phase adds a permission, this screen is the place it is explained**
before it is requested.

**Acceptance.** Supports `AC-GLOBAL-01`. **Tests.** `T-SET-05`.

---

## SCR-ABOUT — About

App icon, name, `BuildConfig.VERSION_NAME` + `(VERSION_CODE)`, then rows:
Privacy policy (in-app `CMP-SHEET` with the bundled policy text — no network),
Open-source licences → `SCR-LICENCES`, Help & support (in-app text with a
`mailto:` intent, wrapped in a `try/catch` for devices with no mail client).

**Acceptance.** `AC-SET-03`. **Tests.** `T-SET-03`.

---

## SCR-LICENCES — Open-source licences

A scrollable list of every third-party component in the release build, each with its
licence, followed by one card per licence whose full text expands on request. Texts
are bundled as raw resources; nothing is fetched.

The list is hand-curated and **verified at build time** against the release runtime
classpath (`ADR-023`, `T-SET-07`): every shipped module must be claimed by exactly one
entry, and no entry may name something that does not ship. Google Filament
(Apache 2.0) and the Inter font (SIL OFL 1.1) are added in the same change that adds
them to the build (Phases 3 and 4). Non-empty assertion in `T-SET-04`.

**States.** The component list is static and always renders. Each licence text is
`Loading` → `Loaded`, or `Unavailable` with an error banner; a text failure never
hides the attribution.

---

## Cross-screen state matrix

| State | Where it lives | Survives rotation | Survives process death | Persisted to disk |
|---|---|---|---|---|
| Onboarding completed | DataStore | ✓ | ✓ | ✓ |
| Torch on/off | `TorchController` (process singleton) + OS | ✓ | ✗ (OS turns it off) | ✗ |
| Screen-light colour / brightness | DataStore (debounced) | ✓ | ✓ | ✓ |
| Emergency pattern running | ViewModel only | ✗ (stopped on pause) | ✗ | ✗ |
| Emergency selected pattern/output/speed | `SavedStateHandle` | ✓ | ✓ | ✗ |
| Photosensitivity acknowledged | DataStore | ✓ | ✓ | ✓ |
| Simulator object settings | DataStore per object | ✓ | ✓ | ✓ |
| Simulator rotation | `SavedStateHandle` | ✓ | ✓ | ✗ |
| Environment / quality | DataStore | ✓ | ✓ | ✓ |
| Bottom-nav tab + per-tab back stack | Navigation `saveState` | ✓ | ✓ | ✗ |

Transient states that **must not** persist: any running light, any error banner, any
in-flight loading state.
