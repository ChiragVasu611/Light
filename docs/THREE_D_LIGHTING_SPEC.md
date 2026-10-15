# THREE_D_LIGHTING_SPEC.md — All-in-One Lighting

The 3D simulator is a core feature, not a decoration. This document defines the
renderer, the coordinate conventions, every object, how light stays attached to its
object, how the environment responds, and what each quality mode costs.

**Honesty constraint.** This is a *real-time approximation* using image-based lighting
and punctual dynamic lights. It is **not** physically accurate global illumination and
the product must never claim it is (`PROJECT_VISION.md` §6.4).

---

## 1. Renderer selection (`ADR-013`)

**Google Filament 1.77.3** — `filament-android`, `gltfio-android`,
`filament-utils-android` (all three at the same version; mismatched versions crash in
native code).

Verified on 2026-10-15:
- Latest stable on Maven Central: `1.77.3`, published 2026-10-08.
- AAR declares `minSdkVersion 21` → compatible with our `minSdk 24`.
- Native payload per ABI (uncompressed, arm64-v8a):
  `libfilament-jni.so` 2.96 MB + `libgltfio-jni.so` 3.09 MB +
  `libfilament-utils-jni.so` 1.38 MB ≈ **7.43 MB**.

Why Filament over the alternatives:

| Option | Verdict |
|---|---|
| **Filament** | Chosen. PBR, real dynamic punctual lights, IBL, shadows, `gltfio` glTF/GLB loader, first-party Android AARs, no runtime/engine licence. |
| SceneView (`io.github.sceneview`) | A Compose-friendly wrapper *over* Filament. Rejected as a dependency because it adds an abstraction layer we would fight for custom light rigging and lifecycle control, and couples our Filament version to theirs. We take Filament directly. |
| Sceneform | Archived by Google. Rejected. |
| Raw OpenGL ES / Vulkan | Would require writing a PBR pipeline, IBL, and a glTF loader. Rejected on cost. |
| Unity / Godot export | A whole game engine for one screen; tens of MB, slow start, licence surface. Rejected. |

**Requirement:** OpenGL ES 3.0. Devices below that get the `Unsupported` state in
`SCR-SIM-GALLERY` while the rest of the app works normally (`NFR-COMPAT-02`). Filament
can also target Vulkan; we use the **OpenGL ES backend** in v1.0 for the widest driver
compatibility and revisit Vulkan only if profiling demands it (`OQ-06`).

---

## 2. Coordinate and orientation convention (`ADR-014`)

**One convention, applied to every model and every light. No exceptions.**

| Axis | Direction | Meaning |
|---|---|---|
| **+Y** | up | scene up; the yaw axis |
| **−Z** | into the screen | **the forward / emission axis** |
| **+X** | right | |

Right-handed, metres, Y-up — which is also the glTF 2.0 convention, so exported
assets need no axis fix-up at load time.

### Model authoring rules (binding on every asset)

1. **Pivot at the origin.** Each model's root node sits at `(0,0,0)` and the mesh is
   positioned so the object's natural rotation centre is at the origin. A torch
   rotates about its mid-body, not its tail.
2. **Emission along −Z.** Any directional emitter (torch lens, LED lamp head) points
   down **−Z** in its rest pose. A model that points +X at rest is rejected at review;
   we fix the asset, never compensate in code.
3. **Up is +Y.** A candle stands along +Y; a tube light lies along **X** (documented
   per-object below, since a tube is linear, not directional).
4. **Unit scale = 1.0.** No scale baked into the root node. Real-world dimensions are
   expressed in metres in the mesh itself.
5. **Named light anchor node.** Each model contains an empty node named exactly
   `LIGHT_ANCHOR` positioned at the light's emission point, and oriented so its −Z is
   the emission direction. `LightRig` looks this node up by name.

Rule 5 is what makes light attachment data-driven rather than a table of hard-coded
offsets. If `LIGHT_ANCHOR` is missing, the model fails validation at load with
`AssetLoad.MissingAnchor` and the viewer shows a recoverable error (`FR-3D-14`).

---

## 3. Light attachment and synchronisation (`FR-3D-04`)

This is the single most important mechanic in the feature, and the thing most
competitors fake.

