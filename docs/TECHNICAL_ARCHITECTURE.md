# TECHNICAL_ARCHITECTURE.md — All-in-One Lighting

## 1. Technical baseline (verified 2026-10-15)

All versions below were resolved from Maven Central / Google's Maven on 2026-10-15
by querying `maven-metadata.xml` and filtering out alpha/beta/rc builds. They are
pinned in `gradle/libs.versions.toml`.

| Component | Version | Notes |
|---|---|---|
| Gradle | 9.4.1 | wrapper, `bin` distribution, checksum-pinned. Matches AGP's version line — see `ADR-021` |
| Android Gradle Plugin | 9.4.1 | latest stable |
| Kotlin | 2.4.21 | latest stable |
| Compose Compiler plugin | 2.4.21 | version-locked to Kotlin |
| Compose BOM | 2026.09.00 | all `androidx.compose.*` versions come from the BOM |
| Navigation Compose | 2.10.2 | |
| Lifecycle (runtime/viewmodel-compose) | 2.11.0 | |
| Activity Compose | 1.13.0 | |
| Core KTX | 1.19.1 | |
| Core SplashScreen | 1.2.0 | |
| DataStore Preferences | 1.2.1 | |
| Coroutines | 1.11.0 | |
| **Filament** | **1.77.3** | `filament-android`, `gltfio-android`, `filament-utils-android` all at 1.77.3 |
| JUnit4 / androidx.test.ext:junit | 1.3.0 | |
| Espresso | 3.7.0 | |
| Turbine | 1.2.1 | Flow testing |
| MockK | 1.14.11 | used sparingly; prefer hand-written fakes |
| Robolectric | 4.17 | JVM-side Android tests |
| Macrobenchmark | 1.5.0 | Phase 4 |

**JDK:** 17 (Zulu 17.0.19 present on the build machine).
**Android SDK:** platforms 34/35/36/36.1 and build-tools up to 37.0.0 present.

### SDK levels (`ADR-002`)

| Setting | Value | Rationale |
|---|---|---|
| `minSdk` | **24** (Android 7.0) | Filament's AAR declares `minSdkVersion 21`, so 24 is comfortably supported. 24 gives us modern `CameraManager.TorchCallback` (API 23+), `setTorchMode` (API 23+), and avoids a long tail of pre-Nougat graphics driver bugs. Covers > 99 % of active devices. |
| `compileSdk` | **36** | Latest installed stable platform. |
| `targetSdk` | **36** | Required for Play Store compliance at submission time; re-verify the required minimum before release. |

API-gated features: flash strength (API 33+), `PowerManager.getCurrentThermalStatus`
(API 29+), `enableEdgeToEdge` behaviour differences (API 29/35). Each is guarded with
an explicit `Build.VERSION.SDK_INT` check behind a capability interface, never
scattered across the UI.

---

## 2. Module structure (`ADR-001`)

A **single Gradle module** (`:app`) with strict package-level layering, plus a
separate `:benchmark` module added in Phase 4.

```
:app
:benchmark          (Phase 4, macrobenchmark only)
```

Multi-module Gradle was considered and rejected: this is a single-purpose app with
one team, no dynamic feature delivery in v1.0, and no shared library consumers.
Module boundaries would add build complexity without buying isolation we need.
Layering is instead enforced by package structure plus a dependency-direction test
(`T-ARCH-01`) that fails if `core` or `domain` imports anything from `feature`
or `ui`.

If 3D asset packs become a Play Asset Delivery feature (Phase 5), **that** is when a
second module appears. Recorded in `ADR-001`.

### Package layout

