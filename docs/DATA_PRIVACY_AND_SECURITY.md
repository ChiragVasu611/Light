# DATA_PRIVACY_AND_SECURITY.md — All-in-One Lighting

**Position: v1.0 is a local-only app. It collects nothing, transmits nothing, and
requests no runtime permissions.** This is a deliberate product property, not an
accident of scope, and it is defended by tests.

---

## 1. Permission matrix

| Permission | Declared | Type | Why | When requested | If denied | Feature without it |
|---|---|---|---|---|---|---|
| `CAMERA` | **No** | — | Not needed — `setTorchMode` works without it (`FR-TORCH-08`) | Never | — | Torch works fully |
| `INTERNET` | **No** | — | Nothing in v1.0 uses a network (`FR-GLOBAL-04`) | Never | — | Everything works |
| `WRITE_SETTINGS` | **No** | special | Window brightness is used instead (`ADR-005`) | Never | — | Screen light works fully |
| `WAKE_LOCK` | **No** | normal | `FLAG_KEEP_SCREEN_ON` is used instead | Never | — | Screen stays on fine |
| `RECORD_AUDIO` | **No** | dangerous | Music Light deferred (`OQ-02`) | Never | — | n/a |
| `ACCESS_FINE_LOCATION` | **No** | dangerous | Nothing needs location | Never | — | n/a |
| `BLUETOOTH_SCAN` / `_CONNECT` | **No** | dangerous | Phase 6 at the earliest | Never | — | n/a |
| `READ/WRITE_EXTERNAL_STORAGE` | **No** | dangerous | The app reads only its own bundled assets | Never | — | n/a |
| `POST_NOTIFICATIONS` | **Conditional** | dangerous | Only for the opt-in background torch notification | At the moment the user enables *Keep torch on in background* — never before | Toggle reverts to off with an explanation; **no crash, no re-prompting** (`AC-PERM-01`) | Torch works normally in the foreground |
| `FOREGROUND_SERVICE` | **Conditional** | normal | Same opt-in gate | Install-time | n/a | — |
| `FOREGROUND_SERVICE_SPECIAL_USE` | **Conditional** | normal | Same opt-in gate | Install-time | n/a | — |

**Rules enforced by test:**
- `T-PERM-01` parses the merged manifest of the release build and fails if any
  permission outside this table appears.
- `T-SMART-03` fails if `INTERNET` is declared in v1.0.
- `T-PERM-02` fails if any permission is requested during onboarding or at app start.

### The permission we removed

The concept board shows a camera-permission dialog for the flashlight. It is not
implemented. `CameraManager.setTorchMode()` controls the torch without `CAMERA`, so
requesting it would be asking for a dangerous permission the app does not need —
which costs user trust and invites Play Store scrutiny. See `ADR-006`.

---

## 2. Data inventory

Everything the app stores, in full. There is no second list.

| Item | Where | Personal? | Why | Retention |
|---|---|---|---|---|
| `onboarding_completed` | DataStore | No | Skip onboarding on later launches | Until app data cleared |
| `keep_torch_background` | DataStore | No | User preference | Same |
| `photosensitivity_ack` | DataStore | No | Don't re-warn after acknowledgement | Same |
| `screenlight_*_color` / `_brightness` / `_mode` | DataStore | No | Restore the user's last light | Same |
| `sim_last_object`, `sim_environment`, `sim_quality` | DataStore | No | Restore simulator context | Same |
| `sim_<objectId>_*` settings | DataStore | No | Per-object simulator settings | Same |
| `reduce_motion` | DataStore | No | Accessibility preference | Same |
| `schema_version` | DataStore | No | Future migration baseline | Same |

**Not stored, anywhere:** names, email addresses, accounts, contacts, location,
photos, audio, device identifiers, advertising IDs, IP addresses, usage events,
session times, crash reports, or any identifier that could link this install to a
person or to another install.

The app creates no files outside its private `filesDir`/DataStore, writes nothing to
shared or external storage, and exposes no `ContentProvider`.

---

## 3. Data flows

```
User input ──► ViewModel ──► PreferencesRepository ──► DataStore (app-private)
                                                            │
3D assets ◄── read-only ◄── APK assets/                     │
                                                            ▼
                                              (no egress of any kind)
```

There is no network client, no socket, no `OkHttp`, no `Retrofit`, and no analytics
SDK in the v1.0 dependency graph. `T-DEP-01` fails the build if a known
networking or analytics artifact appears in the release runtime classpath.

---

## 4. Play Store Data Safety declaration (v1.0)

| Question | Answer |
|---|---|
| Does your app collect or share any user data? | **No** |
| Is all user data encrypted in transit? | n/a — no data is transmitted |
| Do you provide a way for users to request data deletion? | Clearing app data removes everything; stated in the privacy policy |
| Data types collected | **None** |
| Third-party SDKs that collect data | **None** |