### Scene graph

```
root (identity)
└── objectRoot                 ← user rotation is applied HERE (quaternion)
    └── modelRoot              ← the loaded glTF asset root
        ├── <meshes…>
        └── LIGHT_ANCHOR       ← empty node from the asset
            └── lightEntity    ← Filament light, LOCAL transform = identity
```

**The light entity is a child of `LIGHT_ANCHOR`, which is a descendant of
`objectRoot`.** Filament's `TransformManager` composes world transforms down the
hierarchy, so when `objectRoot`'s quaternion changes, the light's world position *and*
world direction change with it — automatically, with no per-frame code that could
drift or be forgotten.

We never write the light's world transform directly. There is exactly one place that
sets a transform in response to user input:

```kotlin
// OrbitController produces the quaternion; LightRig never touches world space.
transformManager.setTransform(
    transformManager.getInstance(objectRoot),
    objectRotation.toMat4()          // pure function, unit-testable
)
```

### Why this satisfies the stated examples

| Requirement | How the graph satisfies it |
|---|---|
| "Rotating the torch rotates its beam in the same coordinate system" | The spot light is a child of the torch's `LIGHT_ANCHOR`; its world direction is `objectRotation × anchorRotation × (0,0,−1)` |
| "The beam must not remain fixed in world space while the torch rotates" | There is no code path that writes a world-space light direction. Structurally impossible. |
| "Rotating a bulb does not turn its broad glow into a fixed narrow beam" | `OBJ-BULB` creates a `LightManager.Type.POINT`. A point light has no direction, so rotation cannot narrow it. `AC-3D-02` asserts no spot light is ever created for the bulb. |
| "Flame orientation and position attached to the candle model" | The flame mesh and its light are both descendants of `modelRoot`. |

### Testability (`T-3D-01`)

The transform math lives in pure Kotlin — `Quaternion`, `Mat4`, `OrbitController.update()`,
`LightRig.worldDirectionOf(anchor, rotation)` — with **no Filament types in the
signature**. A JVM unit test rotates 90° about Y and asserts the beam direction moved
from `(0,0,−1)` to `(−1,0,0)` within 1°. No GPU, no emulator, millisecond runtime.
The Filament binding is then covered separately by one instrumented smoke test.

---

## 4. Object catalogue

Five objects ship in v1.0. Each is defined by a `LightObjectSpec` loaded from
`assets/catalog.json`, so adding an object later is data plus an asset, not code.

```json
{
  "id": "obj_torch",
  "nameRes": "obj_torch_name",
  "category": "portable",
  "asset": "models/torch.glb",
  "thumb": "thumbs/torch.webp",
  "light": {
    "type": "spot",
    "defaultIntensityLumen": 900,
    "colorKelvin": 5200,
    "innerConeDeg": 12, "outerConeDeg": 22,
    "falloffMetres": 12.0
  },
  "controls": ["brightness", "beamAngle"],
  "defaultOrientationDeg": { "yaw": -25, "pitch": 10 },
  "beamVisualisation": true
}
```

---

### OBJ-TORCH — Torch / flashlight

| Property | Value |
|---|---|
| Category | Portable |
| Pivot | Mid-body, origin |
| Forward | −Z through the lens |
| Dimensions | 0.16 m long × 0.035 m barrel Ø, head 0.05 m Ø |
| Materials | Anodised aluminium body (metallic 1.0, roughness 0.35), knurled grip (roughness 0.6), glass lens (transmission, roughness 0.05), polished reflector (metallic 1.0, roughness 0.08) |
| Emitting surface | Lens disc, 0.042 m Ø, emissive 8 000 nits scaled by brightness |
| Light type | **Spot** (`LightManager.Type.FOCUSED_SPOT`) |
| Intensity | 900 lm default; slider 100–2000 lm |
| Colour | 5200 K (cool white LED) |
| Cone | Narrow 6°/12°, Medium 12°/22°, Wide 24°/40° (inner/outer) |
| Falloff | 12 m |
| Default orientation | yaw −25°, pitch +10° (three-quarter view, beam visible across the scene) |
| Rotation | Yaw unlimited; pitch clamped ±85° |
| Controls | Brightness, Beam angle |
| Beam visualisation | **Yes** — see §6 |
| Target triangles | ≤ 12 k |
| Textures | 1× 1024² BC/ASTC albedo+ORM packed |
| Render cost | Spot light + 1 shadow-casting light. Highest-cost object (shadows + beam volume) |

