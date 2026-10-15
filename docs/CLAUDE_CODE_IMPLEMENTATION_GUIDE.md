# CLAUDE_CODE_IMPLEMENTATION_GUIDE.md — All-in-One Lighting

Operating rules for any coding agent working in this repository. Read this before
touching code.

---

## 1. Before you write anything

1. **Inspect the repository.** Read what exists. Do not assume a file is absent, and
   do not assume one is present.
2. **Read the relevant specs.** For a screen: `SCREEN_SPECIFICATION.md` +
   `UI_DESIGN_SYSTEM.md`. For hardware: `ANDROID_HARDWARE_SPEC.md`. For 3D:
   `THREE_D_LIGHTING_SPEC.md`. Always: `TECHNICAL_ARCHITECTURE.md`.
3. **Check the roadmap.** Work the current phase's tasks in order. Do not start a
   later phase before the current phase's exit criteria are met and evidenced.
4. **Never overwrite existing work without reading it first.**
5. **Verify APIs you are unsure about.** Check the actual library version, read the
   actual signature. Do not invent an API, a parameter, or a capability. If you cannot
   verify it, say so and stop — a plausible-looking wrong API is worse than a question.

---

## 2. Repository conventions

### Layout

```
/
├── docs/                      specification package (this directory)
├── gradle/libs.versions.toml  the ONLY place dependency versions live
├── gradlew, gradle/wrapper/   committed, checksum-pinned
├── settings.gradle.kts
├── build.gradle.kts           root
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro     Filament keep rules live here
│   └── src/
│       ├── main/kotlin/com/allinonelighting/...
│       ├── main/res/
│       ├── main/assets/       catalog.json, models/, thumbs/, env/, materials/
│       ├── test/              JVM: unit, state-holder, Robolectric, static checks
│       └── androidTest/       device: Compose UI, renderer integration
└── benchmark/                 Phase 4
```

Package structure is fixed by `TECHNICAL_ARCHITECTURE.md` §2 and enforced by
`T-ARCH-01`.

### Kotlin style

- Official Kotlin style; 4-space indent; 100-column soft limit.
- Explicit visibility on public API; `internal` by default inside the app module.
- `val` over `var`; immutable data classes for state.
- No `!!`. No bare `catch (e: Exception)` that swallows — map to `AppError` or rethrow.
- Sealed interfaces for state and error hierarchies.
- Suspending functions for anything that can block; never block the main thread.
- Named arguments for any call with more than two parameters of the same type.

### Compose

- Composables are `@Composable fun PascalCase()` returning `Unit`.
- Signature shape: `fun Screen(state: UiState, onEvent: (Event) -> Unit, modifier: Modifier = Modifier)`.
- `modifier: Modifier = Modifier` is always the first optional parameter.
- **No business logic in composables.** No repository calls, no `CameraManager`, no
  coroutine launches except `LaunchedEffect` for UI concerns.
- State hoisted; stateless composables wherever possible.
- `collectAsStateWithLifecycle()`, never `collectAsState()` for app state.
- Every public component gets a `@Preview` using `AppTheme`.
- Never read `Settings.Global`/`Settings.System` from a composable — go through a
  provided composition local or the ViewModel.

### File size

One screen per package: `XxxScreen.kt`, `XxxViewModel.kt`, `XxxUiState.kt`,
`XxxEvents.kt`. If a file passes ~400 lines, split it along a real seam — do not
create an abstraction layer just to reduce line count.

### Naming

| Thing | Convention | Example |
|---|---|---|
| Screen composable | `<Name>Screen` | `FlashlightScreen` |
| ViewModel | `<Name>ViewModel` | `FlashlightViewModel` |
| State | `<Name>UiState` | `FlashlightUiState` |
| Event | `<Name>Event` | `FlashlightEvent` |
| Component | `App<Thing>` | `AppSegmentedControl` |
| Fake | `Fake<Interface>` | `FakeTorchController` |
| Test | backticked behaviour sentence | `` `toggle fails leaves state off` `` |
| String resource | `<screen>_<purpose>` | `flashlight_unavailable_no_flash` |

---

## 3. Hard rules

These are not style preferences. Violating one is a defect.

1. **No fake functionality.** A control either does its documented job, is visibly
   disabled with a stated reason, or is labelled Coming Soon. Never a no-op `onClick`.
2. **No optimistic hardware state.** The UI shows a hardware state only after the OS
   confirms it. (`FR-GLOBAL-02`)
3. **No invented APIs.** If you are not certain a method exists with that signature in
   the pinned version, verify it or stop.
4. **No claimed test results.** Never write "tests pass" unless you ran the command
   and read the output. Paste the real result.
5. **No ignored errors.** Compiler errors, test failures, lint errors and runtime
   exceptions are fixed, not suppressed. `@Suppress` requires a comment explaining why
   and an entry in `DECISIONS.md` if it is architectural.
6. **No hard-coded design values.** Colours, spacings, radii and durations come from
   tokens. (`T-DS-02`)
7. **No hard-coded user-facing strings.** Everything in `strings.xml`. (`T-GLOBAL-02`)
8. **No business logic in composables.**
9. **No secrets in source control.** `local.properties`, `keystore.properties`,
   `*.jks`, `*.keystore` are git-ignored. No API key, token or password in any
   committed file.