This declaration must be re-reviewed the moment Phase 5 (billing) or Phase 6
(smart lights) begins — both change the answer.

---

## 5. Storage security

- DataStore lives in app-private internal storage, protected by the Android
  application sandbox.
- `android:allowBackup="false"` and no `dataExtractionRules` opt-in
  [Recommended] — the stored data is trivial preferences, and disabling backup
  removes an entire class of data-at-rest questions for zero user cost. A user who
  moves phones re-picks a colour; that is an acceptable trade.
- No encryption beyond the sandbox is warranted because no stored item is sensitive.
  **If Phase 6 stores smart-light credentials, that changes** — those go into
  Android Keystore-backed encrypted storage
  ([SMART_LIGHT_FUTURE_SPEC.md](SMART_LIGHT_FUTURE_SPEC.md) §5).
- Corrupt DataStore files are handled by a `ReplaceFileCorruptionHandler` that resets
  to defaults rather than crashing on launch.

---

## 6. Network security posture (v1.0)

```xml
android:usesCleartextTraffic="false"
```

No `INTERNET` permission is declared, which makes the above belt-and-braces. Should a
future phase add networking:

- TLS only; certificate validation never disabled in any build type.
- A `network_security_config.xml` with explicit, minimal exceptions, each justified in
  `DECISIONS.md`.
- No user data in URLs or query strings.
- No third-party SDK added without a documented review of what it transmits.

---

## 7. Logging

- One `AppLogger` interface. The release implementation forwards only `WARN` and
  `ERROR`, and never logs user-selected colours, device identifiers, or file paths
  outside the app sandbox.
- No `println`, no bare `Log.d` in shipped code — a static check (`T-LOG-01`) fails
  the build on direct `android.util.Log` usage outside the logger implementation.
- Stack traces are never shown to the user (`TECHNICAL_ARCHITECTURE.md` §6).

---

## 8. Analytics and crash reporting

**Not included in v1.0.** If proposed later, the following are mandatory, not optional:

1. **Opt-in**, off by default, with a plain-language explanation of what is sent.
2. No automatic collection before consent — the SDK is not initialised until the user
   opts in.
3. A visible opt-out in Settings that takes effect immediately and deletes any locally
   queued payloads.
4. Data minimisation: no device identifiers, no advertising ID, no IP retention beyond
   what the vendor requires for delivery.
5. A documented retention period and the vendor's data-processing terms reviewed.
6. The Play Data Safety declaration updated *before* release.
7. An ADR recording the decision and the alternatives considered.

Where crash reporting is genuinely valuable, prefer Google Play's built-in
Android vitals, which requires no SDK, no permission, and no consent flow, over
embedding a third-party crash SDK. [Recommended]

---

## 9. Children and sensitive contexts

The app has no content rating concerns, no user-generated content, no social features
and no ads. The one genuine safety consideration is **photosensitivity**: the
emergency strobe is rate-clamped to 5 Hz and gated behind a one-time explicit warning
(`FR-EMER-05`), and the candle flicker is bounded (`FR-CANDLE-01`).

The emergency screen also carries a permanent disclaimer that the app does not contact
emergency services and does not guarantee visibility (`FR-EMER-08`) — an honesty
requirement, and a liability one.

---

## 10. Supply chain

- All dependencies are pinned to exact versions in `gradle/libs.versions.toml`; no
  dynamic versions (`+`, `latest.release`) anywhere.
- The Gradle wrapper is committed with `distributionSha256Sum` set.
- Dependency sources are limited to `google()` and `mavenCentral()` — no arbitrary
  third-party Maven repositories.
- New dependencies require an ADR entry stating what they do, their licence, their
  size impact, and what they transmit.
- No secret, key, keystore or signing credential is committed. Release signing config
  reads from environment variables or a git-ignored `keystore.properties`;
  `.gitignore` covers `*.jks`, `*.keystore`, `keystore.properties`, `local.properties`.
- `T-DEP-01` asserts the release runtime classpath contains no networking or analytics
  artifact.

---

## 11. Privacy policy content requirements

The bundled, offline privacy policy (shown in `SCR-ABOUT`, no network needed) must
state, in plain language:

1. The app collects no personal data.
2. The app has no internet permission and transmits nothing.
3. What is stored locally (a short list of preferences) and that clearing app data
   removes it.
4. That the flashlight is controlled without camera access.
5. A contact address for questions.
6. A "last updated" date.

It must not contain boilerplate about analytics, advertising partners, or data sharing
that does not apply — a policy describing collection the app does not do is both
misleading and a Data Safety mismatch.