---

### OBJ-BULB — Incandescent-style bulb

| Property | Value |
|---|---|
| Category | Household |
| Pivot | Centre of the glass envelope |
| Forward | n/a — omnidirectional |
| Dimensions | A60 form: 0.11 m tall, 0.06 m Ø glass, E27 base |
| Materials | Glass envelope (transmission 0.9, roughness 0.05, thin-walled), tungsten filament (emissive), brass base (metallic 1.0, roughness 0.3), ceramic insulator |
| Emitting surface | Filament geometry, emissive; glass carries a soft emissive bloom term only |
| Light type | **Point** (`LightManager.Type.POINT`) — never spot (`FR-3D-06`, `AC-3D-02`) |
| Intensity | 800 lm default; slider 100–1600 lm |
| Colour | Warm 2700 K · Neutral 4000 K · Cool 5500 K |
| Falloff | 8 m |
| Default orientation | yaw 0°, pitch 0° (upright, base down) |
| Rotation | Full yaw; pitch ±85°. Rotating it changes the view, not the emission pattern — correct and expected for a point source. |
| Controls | Brightness, Colour temperature |
| Beam visualisation | No. A soft radial glow + bloom only. |
| Target triangles | ≤ 8 k |
| Render cost | Low — one point light, no shadow in Performance mode |

---

### OBJ-TUBE — Tube light (linear fluorescent / LED batten)

| Property | Value |
|---|---|
| Category | Household |
| Pivot | Centre of the tube |
| Long axis | **X** (horizontal). Emission is radial about X, not directional. |
| Dimensions | T8 form: 0.60 m long × 0.026 m Ø |
| Materials | Frosted acrylic diffuser (translucent, roughness 0.5, emissive), aluminium end caps, white housing |
| Emitting surface | The whole cylindrical diffuser, emissive |
| Light type | **Multiple point lights along the axis** (`FR-3D-07`) |
| Light count | High quality: 7 · Balanced: 5 · Performance: 3, evenly spaced along X from −0.27 m to +0.27 m |
| Intensity | Total 1600 lm default, divided evenly among the lights; slider 200–3000 lm total |
| Colour | Warm 3000 K · Neutral 4000 K · Cool 6500 K |
| Falloff | 6 m per light |
| Default orientation | yaw −15°, pitch 0° |
| Rotation | Full yaw; pitch ±85°. Illumination elongates with the tube because the emitters are children of it. |
| Controls | Brightness, Colour temperature |
| Beam visualisation | No |
| Target triangles | ≤ 4 k |
| Render cost | Medium — 3–7 point lights; the dominant cost driver among the five |

> **On the "performance fallback" caveat in the brief.** Dropping to 3 point lights in
> Performance mode is a *visible* approximation: the illumination on the ground plane
> becomes slightly scalloped rather than smoothly linear. That is the deliberate
> fallback, and it is documented in `LIM-TUBE-SEGMENTS` rather than hidden.

---

### OBJ-CANDLE — Wax candle

| Property | Value |
|---|---|
| Category | Decor |
| Pivot | Base centre of the candle body |
| Up | +Y |
| Dimensions | 0.08 m tall × 0.045 m Ø; flame 0.025 m tall, centred 0.012 m above the wick |
| Materials | Wax (subsurface approximation: high roughness 0.7, light transmission term), blackened wick, flame as a 2-plane billboard cross with an animated emissive gradient |
| Emitting surface | Flame billboard, emissive 12 000 nits at the core falling to 0 at the edge |
| Light type | **Point**, positioned at the flame centre |
| Intensity | 12 lm default; slider 4–40 lm (a candle is dim — the scene exposure compensates, which is itself instructive) |
| Colour | 1850 K, fixed (a candle has no colour-temperature control) |
| Falloff | 2.5 m |
| Default orientation | yaw 0°, pitch 0° |
| Rotation | Full yaw; pitch clamped **±35°** — a candle tipped past that reads as wrong, and the flame billboard would reveal itself. Documented deviation from the general ±85°. |
| Controls | Brightness, **Flame animation** (Off / Low / High). No beam angle, no colour temperature (`AC-3D-07`). |
| Flame animation | Intensity = `base × (1 + a·noise(t))`, `a = 0` / `0.08` / `0.15`; noise is smoothed value noise at 6 Hz, result clamped to `[0.85, 1.15]`. The flame billboard scales by the same factor and sways ≤ 3°. **Hard bound — no unbounded random flicker** (brief: "avoid unrealistic flickering that makes the interface uncomfortable") |
| Reduce motion | Animation forced Off, steady flame |
| Target triangles | ≤ 3 k |
| Render cost | Lowest |

