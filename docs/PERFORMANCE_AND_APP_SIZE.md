# PERFORMANCE_AND_APP_SIZE.md — All-in-One Lighting

**Every number here is a budget, not a measurement.** Targets are marked
`[Provisional]` until produced by the measurement procedures in §7 and §8 on the
reference devices.

---

## 1. Device classes

| Class | Definition | Reference-ish spec |
|---|---|---|
| **Low** | ≤ 4 GB RAM, or `isLowRamDevice`, API 24–29 | entry-level SoC, GL ES 3.0, 720p |
| **Mid** | 6–8 GB RAM, API 30–33 | mainstream SoC, 1080p, 60–90 Hz |
| **High** | ≥ 8 GB RAM, API 34+ | flagship SoC, 1080p+, 120 Hz |

All targets below default to **Mid** unless stated.

---

## 2. Startup

| Metric | Budget | Notes |
|---|---|---|
| Cold start → first frame (Low) | ≤ 1800 ms p50 | |
| Cold start → interactive Home (Mid) | ≤ 1200 ms p50 | `NFR-PERF-01` |
| Cold start → interactive Home (High) | ≤ 800 ms p50 | |
| Warm start | ≤ 400 ms | |
| Splash hold | **0 ms artificial** | `FR-ONB-01`; the only gate is a single DataStore read |

How we keep it:
- No work in `Application.onCreate()` beyond constructing `AppContainer` — which
  builds **lazily**. No `CameraManager` query, no DataStore read, no asset parse.
- `MainActivity` sets content immediately; the splash condition clears on the first
  DataStore emission.
- Filament's native libraries are loaded **only** when the simulator is first opened,
  never at startup. `System.loadLibrary` for ~7.4 MB of native code at launch would
  dominate cold start for a user who only wants a torch.
- Baseline Profile generated in Phase 4 (`androidx.baselineprofile`) targeting the
  startup and Home-render paths.
- No `ContentProvider`-based initialisation beyond what AndroidX requires;
  unnecessary `androidx.startup` initializers are removed in the manifest.

---

## 3. Runtime performance

| Metric | Budget | Requirement |
|---|---|---|
| Screen navigation → first frame | ≤ 150 ms | `NFR-PERF-02` |
| Simulator model load (cold) | ≤ 1500 ms | `NFR-PERF-03` |
| Simulator sustained fps, Balanced, Mid | ≥ 55 fps over 60 s | `NFR-PERF-04` |
| Simulator sustained fps, Performance, Low | ≥ 28 fps (30 cap) | |
| Jank frames during rotation | < 2 % | `NFR-PERF-05` |
| p95 frame time, Balanced, Mid | ≤ 18 ms | `T-PERF-02` |
| Touch → visible rotation | ≤ 2 frames | |
| Slider drag → light update | same frame | |

Rules that protect these:
- The render loop runs **only** while the simulator is resumed (`FR-3D-13`). Zero
  frames while backgrounded — verified by `T-3D-17`.
- One model resident at a time; no model cache (`THREE_D_LIGHTING_SPEC.md` §10).
- No unbounded animation anywhere: the candle is capped at 4 Hz, the flame at 6 Hz,
  and all animation is elapsed-time driven.
- DataStore writes from sliders are `debounce(400)`.
- Compose: `@Immutable`/`@Stable` on state types, `derivedStateOf` for computed UI
  values, lambdas hoisted so cards do not recompose on unrelated state. Verified with
  recomposition counts during Phase 4, not assumed.

---

## 4. Memory

| Metric | Budget |
|---|---|
| Peak PSS, Home only | ≤ 140 MB |
| Peak PSS, simulator Performance | ≤ 240 MB |
| Peak PSS, simulator Balanced | ≤ 320 MB (`NFR-MEM-01`) |
| Peak PSS, simulator High | ≤ 420 MB |
| Native growth over 20 enter/exit cycles | **none beyond allocator noise** (`NFR-MEM-02`) |

The native-growth budget is the strict one. Filament allocates outside the Java heap,
so a leaked `Engine`, `Scene` or asset will not show up as a Java OOM — it will show
up as a device slowly dying. `T-3D-04` runs 20 cycles and asserts no monotonic growth,
and the single teardown sequence in `FilamentEngineHost.release()`
([THREE_D_LIGHTING_SPEC.md](THREE_D_LIGHTING_SPEC.md) §11) is the only place engine
objects are destroyed.

