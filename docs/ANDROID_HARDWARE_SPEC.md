# ANDROID_HARDWARE_SPEC.md — All-in-One Lighting

How the app touches real hardware, what varies across devices, and how every failure
is handled. **Governing rule: never show a successful hardware state when the
underlying operation failed** (`FR-GLOBAL-02`).

---

## 1. Camera torch

### API choice

`CameraManager.setTorchMode(cameraId, enabled)` — available since **API 23**, below
our `minSdk 24`.

**No `CAMERA` permission is required or requested** (`FR-TORCH-08`). `setTorchMode`
operates without opening a camera device and without the permission. Requesting
`CAMERA` for a flashlight is unnecessary, raises Play Store data-safety questions,
and frightens privacy-conscious users.

> The concept board shows a camera-permission dialog. It is removed. See `ADR-006`.

### Camera selection

```kotlin
fun findTorchCameraId(cm: CameraManager): String? =
    cm.cameraIdList.firstOrNull { id ->
        val c = cm.getCameraCharacteristics(id)
        c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
        c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
    } ?: cm.cameraIdList.firstOrNull { id ->
        cm.getCameraCharacteristics(id)
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }
```

Back-facing flash is preferred; any flash-capable camera is the fallback (some devices
expose a front flash). Multi-camera devices may list several flash-capable logical
cameras — we take the first match and do not attempt to drive more than one.

`cameraIdList` itself can throw `CameraAccessException`; the whole discovery is wrapped
and a failure maps to `TorchError.Unknown`.

### State is read from the OS, never from our own command

```kotlin
private val callback = object : CameraManager.TorchCallback() {
    override fun onTorchModeChanged(cameraId: String, enabled: Boolean) { … }
    override fun onTorchModeUnavailable(cameraId: String) { … }
}
cm.registerTorchCallback(callback, handler)
```

- `onTorchModeChanged` is the **only** source of the displayed on/off state.
- `onTorchModeUnavailable` fires when another app takes the camera → state becomes
  `Unavailable(CameraInUse)` (`AC-TORCH-05`).
- The callback also fires on registration with the current state, which resolves the
  initial `Unknown`.
- The callback is registered on a background `Handler` and its results are published
  into a `MutableStateFlow`.

This is the mechanism that makes `FR-GLOBAL-02` structurally true rather than a
convention someone must remember.

### Torch brightness (`FR-TORCH-03`)

Android 13 (API 33) added per-device flash strength:

| API | Capability |
|---|---|
| `CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL` | max level; **1 means no adjustment** |
| `CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL` | default level |
| `CameraManager.turnOnTorchWithStrengthLevel(id, level)` | set level (API 33+) |
| `CameraManager.getTorchStrengthLevel(id)` | read current level (API 33+) |

**Exposure rule:** the brightness slider is composed **only** when
`SDK_INT >= 33 && maxLevel > 1`. Otherwise it is *absent from the layout* — not
disabled, not pinned at 100 %. A disabled slider implies the feature exists and is
temporarily off; absence is the truthful rendering.

Most devices in the field report `maxLevel == 1`. **The majority of users will never
see this slider**, and the concept board's always-present brightness control on the
Flashlight screen is therefore not implementable as drawn. Recorded in
`LIM-FLASH-STRENGTH`.

### Beam mode — not implementable

The board shows Narrow / Medium / Wide on the Flashlight screen. **Android exposes no
API to change the flash LED's beam angle**, and the hardware has no moving optics.
The control is removed (`FR-TORCH-05`). It survives only in the 3D simulator, where
beam angle is a property of a simulated light and is genuinely adjustable.

### Failure matrix

| Condition | Detection | `TorchError` | UI |
|---|---|---|---|
| No flash hardware | `FLASH_INFO_AVAILABLE` false for all cameras, or `FEATURE_CAMERA_FLASH` absent | `NoFlashUnit` | `CMP-ERROR`, "This device doesn't have a camera flash." No retry. |
| Camera held by another app | `CameraAccessException.CAMERA_IN_USE` / `MAX_CAMERAS_IN_USE`, or `onTorchModeUnavailable` | `CameraInUse` | `CMP-BANNER` error + **Retry**. Never auto-retries in a loop. |
| Disabled by device policy | `CameraAccessException.CAMERA_DISABLED` | `DisabledByPolicy` | `CMP-ERROR`, "Your device's administrator has disabled the flashlight." No retry. |
| Camera service dead | `CameraAccessException.CAMERA_ERROR` | `Unknown` | `CMP-BANNER` + Retry |
| `setTorchMode` throws `IllegalArgumentException` | catch | `NoFlashUnit` | treat as unsupported |
| Torch turned off externally | `onTorchModeChanged(false)` | — | state → Off, no error shown |

Every branch is reachable in tests through `FakeTorchController`, which exposes
`emitState()` and `failNextWith(error)` (`T-TORCH-02..05`).