---

### OBJ-LED-LAMP — Modern LED desk lamp

| Property | Value |
|---|---|
| Category | Household |
| Pivot | Base centre |
| Forward | −Z out of the head's emitting face |
| Dimensions | Base 0.14 m Ø, arm 0.30 m, head 0.18 m × 0.04 m |
| Materials | Matte painted metal (roughness 0.5), diffuser panel (translucent emissive), rubber base |
| Emitting surface | The rectangular diffuser panel, 0.16 m × 0.03 m |
| Light type | **Spot**, wide cone — matching the head's actual geometry (`FR-3D-17`) |
| Intensity | 600 lm default; slider 100–1200 lm |
| Colour | Warm 3000 K · Neutral 4000 K · Cool 5700 K, **plus** an RGB colour swatch row (this is the one colour-capable source) |
| Cone | 35°/55° inner/outer, fixed — the lamp's geometry defines it; no beam-angle control |
| Falloff | 5 m |
| Default orientation | yaw −20°, pitch 0° |
| Rotation | Full yaw; pitch ±85° |
| Controls | Brightness, Colour temperature, Colour |
| Beam visualisation | Subtle — a soft cone at ≤ 30 % the opacity used for the torch |
| Target triangles | ≤ 10 k |
| Render cost | Medium |

---

### Deferred objects

| Object | Status | Why |
|---|---|---|
| Screen light | `[Deferred]` | A flat emissive quad adds little to the *simulator* that the real Screen Light screen does not already do better. Removing it saves an asset and a grid tile. (`ADR-012`) |
| Emergency beacon | `[Deferred]` to Phase 5 | A rotating beacon needs an animated reflector and a timed emissive sweep — meaningful extra asset and animation work. Spec retained below for Phase 5. |

**Emergency beacon (Phase 5 spec, not implemented in v1.0):** dome 0.12 m Ø, rotating
inner reflector at 2 Hz, spot light parented to the reflector so the beam sweeps,
`Red500`-equivalent 630 nm emissive dome, pulse mode as an alternative to rotation.
The light must be a child of the rotating reflector node, not animated in world space —
the same rule as §3.

---

## 5. Environments (`FR-3D-09`)

Both environments are real lighting setups, not background images.

### ENV-DARK — Dark room

| Element | Value |
|---|---|
| Skybox | Solid `#05070F` (matches `Ink900`) |
| IBL | A very low-intensity neutral cubemap, 300 lux equivalent — enough that unlit geometry is not pure black, low enough that the object's own light dominates |
| Ground | 4 m × 4 m plane, dark matte (albedo 0.08, roughness 0.85) |
| Backdrop | Curved cyclorama wall 2.2 m behind, albedo 0.12, roughness 0.9 |
| Purpose | Show the light's own contribution: beam shape, falloff, pooling, shadows |
| Exposure | aperture f/8, shutter 1/60, ISO 400 |

### ENV-STUDIO — Neutral studio

| Element | Value |
|---|---|
| Skybox | Neutral grey gradient `#2A3040` → `#141A26` |
| IBL | Neutral studio cubemap, ~8 000 lux equivalent, 3-point feel |
| Ground | Light grey plane (albedo 0.55, roughness 0.6) |
| Backdrop | Seamless light-grey cyclorama (albedo 0.6) |
| Purpose | Show the object's materials and form, and judge colour temperature against a neutral reference |
| Exposure | aperture f/16, shutter 1/125, ISO 100 |