---

## 5. Battery and thermal

| Scenario | Expectation |
|---|---|
| Screen light, static colour | No measurable CPU beyond compositing — the surface is a static fill and does not recompose (`NFR-BAT-01`) |
| Candle flicker | ≤ 4 colour updates/sec |
| Emergency strobe | Coroutine `delay`, never a spin loop |
| Simulator | The dominant consumer; capped per quality mode, degraded on thermal/battery-saver |
| Backgrounded | **Zero** scheduled work (`NFR-BAT-02`, `T-GLOBAL-04`) |

Thermal and battery-saver degradation is specified in
[THREE_D_LIGHTING_SPEC.md](THREE_D_LIGHTING_SPEC.md) §7 and is always announced to the
user, never silent.

---

## 6. App size

### 6.1 Measured inputs (verified 2026-10-15)

Native payload, **uncompressed**, per ABI, read directly from the published AARs:

| Library | arm64-v8a | armeabi-v7a |
|---|---|---|
| `filament-android` 1.77.3 | 2.96 MB | 2.23 MB |
| `gltfio-android` 1.77.3 | 3.09 MB | 2.27 MB |
| `filament-utils-android` 1.77.3 | 1.38 MB | 1.16 MB |
| **Total native** | **7.43 MB** | **5.66 MB** |

These are facts. Everything below that combines them is a projection.

### 6.2 Download-size budget (per-ABI split, arm64-v8a) `[Provisional]`

| Component | Budget | Basis |
|---|---|---|
| Filament native (compressed) | ~4.5 MB | 7.43 MB measured, assuming typical ~60 % native compression — **must be measured** |
| Kotlin + AndroidX + Compose (post-R8) | ~4.5 MB | typical Compose app after shrinking |
| App code + resources | ~1.5 MB | |
| 3D models (5 × `.glb`, Draco) | ≤ 0.9 MB | `THREE_D_LIGHTING_SPEC.md` §10 |
| IBL/skybox `.ktx` (2 environments) | ≤ 0.8 MB | |
| Thumbnails + onboarding art (WebP) | ≤ 0.6 MB | |
| Inter font subset | ≤ 0.3 MB | |
| **Total download** | **≤ 28 MB** (`NFR-SIZE-01`) | headroom above the ~13 MB projection |
| **Installed** | **≤ 75 MB** (`NFR-SIZE-02`) | |

The budget deliberately exceeds the projection. A budget that assumes everything goes
right is not a budget.

> **gltfio is the single largest line item at 3.09 MB.** If measurement shows the
> download exceeding budget, the first lever is checking whether `gltfio` can be
> dropped in favour of loading pre-converted `.filamesh` assets with `filament-utils`
> alone. That trades build-pipeline complexity for roughly 3 MB. Tracked as `OQ-07`;
> do not attempt it before measuring.

### 6.3 Size levers, in the order we would use them

1. App Bundle with ABI splits — ship arm64-v8a and armeabi-v7a; exclude x86/x86_64
   from release (kept in debug for emulators).
2. R8 full mode + resource shrinking (`isMinifyEnabled`, `isShrinkResources`).
3. Draco mesh compression on all `.glb`.
4. ASTC/ETC2 textures via KTX2; verify `gltfio` support at implementation time.
5. WebP for every raster image; no PNG in the final build.
6. Font subsetting to Latin + the scripts we localise into.
7. `android.enableResourceOptimizations`, unused-resource removal.
8. **If still over budget:** move the two optional 3D models to Play Asset Delivery
   (Phase 5) rather than cutting quality from the core three.

### 6.4 What we will not do

- Ship every premium model in the base APK (brief: the initial build includes only
  what the core release needs).
- Download assets at first launch — the app must work offline immediately
  (`FR-GLOBAL-04`).
- Strip ABIs down to armeabi-v7a only to save space; arm64 is the overwhelming
  majority of active devices and would suffer.

---

## 7. How download size is actually measured

Projections are not evidence. The procedure, run at every release candidate and
recorded in the release notes:

```bash
# 1. Build the release bundle
./gradlew :app:bundleRelease

# 2. Generate the device-specific APK set (signed)
bundletool build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=build/size/app.apks \
  --ks=$KEYSTORE --ks-key-alias=$ALIAS --mode=default

# 3. Report download and install sizes for all device configurations
bundletool get-size total \
  --apks=build/size/app.apks \
  --dimensions=ABI,SDK \
  --human-readable-sizes
```

Also record, for the arm64-v8a/API 34 configuration specifically:

```bash
bundletool get-size total --apks=build/size/app.apks \
  --device-spec=device-spec-arm64-api34.json --human-readable-sizes
```

And for installed size, from a real device after install:

```bash
adb shell dumpsys diskstats | grep -i allinonelighting
# or
adb shell pm path com.allinonelighting   # then stat the APK and lib dirs
```

The measured numbers replace the `[Provisional]` markers in this document. If a
measurement exceeds budget, apply §6.3 levers in order and re-measure — do not revise
the budget to match the result without an explicit decision in `DECISIONS.md`.

`T-REL-02` automates step 3 and fails the build if the arm64 download size exceeds
`NFR-SIZE-01`.

---

## 8. How performance is actually measured

### Startup (`T-PERF-01`)

```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest \
  -P android.testInstrumentationRunnerArguments.class=com.allinonelighting.benchmark.StartupBenchmark
```

`StartupBenchmark` uses `MacrobenchmarkRule` with `StartupTimingMetric`,
`iterations = 10`, `CompilationMode.Partial()` with the baseline profile, and
`StartupMode.COLD`. Reports `timeToInitialDisplayMs` and
`timeToFullDisplayMs` — the latter requires calling
`reportFullyDrawn()` once Home's torch state has resolved.

Additionally, a trace is inspected once per release to confirm **no artificial delay**
exists on the startup path (`AC-ONB-05`):

```bash
adb shell am start -W -n com.allinonelighting/.MainActivity   # TotalTime / WaitTime
```

### Frame timing (`T-PERF-02`)

`MacrobenchmarkRule` with `FrameTimingMetric` over a scripted interaction: open the
simulator, load `obj_torch`, perform 10 scripted drag gestures, hold 10 s. Reports
`frameDurationCpuMs` p50/p90/p95/p99 and `frameOverrunMs`. `JankStats` is also wired
into debug builds for ad-hoc observation.

### Memory (`NFR-MEM-*`)

```bash
adb shell dumpsys meminfo com.allinonelighting   # before, during, after simulator
```

For the native-growth assertion, `T-3D-04` samples `Debug.getNativeHeapAllocatedSize()`
after each of 20 enter/exit cycles and fails on a monotonic upward trend outside a
tolerance band.

### Battery

Phase 4, once per release: `adb shell dumpsys batterystats --reset`, run a scripted
15-minute session (5 min screen light, 5 min simulator, 5 min idle), then
`adb bugreport` and inspect in Battery Historian. The check is qualitative — no
wakelocks, no background jobs, no CPU while idle — rather than a milliamp target,
which is not reproducible across devices.

---

## 9. Performance rules for implementers

1. Never load Filament's native libraries before the user opens the simulator.
2. Never render a frame while the simulator is not resumed.
3. Never hold more than one 3D model in memory.
4. Never drive animation by frame count; always by elapsed time.
5. Never write to DataStore on every slider pixel; debounce.
6. Never do I/O or capability queries in `Application.onCreate()`.
7. Never add a dependency without recording its size impact.
8. Never mark a budget met without the measurement output to prove it.
9. Profile before optimising; record the before/after numbers in the task report.
10. A performance regression is a bug, and gets a test or a benchmark, like any bug.

---

## 10. Provisional-status summary

| Target | Status | Becomes verified when |
|---|---|---|
| Cold start budgets | `[Provisional]` | `T-PERF-01` runs on all three device classes |
| Frame-rate budgets | `[Provisional]` | `T-PERF-02` runs on Low and Mid |
| Model-load budget | `[Provisional]` | `T-PERF-03` runs with final assets |
| Memory budgets | `[Provisional]` | `dumpsys meminfo` sampled on Low and Mid |
| Download size | `[Provisional]` | §7 procedure run on a real release bundle |
| Installed size | `[Provisional]` | measured on a real device after install |
| Filament native payload | **Verified 2026-10-15** | — (read from the published AARs) |