```
com.allinonelighting
├── AllInOneLightingApp.kt          Application; builds AppContainer
├── MainActivity.kt                 single Activity; edge-to-edge; NavHost host
│
├── core/
│   ├── result/                     AppResult<T>, AppError hierarchy
│   ├── time/                       Clock / TimeSource abstraction (testable)
│   ├── device/                     DeviceCapabilities, DeviceClassifier, ThermalMonitor
│   └── di/                         AppContainer, ViewModelFactory helpers
│
├── domain/
│   ├── torch/                      TorchController (interface), TorchState, TorchError
│   ├── screenlight/                ScreenLightController (interface), ScreenLightSettings
│   ├── emergency/                  EmergencyPattern, PatternStep, PatternScheduler
│   ├── simulator/                  LightObject, LightSourceSpec, SimulatorSettings,
│   │                               QualityMode, Environment
│   └── smartlight/                 SmartLightProvider, SmartLightDevice,
│                                   DeviceCapabilities  (interfaces only, no impl)
│
├── data/
│   ├── prefs/                      DataStore-backed PreferencesRepository
│   ├── catalog/                    LightObjectCatalog (reads assets/catalog.json)
│   └── hardware/
│       ├── CameraTorchController   real CameraManager implementation
│       ├── WindowScreenLight       window-brightness / keep-screen-on implementation
│       └── AndroidDeviceCaps       flash availability, GL ES level, thermal, battery
│
├── render/
│   ├── FilamentEngineHost          Engine/Renderer/SwapChain lifecycle
│   ├── SceneBuilder                scene graph assembly, ground plane, backdrop
│   ├── LightRig                    per-object light creation + transform parenting
│   ├── ModelLoader                 gltfio asset load, caching, error mapping
│   ├── QualityProfile              effect toggles per QualityMode
│   ├── OrbitController             drag/pinch → quaternion + camera pose
│   └── FilamentViewport            Compose AndroidView wrapper + lifecycle binding
│
├── feature/
│   ├── onboarding/                 Screen + ViewModel + UiState
│   ├── home/
│   ├── flashlight/
│   ├── screenlight/
│   ├── emergency/
│   ├── tools/
│   ├── simulator/                  gallery + viewer
│   ├── smartlight/
│   └── settings/
│
├── navigation/                     Route sealed types, NavHost graph, bottom nav
└── ui/
    ├── theme/                      tokens (see UI_DESIGN_SYSTEM.md §11)
    └── components/                 CMP-* reusable composables
```

**Dependency direction (enforced by `T-ARCH-01`)**

```
feature ──► domain ──► core
   │           ▲
   │           │
   └──► ui     └── data, render (implement domain interfaces)
```

- `domain` depends on `core` only. It contains **no Android framework imports**
  except `androidx.annotation`. This is what makes it unit-testable on the JVM.
- `data` and `render` depend on `domain` and `core` and may use Android APIs.
- `feature` depends on `domain`, `ui`, `core`. It must **not** import `data` or
  `render` implementation classes directly — it receives interfaces from the container.
- `ui` depends on nothing but Compose.

---

## 3. Dependency injection (`ADR-003`)

**Manual constructor injection via an `AppContainer`.** No Hilt, no Dagger, no KSP.

Rationale: the app has roughly a dozen injectable objects and one Activity. Hilt would
add a KSP processor, an annotation-processing build step, a Kotlin/KSP version
coupling, and a `@HiltViewModel` indirection — for a graph small enough to read on one
screen. The testing requirement is *substitutability*, which interfaces plus a
container satisfy completely.

```kotlin
interface AppContainer {
    val torchController: TorchController
    val screenLight: ScreenLightController
    val preferences: PreferencesRepository
    val catalog: LightObjectCatalog
    val deviceCapabilities: DeviceCapabilities
    val thermalMonitor: ThermalMonitor
    val timeSource: TimeSource
    val rendererFactory: RendererFactory
}
```

- `DefaultAppContainer(context)` builds the real graph; held by `Application`.
- Tests construct `FakeAppContainer` with fakes for any subset.
- ViewModels are created through a `viewModelFactory { }` that reads the container
  from `APPLICATION_KEY`, so screens stay free of construction logic.
- A `LocalAppContainer` composition local exists **only** for Compose previews and
  UI tests; production composables receive their ViewModel, not the container.

If the graph grows past ~25 entries or gains scoped lifetimes beyond
process/ViewModel, revisit (recorded as a trigger in `ADR-003`).

---

## 4. Presentation pattern

**Unidirectional data flow. One state type per screen.**