Switching environments changes: skybox, IBL, ground/backdrop materials, and camera
exposure. It changes **nothing else** — object selection, rotation, brightness, beam
angle, colour temperature and flame animation are all preserved (`AC-3D-03`). That
acceptance criterion exists because environment switching is the classic place where
a naive implementation rebuilds the scene and silently resets user settings.

### IBL assets

Filament `.ktx` cubemaps generated offline with `cmgen` from source HDRIs.
- Environment map: 256² per face, RGBM, mip-chained.
- Irradiance: spherical-harmonics coefficients only (not a texture).
- Budget: ≤ 400 KB per environment. Both environments ≤ 800 KB total.
- Source HDRIs must be CC0 (Poly Haven or equivalent); provenance recorded in
  `docs/licences/assets.md` (`FR-ASSET-LICENCE`).

---

## 6. Beam visualisation

A spot light alone is invisible in clean air — you see what it *hits*, not the beam.
Users expect to see the cone. We render it explicitly for `OBJ-TORCH` (and subtly for
`OBJ-LED-LAMP`).

**Technique:** a cone mesh parented to `LIGHT_ANCHOR`, drawn with an additive,
depth-tested-but-not-depth-writing unlit material whose alpha falls off with
(a) radial distance from the axis and (b) distance from the apex, modulated by the
light's intensity and colour.

- It is a **child of the same anchor as the light**, so §3 guarantees it stays aligned.
- Disabled entirely in Performance mode.
- Opacity scales with brightness so turning the light down dims the visible cone.
- **This is a stylisation, not volumetric scattering.** We do not ray-march a
  participating medium. Documented in `LIM-BEAM-STYLISED`.

---

## 7. Quality modes (`FR-3D-11`)

| Feature | Performance | Balanced | High quality |
|---|---|---|---|
| Render scale | 0.65× | 0.85× | 1.0× |
| MSAA | off | off | 4× |
| Anti-aliasing | none | FXAA | FXAA + TAA |
| Shadows | **off** | 1 shadow-casting light, 1024² PCF | 1 light, 2048² PCF, soft |
| Screen-space AO | off | off | on (low quality setting) |
| Bloom | off | on (cheap) | on |
| Beam cone | off | on | on |
| IBL resolution | 128² | 256² | 256² |
| Tube-light segments | 3 | 5 | 7 |
| Dynamic resolution | on | on | off |
| Frame-rate cap | 30 fps | 60 fps | 60 fps |
| Target devices | ≤ 4 GB RAM, or thermally throttled, or battery saver | mid-tier | ≥ 8 GB RAM + recent GPU |

### Automatic selection

Initial mode is chosen by `DeviceClassifier` from: total RAM
(`ActivityManager.MemoryInfo.totalMem`), `isLowRamDevice`, and GL ES version. The user
can override it in the viewer or in Settings → Display; the override persists.

### Automatic degradation

| Trigger | Action | User feedback |
|---|---|---|
| `PowerManager.getCurrentThermalStatus() >= THERMAL_STATUS_SEVERE` (API 29+) | Drop to Performance, cap 30 fps | `CMP-BANNER` warning naming thermal throttling (`T-3D-10`) |
| `PowerManager.isPowerSaveMode` | Drop to Performance | `CMP-BANNER` info (`T-3D-09`) |
| `onTrimMemory(RUNNING_CRITICAL)` | Release skybox + IBL, keep the model and lights | `CMP-BANNER` info (`T-3D-11`) |

Degradation is **never silent** — a user who set High quality and sees it drop must be
told why. Restoring the user's chosen mode happens automatically when the trigger
clears.

---

## 8. Interaction (`FR-3D-03`, `FR-3D-12`)

### Gesture mapping (`ADR-015`)

| Gesture | Effect |
|---|---|
| Horizontal drag | **Yaw** about the scene up axis (+Y). Rightward drag → object rotates right. Unlimited, wraps through 360°. |
| Vertical drag | **Pitch** about the camera's right axis. Clamped to **±85°** (±35° for the candle). |
| Pinch | Dolly camera in `[0.6×, 2.5×]` of default distance |
| Double tap | Reset orientation + camera |

Sensitivity: 0.35° per dp of drag, so a full 360° yaw takes roughly a screen-and-a-half
of travel — fast enough to explore, slow enough to be precise.

