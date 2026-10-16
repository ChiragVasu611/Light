# TESTING_AND_ACCEPTANCE.md — All-in-One Lighting

Every functional requirement has at least one verifiable acceptance criterion and at
least one test. No requirement says "works properly".

---

## 1. Test pyramid and where things run

| Layer | Runs on | Framework | What it covers |
|---|---|---|---|
| **Unit** | JVM (`test/`) | JUnit4, kotlinx-coroutines-test, Turbine | Pure logic: pattern timing, quaternion/transform math, colour conversion, preference mapping, error mapping |
| **State-holder** | JVM (`test/`) | JUnit4 + fakes | ViewModel state transitions against fake hardware |
| **Robolectric** | JVM (`test/`) | Robolectric 4.17 | Manifest assertions, resource/theme checks, things needing a shallow Android runtime |
| **Compose UI** | Device/emulator (`androidTest/`) | `compose-ui-test-junit4` | Screen rendering, states, semantics, navigation |
| **Renderer integration** | Device (`androidTest/`) | JUnit4 + Filament | Engine init, model load, create/destroy cycles |
| **Macrobenchmark** | Device (`:benchmark`) | `benchmark-macro-junit4` 1.5.0 | Startup, frame timing, jank |
| **Manual device matrix** | Real devices | Checklist (§9) | Actual torch, actual light output, OEM behaviour |

**Deliberate split:** anything that *can* be a JVM test must be. The 3D light-sync
math (`T-3D-01`) is a JVM test because `OrbitController` and `LightRig` expose pure
functions over quaternions with no Filament types in their signatures. Only the
Filament binding itself needs a device.

---

## 2. Fakes

Hardware and rendering sit behind interfaces
([TECHNICAL_ARCHITECTURE.md](TECHNICAL_ARCHITECTURE.md) §11) so every failure path is
reachable deterministically.

| Fake | Capabilities |
|---|---|
| `FakeTorchController` | `emitState(TorchState)`, `failNextWith(TorchError)`, records every `setEnabled`/`setStrength` call with arguments and order |
| `FakeDeviceCapabilities` | data class; defaults to "capable", each field overridable |
| `FakeScreenLightController` | records `applyBrightness`/`releaseBrightness`/`keepScreenOn` calls |
| `FakeRenderer` | records scene-graph operations and computes world transforms with the real math, **without a GPU** |
| `FakePreferencesRepository` | in-memory `MutableStateFlow`, can be made to fail reads/writes |
| `TestTimeSource` | virtual clock for the emergency scheduler and flicker |
| `FakeSmartLightProvider` | Phase 6 only; unused in v1.0 production code |

Hand-written fakes are preferred over MockK for anything stateful: they are readable,
they assert call order naturally, and they do not silently accept a method signature
change.

---

## 3. Test inventory