10. **No new permission** without updating `DATA_PRIVACY_AND_SECURITY.md` §1 and
    `T-PERM-01`.
11. **No new dependency** without an ADR recording purpose, licence, size impact, and
    what it transmits.
12. **No Filament call off the main thread.** File I/O is fine on IO; every engine,
    scene, view, renderer and transform call is main-thread.
13. **No rendering while the simulator is not resumed.**
14. **No unbounded animation.** Flicker is clamped; animation is elapsed-time driven.
15. **Tests change with behaviour.** Changing behaviour without touching a test means
    the behaviour was untested — add the test.
16. **Record deviations.** Any departure from these documents goes in `DECISIONS.md`
    with its rationale, before or with the change.
17. **Update the traceability matrix** as features land.
18. **Never claim the app is bug-free.** Report evidence: what you built, what you ran,
    what passed, what you know is broken.

---

## 4. Validation commands

Run the ones relevant to your change. Report actual output.

```bash
./gradlew assembleDebug                    # must succeed before you claim anything compiles
./gradlew testDebugUnitTest                # JVM tests
./gradlew connectedDebugAndroidTest        # device/emulator tests
./gradlew lintDebug                        # lint
./gradlew :app:assembleRelease             # R8 — catches Filament keep-rule bugs
./gradlew check                            # unit tests + lint
./gradlew :app:dependencies --configuration releaseRuntimeClasspath   # dependency audit
```

**Minimum bar for any code change:** `assembleDebug` + `testDebugUnitTest`.
**Minimum bar for a UI change:** add `connectedDebugAndroidTest`.
**Minimum bar for a Filament change:** add `assembleRelease` and, on a device, `T-REL-01`.

If a command fails, fix the cause. Do not proceed to the next task with a red build,
and do not report a task complete with failing tests.

---

## 5. Implementation order

Follow `ROADMAP.md`. Within a task:

1. Read the spec sections for the thing you are building.
2. Write or update the test first where the behaviour is testable logic
   (state transitions, timing, math, error mapping). For pure-layout work, build the
   component then the UI test.
3. Implement the smallest complete slice.
4. Run `assembleDebug` and the relevant tests.
5. Fix everything red.
6. Update the traceability matrix.
7. Report (§6).

Incremental means: build and test after each meaningful change, not once at the end of
a phase.

---

## 6. Required task report

After every completed task, report exactly this:

```
## Task: <id and name>

### 1. Files created or modified
- path/to/File.kt — what changed and why

### 2. Behaviour implemented
- Plain description, with the FR-/AC- ids it satisfies

### 3. Tests added or updated
- T-XXX-NN — what it asserts

### 4. Commands executed and actual results
$ ./gradlew assembleDebug
BUILD SUCCESSFUL in 42s

$ ./gradlew testDebugUnitTest
BUILD SUCCESSFUL in 18s
37 tests, 0 failures

### 5. Known issues
- Anything broken, unverified, or working around a limitation. "None" only if true.

### 6. Remaining work in this task
- Anything deliberately left, and why.

### 7. Next task
- The next item in the approved roadmap.
```

Section 4 must contain **real output**. Paraphrasing, summarising, or writing
"all tests passed" without the output is a process violation. If a command was not
run, say so explicitly rather than omitting it.

---

## 7. When the spec is wrong

The specification is a plan, not scripture. If implementation reveals that something
is impossible, unsafe, or clearly worse than an alternative:

1. **Stop** before building the wrong thing.
2. State the problem concretely: what the spec says, what is actually true, what you
   verified.
3. Recommend a specific alternative.
4. If the decision materially affects cost, hardware compatibility or release scope —
   **ask**. Do not decide it silently.
5. If it is a local implementation detail, decide it, implement it, and record it in
   `DECISIONS.md` with the rationale.

Examples of things to escalate rather than decide: dropping a shipped feature, adding
a permission, adding a network dependency, changing minSdk, changing the renderer.

Examples of things to decide and record: a different Compose idiom, an extra internal
helper, a test-structure change, a token renamed for clarity.

---

## 8. Working with the concept board

The supplied UI board is the **visual** reference. Where it conflicts with Android
reality or with the safety and honesty rules, this documentation wins, and
`UI_DESIGN_SYSTEM.md` §12 lists every such deviation with its reason.

Do not re-add a removed control because the board shows it. Do not invent a control the
board does not show without a spec entry. If you believe a deviation is wrong, raise it
(§7) rather than quietly reverting it.

---

## 9. Definition of done

See `ROADMAP.md` §"Cross-phase definition of done". Short version: implemented, tested,
tests actually run, build green, matrix updated, deviations recorded, report produced.

---

## 10. Things that will get a change rejected

- A control with an empty `onClick`
- A `TODO()` on a path the user can reach
- A test that asserts nothing, or that is `@Ignore`d without an issue reference
- A claimed result with no command output
- A hard-coded colour, dp, sp, or user-facing string
- A new permission with no documentation update
- A `catch (e: Exception) {}` that swallows silently
- Business logic inside a composable
- A Filament call off the main thread
- Engine teardown outside `FilamentEngineHost.release()`
- Any statement that the app is bug-free, complete, or guaranteed to work