**Why pitch is clamped rather than free.** Free pitch crosses the pole, where yaw and
roll become degenerate and the object appears to flip unexpectedly. Clamping pitch at
±85° while leaving yaw unlimited gives the user the entire azimuth of the object with
no flip. This is the documented answer to the brief's question about reaching a full
360° view without unexpected flips.

**Rotation is stored as a quaternion**, composed as
`q = yawQuat(yaw) * pitchQuat(pitch)` and rebuilt from the clamped Euler pair each
update. Storing Euler angles and converting once avoids accumulated drift from
repeated quaternion multiplication, and makes the clamp trivially expressible.

### Touch arbitration

- The viewport's pointer-input modifier calls `awaitPointerEvent` and consumes
  movement once a drag passes `viewConfiguration.touchSlop`, so the parent scroll
  cannot steal the gesture.
- `Modifier.systemGestureExclusion()` on the viewport's left/right 24 dp edges so an
  edge drag rotates rather than triggering back-gesture navigation.
- A tap that never exceeds touch slop does nothing — it must not nudge the rotation.

### Frame timing (`FR-3D-18`)

The render loop is driven by `Choreographer.postFrameCallback`, and **all** animation
(flame flicker, beacon sweep, any transition) advances by the real elapsed nanoseconds
between frames, not by a fixed per-frame increment. A frame-rate-coupled animation
would run at half speed on a 30 fps device.

```kotlin
val dtSeconds = (frameTimeNanos - lastFrameNanos) / 1_000_000_000.0f
// clamp to avoid a huge jump after a pause
val dt = dtSeconds.coerceIn(0f, 0.1f)
```

### Rotation feedback

The decorative 360° arc beneath the object (from the concept board) brightens
(`Cyan400` at 40 % → 90 %) while a drag is in progress and fades back over
`motionStd`. It is the only rotation affordance; there is no numeric readout, which
would imply a precision the simulation does not claim.

---

## 9. Shared controls (`FR-3D-16`)

| Control | Applies to | Range | Default | Persisted |
|---|---|---|---|---|
| Brightness | all | per-object lumen range (§4) | per-object | ✓ per object |
| Beam angle | Torch only | Narrow / Medium / Wide | Medium | ✓ per object |
| Colour temperature | Bulb, Tube, LED lamp | Warm / Neutral / Cool | Neutral | ✓ per object |
| Colour | LED lamp only | 8 presets | white | ✓ per object |
| Flame animation | Candle only | Off / Low / High | Low | ✓ per object |
| Environment | all | Dark / Studio | Dark | ✓ global |
| Quality mode | all | Performance / Balanced / High | device-derived | ✓ global |
| Reset rotation | all | action | — | ✗ |

**Controls that do not apply are not rendered** — not disabled, not greyed. Rendering a
disabled "Beam angle" on a candle implies the candle might one day have a beam.
`AC-3D-07` asserts absence from the composition, not a disabled state.

Validation: every slider value is `coerceIn`-ed to the object's documented range on
read from DataStore, so a corrupted preference cannot produce a 0-lumen or
10 000-lumen light.

---

## 10. Asset management

### Catalog

`assets/catalog.json` — the array of `LightObjectSpec` shown in §4. Parsed with
`kotlinx.serialization`. A parse failure is an `AssetLoad.CatalogCorrupt` error
surfacing as the gallery's error state (`SCR-SIM-GALLERY`), not a crash.

### Layout

```
app/src/main/assets/
├── catalog.json
├── models/     torch.glb  bulb.glb  tube.glb  candle.glb  led_lamp.glb
├── thumbs/     torch.webp bulb.webp tube.webp candle.webp led_lamp.webp
├── env/        dark_ibl.ktx  dark_skybox.ktx  studio_ibl.ktx  studio_skybox.ktx
└── materials/  beam.filamat  flame.filamat
```

### Loading

1. Read the `.glb` bytes on `Dispatchers.IO`.
2. Hand the buffer to `gltfio` `AssetLoader` / `ResourceLoader` **on the main thread**
   (Filament is not thread-safe, §9 of the architecture doc).