```kotlin
sealed interface FlashlightUiState {
    data object Checking : FlashlightUiState
    data class Ready(
        val isOn: Boolean,
        val brightness: BrightnessControl?,   // null ⇒ not supported, not rendered
        val banner: BannerState?
    ) : FlashlightUiState
    data class Unavailable(val reason: TorchError) : FlashlightUiState
}
```

Rules:
1. A screen's ViewModel exposes exactly one `StateFlow<UiState>`, collected with
   `collectAsStateWithLifecycle()`.
2. Loading/content/error are **variants of that type**, never parallel booleans.
   `isLoading && error != null` must be unrepresentable.
3. Composables receive `(state, onEvent)` — they never hold business logic and never
   call a repository.
4. One-shot effects (navigation, snackbars) go through a `Channel<Effect>` consumed as
   a `Flow`, not through nullable state fields that need clearing.
5. `SavedStateHandle` holds only what the table in
   [SCREEN_SPECIFICATION.md](SCREEN_SPECIFICATION.md) marks as surviving process death.

---

## 5. State ownership (`FR-GLOBAL-02`, `ADR-004`)

This is the most important rule in the architecture. **Every shared resource has
exactly one owner. Controls read the owner's state; they never cache a copy.**

| Resource | Owner | Scope | How state is read |
|---|---|---|---|
| Camera torch | `CameraTorchController` | Process singleton in `AppContainer` | `StateFlow<TorchState>` fed by `CameraManager.TorchCallback` |
| Screen light window attributes | `WindowScreenLight`, bound to the Activity window | Activity | Applied on resume, cleared on pause |
| Emergency pattern scheduler | `EmergencyViewModel` | ViewModel (screen) | `StateFlow<EmergencyUiState>` |
| Filament engine + scene | `FilamentEngineHost` | Simulator screen only | Created on enter, destroyed on exit |
| Preferences | `PreferencesRepository` | Process singleton | `Flow<Preferences>` from DataStore |

### Why the torch is a process singleton, not a ViewModel

A ViewModel-scoped torch would be destroyed and recreated on rotation, and two
screens (Home and Flashlight) would each hold their own idea of the torch state.
As a process singleton fed by the OS callback:

- Home and Flashlight render the *same* flow, so they cannot disagree.
- An external torch-off (another app, the system) propagates to every observer
  (`AC-TORCH-05` / `FR-HOME-02`).
- Rotation cannot toggle the torch.

The controller **never** stores "what I asked for" as the displayed state. It stores
the last value received from `onTorchModeChanged`, plus a separate `lastError`. This
is the mechanism that implements `FR-GLOBAL-02` — the UI cannot show "on" for a failed
operation, because "on" only ever comes from the OS.

```kotlin
sealed interface TorchState {
    data object Unknown : TorchState
    data class Available(val isOn: Boolean, val maxStrength: Int) : TorchState
    data class Unavailable(val error: TorchError) : TorchState
}

sealed interface TorchError {
    data object NoFlashUnit : TorchError
    data object CameraInUse : TorchError
    data object DisabledByPolicy : TorchError
    data class Unknown(val cause: String) : TorchError
}
```

---

## 6. Error model

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    val userMessageRes: Int          // every error carries its own string resource
    data class Hardware(val torch: TorchError, override val userMessageRes: Int) : AppError
    data class AssetLoad(val assetId: String, val kind: Kind, override val userMessageRes: Int) : AppError
    data class Renderer(val stage: Stage, override val userMessageRes: Int) : AppError
    data class Storage(override val userMessageRes: Int) : AppError
}
```

Rules:
- No exceptions cross a layer boundary. `data` and `render` catch platform exceptions
  and map them to `AppError`.
- Every `AppError` carries a string resource id — there is no path to a generic
  "Something went wrong" message.
- Exception class names and stack traces are never shown to the user; they go to
  logcat via a single `AppLogger` that is a no-op for `VERBOSE`/`DEBUG` in release.
- Recoverable errors render as `CMP-BANNER` or `CMP-ERROR` with exactly one primary
  recovery action. Snackbars are not used for errors.
- A failed preference read never fails a screen; it falls back to documented defaults.

---

## 7. Data flow examples

### Torch toggle

```
User taps CMP-POWER
  → HomeViewModel.onEvent(ToggleTorch)
    → torchController.toggle(): AppResult<Unit>
       ├ Success → (no UI change yet)
       │            OS fires TorchCallback.onTorchModeChanged(id, true)
       │              → controller updates StateFlow<TorchState>
       │                → HomeViewModel maps to UiState
       │                  → card renders "On"
       └ Failure(CameraInUse)
                    → controller sets lastError, state stays Available(isOn=false)
                      → card stays "Off" + banner shows the conflict message
