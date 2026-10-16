# Handoff — All-in-One Lighting

Paste this as your first message in the new session.

---

I'm continuing work on an Android app at `/Users/deo/Desktop/AllInOneLighting`.
It's a git repo on branch `master`, working tree clean, 8 commits.

**Start by reading `docs/README.md`, then `docs/ROADMAP.md` and
`docs/CLAUDE_CODE_IMPLEMENTATION_GUIDE.md`.** The `docs/` directory is the agreed
source of truth — 17 specification documents — and the implementation guide contains
the rules you must follow. Do not re-plan the project; follow what's written.

## Where things stand

**Phase 0 (documentation): complete.** 17 docs, all cross-referenced and consistent.

**Phase 1 (application foundation): nearly complete.** Built and verified on real
hardware. What exists: Gradle build, full design-system token set, core result/error
types, `AppContainer` (manual DI), DataStore preferences repository, 11 reusable
components, navigation with typed route arguments, and these working screens —
onboarding, settings, permissions, about, smart light (informational), more tools.
Home, 3D gallery, flashlight, screen light, candle, emergency, display settings and
licences are deliberate honest placeholders that say they're not implemented yet.

**Remaining in Phase 1 — do these in order:**

1. **1.11b** — Display settings screen (default 3D environment, default quality mode,
   reduce-motion toggle) and the Licences screen. Both static; no hardware needed.
   The Licences screen is release gate 14 and must list Filament (Apache 2.0).
2. **Onboarding illustrations** — pages currently render text-only, leaving a visible
   gap. Must be WebP, ≤120 KB, with **no text baked into the image** (FR-GLOBAL-03).
3. **1.9b is NOT Phase 1 work** — the real Home layout lands with torch control in
   Phase 2, because its hero card needs live torch state.

**Do not start Phase 2 until Phase 1 exit criteria in `docs/ROADMAP.md` are met.**

## Verification — always run these and report real output

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
./gradlew assembleDebug
./gradlew testDebugUnitTest        # currently 39 tests, 0 failures
./gradlew lintDebug
./gradlew assembleRelease          # R8 — catches Filament keep-rule bugs later
./gradlew connectedDebugAndroidTest   # needs a device; 49 tests, 0 failures
```

Never claim a test passed without running it. `JAVA_HOME` must be set to JDK 17 —
the machine's default is JDK 26 and the build fails with it.

## Toolchain facts you must not re-litigate

These were each discovered by a failing build; they are correct and documented in
`docs/DECISIONS.md` (ADR-002, ADR-021, ADR-022):

- **Gradle 9.8.1** — AGP 9.4.1 requires ≥ 9.6.0.
- **No `org.jetbrains.kotlin.android` plugin** — AGP 9 has built-in Kotlin support and
  rejects it. Only `kotlin.plugin.compose` is applied. If a tutorial tells you to add
  the Kotlin Android plugin, it is pre-AGP-9 and wrong.
- **compileSdk 37** (platform android-37.0 is installed), **targetSdk 36**, **minSdk 24**.
- Manual DI via `AppContainer`. No Hilt, no KSP. Deliberate (ADR-003).

## Test devices

Two real devices have been used. Connect one and set `ANDROID_SERIAL`.

| Device | Serial | Android | Width | Flash |
|---|---|---|---|---|
| OPPO CPH2127 (phone) | `468efcd0` | 12 / API 31 | Compact | **yes**, no strength API |
| Samsung SM-X110 (tablet) | `R9ZW90JWCMY` | 15 / API 35 | Medium, 621 dp | **no flash unit** |

The tablet is the no-flash device needed to verify `AC-HOME-02` in Phase 2. Neither
device can exercise the torch flash-strength path (needs API 33+ *and*
`FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1`), so that stays unverified on hardware.

## Rules that matter most

From `docs/CLAUDE_CODE_IMPLEMENTATION_GUIDE.md` §3 — read all 18, but these bite hardest:

1. **No fake functionality.** Every control works, is disabled with a visible reason,
   or is labelled Coming Soon. No empty `onClick`.
2. **No optimistic hardware state.** The UI shows torch "On" only after the OS
   confirms it via `CameraManager.TorchCallback` — never after our own command.
   `PowerToggle` is already built to make this structurally impossible; don't add
   internal state to it.
3. **No design literals.** No raw `Color(0x…)`, `N.dp` or `N.sp` outside
   `ui/theme`. A test (`T-DS-02`) fails the build on violations.
4. **No hard-coded user-facing strings.** Everything in `strings.xml`.
5. **No new permission** without updating `docs/DATA_PRIVACY_AND_SECURITY.md` §1.
   v1.0 requests **zero** runtime permissions — including no `CAMERA`, because
   `setTorchMode()` doesn't need it (ADR-006).
6. Record any deviation in `docs/DECISIONS.md`; update `docs/TRACEABILITY_MATRIX.md`
   as features land.

## Report format required after each task

Files changed · behaviour implemented (with FR-/AC- ids) · tests added · **commands run
with their actual output** · known issues · remaining work · next task.

## Two blockers to raise early

- **OQ-11** — nobody has produced the five 3D models yet. **Phase 3 cannot start
  without them.** They must follow the `LIGHT_ANCHOR` convention in ADR-014. A
  placeholder cube does not validate that convention.
- **OQ-01** — whether in-app billing is in scope at all. Blocks Phase 5.

Both are in `docs/KNOWN_LIMITATIONS.md` §4 with recommended defaults.