3. Validate: root node present, `LIGHT_ANCHOR` present, triangle count within budget.
4. Attach lights per §3, apply persisted settings.

**Caching.** One model is resident at a time. Leaving the viewer destroys the asset.
An LRU of parsed models was considered and rejected for v1.0: models are small and load
in well under the 1500 ms budget, and holding native buffers for unseen models is
exactly the kind of memory growth `NFR-MEM-02` forbids.

### Failure handling (`FR-3D-14`)

| Failure | `AppError` | UI |
|---|---|---|
| File missing from assets | `AssetLoad.Missing` | "This light source couldn't be loaded." + Retry |
| Bytes unparseable | `AssetLoad.Corrupt` | same message + Retry |
| `LIGHT_ANCHOR` missing | `AssetLoad.MissingAnchor` | same message + Retry (it is a build defect; the user cannot fix it, but Retry costs nothing and the message stays honest) |
| Filament engine creation fails | `Renderer.EngineInit` | "3D simulation isn't available on this device." + Back to gallery |
| GL ES < 3.0 | `Renderer.Unsupported` | Gallery-level unsupported state (`NFR-COMPAT-02`) |

Every one of these renders a `CMP-ERROR` with a cause-specific message and exactly one
primary action. None of them is a blank screen, a crash, or a silent fallback to a
grey box.

### Versioning

`catalog.json` carries `"schemaVersion": 1` and each object carries `"assetVersion"`.
Nothing downloads in v1.0, so versioning is forward-preparation for Phase 5 asset
packs; it costs two fields now and avoids a migration later.

### Licensing (`FR-ASSET-LICENCE`)

Every model, texture and HDRI must have its source and licence recorded in
`docs/licences/assets.md` before it is committed, with columns: file, source URL,
author, licence, modifications made. CC0 or a licence permitting commercial
redistribution without attribution-in-UI is required; anything requiring attribution
gets a row in `SCR-LICENCES`. **An asset with no recorded provenance is not merged.**

### Download size

Model budget ≤ 900 KB total for all five `.glb` files (Draco-compressed geometry,
KTX2/Basis textures where `gltfio` supports them — verify at implementation time,
`OQ-07`). Full budget accounting in
[PERFORMANCE_AND_APP_SIZE.md](PERFORMANCE_AND_APP_SIZE.md).

---

## 11. Performance targets

| Metric | Performance | Balanced | High |
|---|---|---|---|
| Sustained fps (mid-tier) | 30 (capped) | ≥ 55 | ≥ 45 |
| Model load (cold) | ≤ 1500 ms | ≤ 1500 ms | ≤ 1500 ms |
| Peak PSS with simulator active | ≤ 240 MB | ≤ 320 MB | ≤ 420 MB |
| Native memory after 20 enter/exit cycles | no monotonic growth | same | same |

All **[Provisional]** until measured on the reference devices
([TESTING_AND_ACCEPTANCE.md](TESTING_AND_ACCEPTANCE.md) §9).

### Resource release (`FR-3D-13`, `AC-3D-04`)

Destruction order is **not** optional — Filament crashes natively if an `Engine` is
destroyed while entities still reference it:

```
1. Stop the Choreographer callback
2. Destroy the loaded asset (AssetLoader.destroyAsset)
3. Destroy ResourceLoader / AssetLoader / MaterialProvider
4. Destroy lights, renderables, and their entities (EntityManager.destroy)
5. Destroy Scene, View, Camera, Renderer, SwapChain
6. Engine.destroy()
```

This sequence is implemented once in `FilamentEngineHost.release()` and is the only
place engine teardown happens.

---

## 12. What this simulator does not claim

Stated plainly here so the product never drifts into overclaiming:

- **Not** physically accurate global illumination. Filament gives direct punctual
  lighting plus image-based ambient; there is no path tracing and no multi-bounce GI.
- **Not** a photometric tool. Lumen values are plausible, not calibrated, and must
  never be presented as a measurement.
- **Not** spectrally accurate. Colour temperature is a correlated-colour-temperature
  approximation to sRGB.
- **Beam cones are stylised**, not volumetric scattering (§6).
- **Tube light is segmented point lights**, not a true area light (§4).

The info sheet behind the gallery's ⓘ icon states the first and last of these in plain
language to the user.