### Global

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-GLOBAL-01` | Control audit: every interactive node is `functional`, `disabled-with-reason`, or `coming-soon` | UI | `AC-GLOBAL-01` |
| `T-GLOBAL-02` | No hard-coded string literal reaches `Text()` outside previews | Static | `AC-GLOBAL-03` |
| `T-GLOBAL-03` | All v1.0 screens render with no network available | UI | `AC-GLOBAL-04` |
| `T-GLOBAL-04` | No `WorkManager`/`AlarmManager`/`JobScheduler` in the release classpath | Static | `NFR-BAT-02` |
| `T-ARCH-01` | `domain`/`core` import no `feature`/`ui`/Android-framework types | Static | layering |
| `T-DS-01` | `darkColorScheme` mapping matches the design-system table exactly | Unit | `UI_DESIGN_SYSTEM.md` §2.2 |
| `T-DS-02` | No raw `Color(0x…)`/`N.dp`/`N.sp` literals in `feature/**` | Static | token discipline |
| `T-DEP-01` | No networking or analytics artifact in the release runtime classpath | Static | privacy §3 |
| `T-LOG-01` | No direct `android.util.Log` outside `AppLogger` | Static | privacy §7 |
| `T-PERM-01` | Merged release manifest declares only permissions in the approved table | Robolectric | privacy §1 |
| `T-PERM-02` | No permission requested during onboarding or at app start | UI | `FR-ONB-05` |
| `T-PERM-03` | Denying `POST_NOTIFICATIONS` reverts the background-torch toggle, shows an explanation, and does not crash or re-prompt | UI | permission matrix, brief criterion 7 |

### Onboarding

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-ONB-01` | Clean install → onboarding page 1 | UI | `AC-ONB-01` |
| `T-ONB-02` | Get started → Home; relaunch → Home | UI | `AC-ONB-02` |
| `T-ONB-03` | Skip → Home; relaunch → Home | UI | `AC-ONB-03` |
| `T-ONB-04` | No permission dialog during onboarding | UI | `AC-ONB-04` |
| `T-ONB-05` | Page index survives rotation | UI | edge case |
| `T-ONB-06` | DataStore write failure still navigates to Home | Unit | edge case |

### Home

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-HOME-01` | Status line text for each of the 7 `TorchState` variants | State-holder | `FR-HOME-02` |
| `T-HOME-02` | No flash unit → card disabled, reason shown, zero torch calls on tap | UI + state | `AC-HOME-02` |
| `T-HOME-03` | Low-battery banner appears below 5 % and does **not** block the toggle | UI | `ADR-007` |
| `T-NAV-01` | All six Home targets open their documented destination | UI | `AC-HOME-04` |
| `T-NAV-02` | Bottom-nav tab switching preserves each tab's back stack | UI | navigation |
| `T-NAV-03` | Back from a full-screen light stops the light, then pops | UI | `FR-GLOBAL-05/06` |

### Torch

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-TORCH-01` | Toggle on → `setEnabled(true)` called once; UI goes `On` **only after** the callback | State-holder | `AC-TORCH-01`, `FR-GLOBAL-02` |
| `T-TORCH-02` | `NoFlashUnit` → unavailable state, toggle not actuable | State-holder + UI | `AC-TORCH-02` |
| `T-TORCH-03` | `CameraInUse` → conflict message + Retry, state stays `Off` | State-holder | `AC-TORCH-03` |
| `T-TORCH-04` | Always-failing controller never produces an `On` UI state | State-holder | `AC-TORCH-04`, `AC-GLOBAL-02` |
| `T-TORCH-05` | Unsolicited `onTorchModeChanged(false)` → UI `Off` | State-holder | `AC-TORCH-05` |
| `T-TORCH-06` | `maxStrength == 1` → no slider composed; `== 5` → slider with 5 steps | UI | `AC-TORCH-06` |
| `T-TORCH-07` | `ON_STOP` with opt-in off → exactly one `setEnabled(false)` | State-holder | `AC-TORCH-07` |
| `T-TORCH-08` | `DisabledByPolicy` → policy message, no retry action | State-holder | failure matrix |
| `T-TORCH-09` | Rotation does not toggle the torch | UI | singleton ownership |

### Screen light

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-SCREEN-01` | Preset tap → surface colour equals the preset value | UI | `AC-SCREEN-01` |
| `T-SCREEN-02` | `Settings.System.putInt` never referenced in the feature | Static | `AC-SCREEN-02` |
| `T-SCREEN-03` | Rotation preserves colour, mode, brightness | UI | `AC-SCREEN-03` |
| `T-SCREEN-04` | Relaunch restores last colour and brightness | UI | `AC-SCREEN-04` |
| `T-SCREEN-05` | Controls hide after 4 s; tap restores without changing colour | UI | `AC-SCREEN-05` |
| `T-SCREEN-06` | `keepScreenOn(true)` on resume, `(false)` on pause | State-holder | `AC-SCREEN-06` |
| `T-SCREEN-07` | Auto-hide disabled when touch exploration is on | UI | accessibility |
| `T-SCREEN-08` | Brightness slider drag issues debounced writes, not one per pixel | State-holder | performance |

### Candle

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-CANDLE-01` | Flicker multiplier stays in `[0.88, 1.12]` over 30 s virtual time | Unit | `AC-CANDLE-01` |
| `T-CANDLE-02` | Reduce motion → steady colour, zero flicker updates | Unit | `NFR-A11Y-04` |

### Emergency

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-EMER-01` | SOS emits 3 short, 3 long, 3 short with exact documented durations on a virtual clock | Unit | `AC-EMER-01` |
| `T-EMER-02` | No speed preset yields a period < 200 ms | Unit | `AC-EMER-02` |
| `T-EMER-03` | STOP halts within one tick; exactly one `setEnabled(false)` | State-holder | `AC-EMER-03` |
| `T-EMER-04` | No flash unit → Torch/Both disabled with reason, Screen pre-selected | UI | `AC-EMER-04` |
| `T-EMER-05` | `ON_STOP` stops pattern and torch | State-holder | `AC-EMER-05` |
| `T-EMER-06` | Photosensitivity dialog on first strobe only | UI | `AC-EMER-06` |
| `T-EMER-07` | Disclaimer present whenever the screen is shown | UI | `AC-EMER-07` |
| `T-EMER-08` | Timing is clock-driven, not frame-driven: a 10× virtual-clock advance produces the expected step count | Unit | `FR-EMER-09` |
| `T-EMER-09` | Torch failure mid-pattern falls back to Screen output and shows a banner, rather than stopping dead | State-holder | error handling |

### More tools

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-TOOLS-01` | List contains exactly the 5 launch rows; Party/Music absent | UI | `AC-TOOLS-01` |
| `T-TOOLS-02` | SOS Light row opens Emergency with SOS preselected | UI | `AC-TOOLS-02` |

### 3D simulator

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-3D-01` | Yaw 90° → beam world direction rotates from `(0,0,−1)` to `(−1,0,0)` ±1° | **Unit** | `AC-3D-01`, `FR-3D-05` |
| `T-3D-02` | `OBJ-BULB` creates a POINT light; no spot light is ever created for it, at any orientation | Unit | `AC-3D-02`, `FR-3D-06` |
| `T-3D-03` | Dark↔Studio switch preserves brightness, beam, temperature, object, rotation | State-holder | `AC-3D-03` |
| `T-3D-04` | 20 enter/exit cycles: all engine objects destroyed in order; no monotonic native growth | Instrumented | `AC-3D-04`, `NFR-MEM-02` |
| `T-3D-05` | Corrupt/missing `.glb` → error state with Retry; Retry re-attempts | State-holder + instrumented | `AC-3D-05` |
| `T-3D-06` | Reset → identity quaternion and default camera pose | Unit | `AC-3D-06` |
| `T-3D-07` | Candle composition has no beam-angle control; torch has no flame control | UI | `AC-3D-07`, `FR-3D-16` |
| `T-3D-08` | `supportsGlEs3 == false` → gallery unsupported state; rest of app usable | UI | `NFR-COMPAT-02` |
| `T-3D-09` | Battery saver → quality drops to Performance + banner | State-holder | degradation |
| `T-3D-10` | Thermal `SEVERE` → Performance + 30 fps cap + banner | State-holder | degradation |
| `T-3D-11` | `TRIM_MEMORY_RUNNING_CRITICAL` → IBL/skybox released, model retained | Instrumented | degradation |
| `T-3D-12` | `isLowRamDevice` → Performance selected initially | Unit | quality selection |
| `T-3D-13` | Pitch clamped to ±85° (candle ±35°); yaw wraps through 360° without flip | Unit | `ADR-015` |
| `T-3D-14` | Tube light creates the documented segment count per quality mode | Unit | `FR-3D-07` |
| `T-3D-15` | Missing `LIGHT_ANCHOR` → `AssetLoad.MissingAnchor` error state, not a crash | Unit | §10 failure table |
| `T-3D-16` | Animation advances by elapsed time: half the frame rate yields the same phase at the same wall-clock | Unit | frame independence |
| `T-3D-17` | Render loop issues zero frames while paused | Instrumented | `FR-3D-13` |
| `T-3D-18` | Out-of-range persisted brightness is coerced into the object's documented range | Unit | validation |

### Settings / About / Smart light

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-SET-01` | No Audio group in Settings | UI | `AC-SET-01` |
| `T-SET-02` | Show introduction again → onboarding shows on relaunch | UI | `AC-SET-02` |
| `T-SET-03` | About version equals `BuildConfig.VERSION_NAME` | UI | `AC-SET-03` |
| `T-SET-04` | Licences list non-empty and lists shipped components; contains Filament once Filament ships (Phase 3, `ADR-023`) | UI | `AC-SET-04` |
| `T-SET-05` | Permissions screen states no runtime permissions are requested | UI | `SCR-SETTINGS-PERMISSIONS` |
| `T-SET-06` | Display settings reflect only the store: selections persist, a failed write leaves the old value, system-removed animations force reduce-motion and disable the switch | Unit + UI | `FR-SET-03`, `ADR-025` |
| `T-SET-07` | Every module in the release runtime classpath is claimed by exactly one licences entry, and no entry names something that does not ship | Static | `FR-SET-06`, `ADR-023` |
| `T-SMART-01` | Smart Light composition contains no `Role.Switch` and no `ProgressBarRangeInfo` | UI | `AC-SMART-01` |
| `T-SMART-02` | Every Smart Light entry point includes "Coming soon" in its semantics | UI | `AC-SMART-02` |
| `T-SMART-03` | `INTERNET` not in the merged manifest | Robolectric | `AC-SMART-03` |

### Lifecycle / process death

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-LIFE-01` | Process death in Screen Light → restores with persisted colour/brightness | UI | cross-cutting table |
| `T-LIFE-02` | Process death during an emergency pattern → restores **Idle**, not running | UI | cross-cutting table |
| `T-LIFE-03` | Process death in the simulator → object + settings restored, model reloads with loading state | UI | cross-cutting table |
| `T-LIFE-04` | Simulated interruption during strobe stops pattern and torch | State-holder | cross-cutting table |

### Accessibility

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-A11Y-01` | Every clickable node ≥ 48×48 dp | UI | `NFR-A11Y-01` |
| `T-A11Y-02` | Computed contrast of every token pair meets its documented ratio | Unit | `NFR-A11Y-02` |
| `T-A11Y-03` | Every non-decorative node has a content description or merged label | UI | `NFR-A11Y-03` |
| `T-A11Y-04` | Animator duration scale 0 → all documented animations are 0 ms | UI | `NFR-A11Y-04` |
| `T-A11Y-05` | Font scale 2.0 on Home → no clipped text; grid reflows | UI | edge case |
| `T-A11Y-06` | RTL locale mirrors layout; colour wheel and 3D gestures do not mirror | UI | edge case |
| `T-A11Y-07` | Simulator viewport exposes Rotate left/right/Reset custom actions | UI | `SCR-SIM-VIEWER` |

### Performance / release

| ID | Test | Level | Verifies |
|---|---|---|---|
| `T-PERF-01` | Cold start trace contains no artificial delay; p50 within budget | Macrobenchmark | `AC-ONB-05`, `NFR-PERF-01` |
| `T-PERF-02` | Simulator frame timing: p95 frame ≤ 18 ms, jank < 2 % | Macrobenchmark | `NFR-PERF-04/05` |
| `T-PERF-03` | Model load p50 ≤ 1500 ms | Macrobenchmark | `NFR-PERF-03` |
| `T-REL-01` | **Release (R8) build** launches, Home renders, simulator loads a model | Instrumented, release variant | Filament keep rules (`TECHNICAL_ARCHITECTURE.md` §12) |
| `T-REL-02` | Release bundle download size within budget | Script | `NFR-SIZE-01` |
| `T-MON-01` | No billing dependency, no currency symbol in strings | Static | `AC-MON-01` |

`T-REL-01` exists because Filament's JNI resolves Java members by name: R8 without the
keep rules produces a crash that **only** appears in release builds. A debug-only test
suite would never catch it.

---

## 4. Test naming and structure

```kotlin
@Test
fun `toggle on with failing controller never reaches On state`() { … }
```

- Backtick names describing behaviour, not method names.
- Arrange / Act / Assert with blank-line separation.
- One behaviour per test; no `if` branches inside a test.
- Flow assertions use Turbine (`awaitItem()` / `expectNoEvents()`), never
  `delay()`-then-read.
- Coroutine tests use `runTest` with an injected `TestDispatcher`; production code
  never hard-codes a dispatcher.

---

## 5. Commands

```bash
./gradlew assembleDebug                      # compile
./gradlew testDebugUnitTest                  # JVM unit + state-holder + Robolectric
./gradlew connectedDebugAndroidTest          # Compose UI + renderer integration (device)
./gradlew lintDebug                          # Android Lint
./gradlew :app:assembleRelease               # R8 release build
./gradlew :benchmark:connectedBenchmarkAndroidTest   # macrobenchmark (Phase 4)
./gradlew check                              # unit tests + lint
```

A task is complete only when the commands relevant to it have been **run** and their
**actual output** reported. Claiming a test passes without executing it is a
process violation ([CLAUDE_CODE_IMPLEMENTATION_GUIDE.md](CLAUDE_CODE_IMPLEMENTATION_GUIDE.md) §4).

---

## 6. Coverage expectations

| Area | Target | Enforced from |
|---|---|---|
| `domain/**` | ≥ 90 % line | Phase 2 |
| `feature/**` ViewModels | ≥ 80 % line | Phase 2 |
| `data/**` mapping logic | ≥ 80 % line | Phase 2 |
| `render/**` pure math | ≥ 90 % line | Phase 3 |
| `ui/components/**` | covered by UI tests, no line target | — |
| Overall `:app` | ≥ 70 % line | Phase 4 exit |

Coverage is a floor, not a goal; a test that asserts nothing meaningful is worse than
no test.

---

## 7. Failure scenarios that must be tested

Each of these has a test above; listed together because they are the ones most likely
to be skipped:

1. Torch hardware absent
2. Torch command fails every time
3. Torch turned off by another app mid-session
4. Camera held by another app
5. Flash strength API absent (the common case)
6. Device policy disables the flashlight
7. 3D model file missing
8. 3D model file corrupt
9. 3D model missing its `LIGHT_ANCHOR`
10. Filament engine fails to initialise
11. GL ES 3.0 unavailable
12. Low-memory trim while rendering
13. Thermal throttling while rendering
14. Battery saver engaged
15. DataStore read failure / corrupt preferences
16. Out-of-range persisted values
17. Process death in every stateful screen
18. `POST_NOTIFICATIONS` denied
19. Font scale 200 %
20. RTL locale
21. Release build with R8 (the Filament keep-rule case)

---

## 8. CI

| Stage | Runs | Gate |
|---|---|---|
| 1 | `assembleDebug` | must compile |
| 2 | `testDebugUnitTest` | zero failures |
| 3 | `lintDebug` | zero errors; warnings reviewed |
| 4 | static checks (`T-DS-02`, `T-DEP-01`, `T-LOG-01`, `T-ARCH-01`, `T-GLOBAL-02/04`) | zero violations |
| 5 | `assembleRelease` | must build with R8 |
| 6 | `connectedDebugAndroidTest` on an emulator | zero failures |
| 7 | size check (`T-REL-02`) | within budget |

Stages 1–5 run on every change. Stage 6 runs on an API 34 emulator; **emulator passes
do not substitute for the real-device matrix** in §9, because the emulator has no
real flash hardware and an atypical GPU.

---

## 9. Manual device matrix

Automated tests cannot verify that light actually came out of the phone.

| Class | Example | Must verify |
|---|---|---|
| Low-end, ≤ 4 GB RAM, API 24–28 | budget device | App launches; torch; simulator in Performance mode or the unsupported state; no OOM |
| Mid-tier, 6–8 GB, API 31–33 | mainstream device | All features; Balanced 55 fps; flash strength API presence |
| High-end, ≥ 8 GB, API 34+ | flagship | High quality mode; 120 Hz display; thermal behaviour under 10 min of rendering |
| No flash unit | tablet | Torch UI disabled with reason; everything else works |
| OEM with aggressive background management | Xiaomi / Oppo / Vivo / Samsung | Background torch opt-in; service survival or graceful failure |
| Foldable | — | Layout across fold/unfold; simulator surface recreation |

### Devices actually available (verified 2026-10-16)

| Device | Class | Android | ABI | RAM | Width | GL ES | Flash | Flash strength API |
|---|---|---|---|---|---|---|---|---|
| **OPPO CPH2127** (phone) | **Low** | 12 (API 31) | arm64-v8a | 3.56 GB | Compact | **3.2** | **yes** | **no** — API 31 < 33 |
| **Samsung SM-X110** (tablet) | **Low** | 15 (API 35) | arm64-v8a | 3.83 GB | **Medium (621 dp)** | **3.2** | **no** | n/a |

This device is a genuinely useful matrix entry: it is Low-class, has a flash unit, is
below API 33 so it cannot expose flash strength (confirming `LIM-FLASH-STRENGTH`
firsthand), supports GL ES 3.2 so Filament will run, and is an OPPO/ColorOS build —
one of the OEMs named in `LIM-OEM-BACKGROUND`.

Between them these two cover a lot: a phone with a flash below API 33, and a
**tablet with no flash unit at all** on API 35 — the device class needed to verify
`AC-HOME-02`, `FR-HOME-07` and `FR-TORCH-07` on real hardware rather than only
against a fake. The tablet also confirms that `uses-feature required="false"` was the
right call: the app installs and runs there, and every non-torch feature works.

The tablet is also the only **Medium** width-class device available, and finding it
immediately exposed an unimplemented responsive rule (see `ScreenContentTest`).

Still needed: a Mid or High device on API 33+ that actually reports
`FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1`, since neither device here can exercise the
flash-strength path at all.

Per-device checklist: cold start feel · torch on/off and physical light confirmed ·
torch state after backgrounding · screen light at min and max brightness · SOS visually
correct · strobe warning appears once · each 3D object loads, rotates, and its light
follows · environment switch preserves settings · leave and re-enter the simulator 10× ·
10 minutes of rendering for thermal behaviour · TalkBack sweep of every screen ·
font scale 200 % sweep.

---

## 10. Release gates

A build ships only when all are green:

| # | Gate | Evidence |
|---|---|---|
| 1 | `assembleDebug` and `assembleRelease` succeed | build output |
| 2 | `testDebugUnitTest` — zero failures | test report |
| 3 | `connectedDebugAndroidTest` — zero failures | test report |
| 4 | `lintDebug` — zero errors | lint report |
| 5 | All static checks pass | check output |
| 6 | `T-REL-01` passes on a **release** build on a real device | run log |
| 7 | Every `L` requirement has an implementation, a test, and a traceability row | [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) |
| 8 | Control audit (`T-GLOBAL-01`): zero no-op controls | audit output |
| 9 | Manual device matrix (§9) complete on ≥ 4 devices incl. one without flash | signed checklist |
| 10 | Performance budgets measured, not assumed; deviations documented | benchmark output |
| 11 | Download size measured against budget | bundletool output |
| 12 | Permission audit: merged manifest matches the approved table | `T-PERM-01` |
| 13 | Accessibility sweep with TalkBack on ≥ 2 devices | checklist |
| 14 | Asset licences recorded for every model, texture and HDRI | `docs/licences/assets.md` |
| 15 | Privacy policy and Play Data Safety declaration match actual behaviour | review |
| 16 | `KNOWN_LIMITATIONS.md` current; no unresolved `[Open]` item blocks launch | review |

---

## 11. The ten required acceptance criteria

From the brief, mapped to this package:

| # | Criterion | Requirement | Test |
|---|---|---|---|
| 1 | Flashlight activates the real torch and the UI reflects the result | `FR-TORCH-01/02` | `T-TORCH-01` |
| 2 | Torch unavailable → explained, never claimed on | `FR-TORCH-07`, `FR-GLOBAL-02` | `T-TORCH-02`, `T-TORCH-04` |
| 3 | Rotating a torch changes its beam direction consistently | `FR-3D-05` | `T-3D-01` |
| 4 | Rotating a bulb does not narrow its glow into a beam | `FR-3D-06` | `T-3D-02` |
| 5 | Dark↔Studio does not reset unrelated settings | `FR-3D-09` | `T-3D-03` |
| 6 | Leaving the simulator releases rendering resources | `FR-3D-13` | `T-3D-04`, `T-3D-17` |
| 7 | Denying an optional permission does not crash | permission matrix | `T-PERM-03` (`POST_NOTIFICATIONS` denial) |
| 8 | A failed model load shows a recoverable error | `FR-3D-14` | `T-3D-05` |
| 9 | Every visible primary control works, or is marked unavailable / coming soon | `FR-GLOBAL-01` | `T-GLOBAL-01` |
| 10 | The app builds with the documented Gradle commands | §5 | CI stages 1 and 5 |