```

The UI change on success comes **only** from the OS callback. This is deliberate and
is what `T-TORCH-04` verifies.

### Simulator object selection

```
Gallery tap → navigate(SimViewer(objectId))
  → SimulatorViewModel init
    → catalog.get(objectId)                 : LightObjectSpec
    → preferences.objectSettings(objectId)  : Flow<ObjectSettings>
    → state = Loading
    → rendererFactory.create(surface, quality)
      → ModelLoader.load(spec.assetPath)
         ├ Success → LightRig.attach(spec, settings) → state = Ready
         └ Failure → state = Error(AssetLoad)
  → user drags → OrbitController.update(delta) → object quaternion
      → LightRig light transforms are children of the object entity ⇒ follow for free
```

---

## 8. Lifecycle handling

| Event | Torch | Screen light | Emergency | Simulator |
|---|---|---|---|---|
| `ON_START` | subscribe to torch flow | — | — | — |
| `ON_RESUME` | — | apply brightness + `KEEP_SCREEN_ON` | — | start render loop |
| `ON_PAUSE` | — | clear brightness override + flag | **stop pattern** | **stop render loop** |
| `ON_STOP` | torch off unless opted in; unsubscribe | — | — | — |
| `ON_DESTROY` | — | — | scheduler cancelled with the ViewModel scope | destroy engine + assets |
| Config change | no effect (singleton) | re-applied on resume | selection kept via `SavedStateHandle` | surface recreated, engine reused if the Activity survives; otherwise full rebuild |
| Process death | OS turns torch off | settings restored from DataStore | restores Idle | object + settings restored, model reloaded with a loading state |

Lifecycle wiring uses `LifecycleEventObserver` registered through a
`DisposableEffect(lifecycleOwner)` in each screen, or `repeatOnLifecycle` for flow
collection. No screen calls `Activity` lifecycle methods directly.

---

## 9. Concurrency

- **Dispatchers are injected**, never referenced as `Dispatchers.IO` inside a class
  under test. `AppContainer` exposes a `DispatcherProvider`.
- Hardware calls (`setTorchMode`) run on `Dispatchers.Default`; they are fast but can
  throw, so they are wrapped in `runCatching` and mapped to `AppResult`.
- DataStore writes are already on its own dispatcher; slider-driven writes are
  `debounce(400)` to avoid disk thrash.
- The emergency scheduler runs in `viewModelScope` on an injected `TimeSource`.
- Filament calls are **not thread-safe**: all `Engine`, `Scene`, `View`, `Renderer`
  and `TransformManager` calls happen on the main thread, driven by `Choreographer`.
  Model file I/O happens on `Dispatchers.IO`; only the final buffer handoff to
  `gltfio` returns to the main thread. This is a hard rule — violating it produces
  non-deterministic native crashes.

---

## 10. Persistence

**Jetpack DataStore (Preferences)**, one `DataStore<Preferences>` instance named
`settings`. No Room — there is no relational data in v1.0.

| Key | Type | Default |
|---|---|---|
| `onboarding_completed` | Boolean | `false` |
| `keep_torch_background` | Boolean | `false` |
| `photosensitivity_ack` | Boolean | `false` |
| `screenlight_{variant}_color` | Int (ARGB) | white / amber for night |
| `screenlight_{variant}_brightness` | Float | `0.7f` (night: `0.15f`) |
| `screenlight_mode` | String | `white` |
| `sim_last_object` | String | `obj_torch` |
| `sim_environment` | String | `dark` |
| `sim_quality` | String | device-class derived |
| `sim_{objectId}_brightness` | Float | per spec |
| `sim_{objectId}_beam` | String | `medium` |
| `sim_{objectId}_temp` | String | `neutral` |
| `sim_{objectId}_flame` | String | `low` |
| `reduce_motion` | Boolean | system-derived |

Migration: a `schema_version` Int key is written from day one so a future migration
has a baseline. No personal data is stored (see
[DATA_PRIVACY_AND_SECURITY.md](DATA_PRIVACY_AND_SECURITY.md)).

---

## 11. Integration boundaries

Four boundaries isolate anything that cannot run deterministically in a unit test:

| Boundary | Interface | Real impl | Test fake |
|---|---|---|---|
| Camera torch | `TorchController` | `CameraTorchController` | `FakeTorchController` (scriptable state + failures) |
| Device capability | `DeviceCapabilities` | `AndroidDeviceCaps` | `FakeDeviceCapabilities` |
| Screen window | `ScreenLightController` | `WindowScreenLight` | `FakeScreenLightController` (records calls) |
| 3D rendering | `RendererFactory` / `SimulatorRenderer` | `FilamentRenderer` | `FakeRenderer` (records scene-graph ops, computes transforms without a GPU) |
| Time | `TimeSource` | `TimeSource.Monotonic` | `TestTimeSource` |
| Smart light (Phase 6) | `SmartLightProvider` | *(none in v1.0)* | `FakeSmartLightProvider` |

`FakeRenderer` is what makes `T-3D-01` (beam direction after rotation) a fast JVM
test: the light-transform math lives in `LightRig`/`OrbitController` as pure
functions over quaternions and matrices, and is asserted without a GPU. Only the
Filament binding itself requires an instrumented test.

---

## 12. Build configuration

- `gradle/libs.versions.toml` version catalog is the single source of dependency
  versions. No version literals in `build.gradle.kts`.
- Gradle wrapper committed, with `distributionSha256Sum` set — reproducible builds.
- `org.gradle.configuration-cache=true`, `org.gradle.caching=true`,
  `android.nonTransitiveRClass=true`, `kotlin.code.style=official`.
- Build types: `debug` (applicationIdSuffix `.debug`, no minify) and `release`
  (R8 full mode, `isMinifyEnabled = true`, `isShrinkResources = true`).
- ABI splits / App Bundle: arm64-v8a and armeabi-v7a; x86/x86_64 excluded from the
  release bundle but retained in debug for emulators. See
  [PERFORMANCE_AND_APP_SIZE.md](PERFORMANCE_AND_APP_SIZE.md).
- **R8 keep rules for Filament are mandatory** — Filament's JNI layer looks up Java
  fields and methods by name. `proguard-rules.pro` must keep
  `com.google.android.filament.**` and `com.google.android.filament.gltfio.**`.
  Omitting this produces a release-only native crash that debug builds never show.
  This is verified by `T-REL-01` (a release-build smoke test).
- `BuildConfig` is enabled for `VERSION_NAME`/`VERSION_CODE` on the About screen.

---

## 13. Logging and diagnostics

A single `AppLogger` interface with `DebugLogger` and a release `NoOpLogger` that
forwards only `WARN`/`ERROR`. No third-party analytics or crash SDK in v1.0
(`FR-GLOBAL-04`); if one is added it must be opt-in per
[DATA_PRIVACY_AND_SECURITY.md](DATA_PRIVACY_AND_SECURITY.md) §6.

---

## 14. Known architectural risks

| Risk | Mitigation | Tracked |
|---|---|---|
| Filament native crashes are hard to diagnose and bypass Kotlin error handling | All engine calls on the main thread; strict create/destroy ordering; a release-build smoke test; R8 keep rules | `LIM-FILAMENT-NATIVE` |
| AGP 9.x is a recent major release; plugin/DSL changes may bite | Pin exact versions in the catalog; the build is validated in Phase 1 before any feature work | `ADR-002` |
| Device-specific torch behaviour cannot be fully covered by CI | Fakes cover logic; a documented manual device matrix covers hardware | [TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md) §9 |
| Single module could ossify as features grow | `T-ARCH-01` enforces layering now, so extraction later is mechanical | `ADR-001` |