### Lifecycle (`FR-TORCH-06`)

| Event | Action |
|---|---|
| App → background, *Keep torch on* off (default) | `setTorchMode(false)` once |
| App → background, *Keep torch on* on | torch stays on; a foreground service with a **Stop** notification action starts |
| Process death | The OS turns the torch off on its own; we assume nothing and re-read state from the callback on next launch |
| Screen rotation | No effect — the controller is a process singleton, not ViewModel-scoped |

The foreground service uses type `specialUse` with a declared justification, and is
the app's only service. If the user has not enabled the opt-in, **no service exists in
the running app at all.**

---

## 2. Screen illumination

### Window brightness, never system brightness (`ADR-005`)

```kotlin
window.attributes = window.attributes.apply { screenBrightness = value } // 0.01f..1.0f
```

| Property | Window brightness | `Settings.System.SCREEN_BRIGHTNESS` |
|---|---|---|
| Permission | **none** | `WRITE_SETTINGS` (special access, user must grant in system settings) |
| Scope | this window only | whole device |
| Restored automatically | **yes**, when the window goes away | no — the app must restore it, and will fail to if it crashes |
| Risk of stranding the user | none | a crash at 1 % brightness leaves an unusable device |

We use window brightness exclusively. Consequences, all good: no permission request,
no `WRITE_SETTINGS` special-access flow, nothing to restore on exit, and no way to
leave the device in a bad state (`FR-SCREEN-05`).

`BRIGHTNESS_OVERRIDE_NONE` (`-1f`) is set on pause to hand control back to the system.

### Keeping the screen on

`window.addFlags(FLAG_KEEP_SCREEN_ON)` on resume, `clearFlags` on pause.
**No `WakeLock`, no `WAKE_LOCK` permission** — the window flag is the correct tool and
cannot leak a wakelock if the app is killed.

### Edge-to-edge and cutouts

- `enableEdgeToEdge()` in `MainActivity`; transparent system bars.
- Screen Light sets `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS` so the colour fills the
  notch region; controls still respect insets.
- The light surface draws behind the bars; the control overlay applies
  `safeDrawingPadding()`.

### Limits we do not fight

- **Battery saver** can cap maximum brightness. Our request is honoured only up to the
  cap. We do not detect and complain about this; the slider represents our request.
- **Adaptive brightness** is overridden by a window value while our window is in front,
  then resumes. Correct behaviour, nothing to do.
- **Ambient/always-on display** is unrelated and untouched.
- A screen light is **not** as bright as the camera flash, and the UI says so
  (`FR-SCREEN-08`).

---

## 3. Permission matrix

v1.0 declares **no dangerous permissions and no runtime permission requests.**

| Permission | Declared? | Why / why not |
|---|---|---|
| `CAMERA` | **No** | `setTorchMode` does not require it (`FR-TORCH-08`) |
| `INTERNET` | **No** | v1.0 is fully offline (`FR-GLOBAL-04`, `AC-SMART-03`) |
| `WRITE_SETTINGS` | **No** | window brightness is used instead (`ADR-005`) |
| `WAKE_LOCK` | **No** | `FLAG_KEEP_SCREEN_ON` is used instead |
| `RECORD_AUDIO` | **No** | Music Light is deferred (`OQ-02`) |
| `ACCESS_FINE_LOCATION` | **No** | nothing needs location. (Note: Wi-Fi scanning for smart-light discovery would need it — a Phase 6 concern, see [SMART_LIGHT_FUTURE_SPEC.md](SMART_LIGHT_FUTURE_SPEC.md)) |
| `BLUETOOTH_*` | **No** | Phase 6 at the earliest |
| `POST_NOTIFICATIONS` | **Conditional** | Only if *Keep torch on in background* ships. Requested at the moment the user enables that toggle — never before. Denial disables the toggle with an explanation; the torch still works normally in the foreground. |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | **Conditional** | Same gate as above. Normal permissions, no runtime prompt. |

**Permission request timing rule:** nothing is requested during onboarding
(`FR-ONB-05`), and nothing is requested on app start. The only possible runtime
request in v1.0 is `POST_NOTIFICATIONS`, triggered by the user deliberately enabling
an opt-in setting, with the rationale shown in the toggle's own supporting text
before the system dialog appears.

### If a permission is denied

| Permission | Denied once | Denied permanently |
|---|---|---|
| `POST_NOTIFICATIONS` | Toggle reverts to off; `CMP-BANNER` explains the background torch needs a notification | Same, plus a link to system app settings. **No crash, no repeated prompting** (`AC-PERM-01`) |

---

## 4. Hardware feature declarations

```xml
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
<uses-feature android:name="android.hardware.camera"       android:required="false" />
<uses-feature android:glEsVersion="0x00030000"             android:required="false" />
```

All `required="false"` — a device without a flash still gets screen light, emergency
screen modes and (if it has GL ES 3.0) the simulator. Marking any of these `required`
would needlessly exclude devices from the Play Store listing.

---

## 5. Device variation and compatibility

| Area | Variation | Our handling |
|---|---|---|
| Flash presence | Many tablets and some budget phones have none | `NoFlashUnit` state; torch UI disabled with a reason (`AC-HOME-02`) |
| Flash strength API | Added API 33; most devices report `maxLevel == 1` even on 33+ | Slider hidden unless `maxLevel > 1` (`LIM-FLASH-STRENGTH`) |
| Multi-camera | Several flash-capable logical cameras | First back-facing match; single torch only |
| Camera conflict | Any app holding the camera blocks the torch | `CameraInUse` + Retry, no auto-retry loop |
| Manufacturer skins | Aggressive background killing (Xiaomi, Oppo, Vivo, Samsung) can kill the foreground service | Documented in `LIM-OEM-BACKGROUND`; the in-foreground torch is unaffected |
| Screen brightness cap | Battery saver / thermal limits | Request honoured up to the cap; no complaint UI |
| GPU / GL ES | Some very old or stripped devices lack GL ES 3.0 | Simulator shows `Unsupported`; rest of the app works (`NFR-COMPAT-02`, `T-3D-08`) |
| Vulkan vs GL backend | Driver quality varies | GL ES backend in v1.0 (`OQ-06`) |
| Low-RAM devices | `ActivityManager.isLowRamDevice` | Performance quality forced; `T-3D-12` |
| Thermal throttling | API 29+ `getCurrentThermalStatus`; absent below 29 | Degrade with a banner where available; below API 29 rely on the fps cap only (`LIM-THERMAL-API`) |
| Battery saver | `PowerManager.isPowerSaveMode` | Drop to Performance with a banner (`T-3D-09`) |
| Foldables / large screens | Window size changes at runtime | `WindowSizeClass`-driven layout; the simulator surface is recreated on configuration change |
| 90/120 Hz displays | Frame budget varies | Animation is time-based, never frame-count-based (`THREE_D_LIGHTING_SPEC.md` §8) |

---

## 6. Battery

| Mode | Dominant draw | Our obligation |
|---|---|---|
| Torch on | LED, 100–400 mW | None beyond not holding it on accidentally — `ON_STOP` turns it off by default |
| Screen light | Display backlight, dominates everything else | No busy loop. The light is a static `Color` fill; Compose does not redraw a static colour. |
| Candle flicker | Display + ~4 recompositions/sec | Flicker is capped at 4 Hz (`FR-CANDLE-01`) and is not an animation frame loop |
| Emergency strobe | LED + display, pulsed | Coroutine `delay`, not a spin loop; stopped on pause |
| 3D simulator | GPU, by far the largest | Render loop stops on pause (`FR-3D-13`); frame caps per quality mode; thermal degradation |

**Background work: none.** v1.0 schedules no `WorkManager` job, no `AlarmManager`
alarm, no `JobScheduler` job (`NFR-BAT-02`, enforced by `T-GLOBAL-04`). The single
optional foreground service exists only while the user has deliberately left the torch
on in the background.

---

## 7. Hardware abstraction (testability)

```kotlin
interface TorchController {
    val state: StateFlow<TorchState>
    suspend fun setEnabled(enabled: Boolean): AppResult<Unit>
    suspend fun setStrength(level: Int): AppResult<Unit>
    fun refresh()
}

interface DeviceCapabilities {
    val hasFlashUnit: Boolean
    val maxTorchStrength: Int        // 1 ⇒ not adjustable
    val supportsGlEs3: Boolean
    val isLowRamDevice: Boolean
    val totalMemoryBytes: Long
}

interface ScreenLightController {
    fun applyBrightness(value: Float)   // 0.01f..1.0f
    fun releaseBrightness()
    fun keepScreenOn(on: Boolean)
}
```

Real implementations live in `data/hardware/`. Fakes live in `src/test/`:
`FakeTorchController` (scriptable states + injectable failures),
`FakeDeviceCapabilities` (data class with defaults),
`FakeScreenLightController` (records every call for assertion).

This is what lets `T-TORCH-02..07`, `T-SCREEN-02/06` and `T-EMER-03/04` run as fast
JVM tests with no device, while still exercising the exact branches that matter.

---

## 8. Hardware rules for implementers

1. Never cache "what I asked for" as the displayed state. Display only what the OS
   reported.
2. Never auto-retry a hardware call in a loop. Offer the user a Retry action.
3. Never request a permission you can avoid. Measure the alternative first.
4. Never mark a `uses-feature` as required unless the app is useless without it.
5. Never leave a system setting modified. Prefer window-scoped APIs.
6. Always wrap platform calls and map exceptions to a typed `AppError` with a string
   resource.
7. Always hide — not disable — a control for a capability the device lacks entirely.
8. Always verify a capability through `DeviceCapabilities`, never through a
   manufacturer or model-name check.
