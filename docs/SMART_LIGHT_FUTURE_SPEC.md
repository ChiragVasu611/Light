# SMART_LIGHT_FUTURE_SPEC.md — All-in-One Lighting

**Status: `[Deferred]` to Phase 6. v1.0 ships interfaces and an informational screen
only.**

The purpose of this document is to make Phase 6 an *addition* rather than a redesign.
Nothing here is implemented in v1.0, and nothing here may be used to justify shipping a
control that does not control anything.

---

## 1. v1.0 scope — what actually ships

| Ships in v1.0 | Does **not** ship in v1.0 |
|---|---|
| `SCR-SMARTLIGHT` informational screen (`FR-SMART-01`) | Any discovery, scan, pair, on/off, brightness or colour control |
| "Coming Soon" badge on every entry point (`FR-SMART-02`) | Any network call, socket, mDNS query or BLE scan |
| Domain interfaces in `domain/smartlight/` with **no** implementation (`FR-SMART-04`) | `INTERNET`, `BLUETOOTH_*` or location permissions |
| A `FakeSmartLightProvider` used only by tests | Any vendor SDK dependency |

**Hard rule.** No disabled toggle, no greyed slider, no empty device list with a
"Scan" button. A control that cannot work must not exist in the layout
(`AC-SMART-01`). The informational screen describes the future in the future tense.

---

## 2. Protocol landscape

Smart bulbs do **not** share one protocol. Choosing an integration is a hardware and
business decision, not a coding one — which is precisely why it is deferred.

| Option | Transport | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Matter** (over Wi-Fi / Thread) | Local IP, standardised | Vendor-neutral; the long-term direction of the industry; local control; strong commissioning security | On Android, control normally goes through Google Home / Play Services APIs rather than a raw stack; requires commissioned devices; a full on-device Matter controller is a large undertaking | **Recommended long-term target**, evaluated first in Phase 6 |
| **Philips Hue local HTTP API** | LAN HTTP to a Bridge | Well documented, stable, genuinely local, easy to test; a Bridge is affordable | Requires a Bridge; Hue-only; link-button pairing flow | **Recommended first integration** if a single ecosystem is chosen |
| **LIFX LAN protocol** | LAN UDP | No hub; documented binary protocol | LIFX-only; UDP discovery is fiddly on some networks | Reasonable second |
| **Tuya / Smart Life** (the bulk of budget bulbs) | Cloud API, or local with per-device keys | Enormous device coverage | Cloud account + API credentials; local keys are awkward to obtain; terms of use constrain redistribution | Evaluate, do not assume |
| **Generic BLE bulbs** | Bluetooth LE | No network needed | No standard GATT profile; per-vendor reverse engineering; needs location-adjacent permissions on Android | Not recommended |
| **Google Home APIs** | Via Play Services | Reaches many ecosystems at once | Requires Google account linking; Play Services dependency; less "local-first" | Evaluate alongside Matter |

**Recommendation for Phase 6 [Recommended]:** pick **one** ecosystem the team owns
physical hardware for, implement it end to end behind the provider interface, and ship
it before adding a second. Philips Hue is the best first candidate on documentation
quality and testability; Matter is the strategic target once the Android control story
is settled. This choice is `OQ-08` and must be resolved with real hardware in hand.

---

## 3. Domain interfaces (present in v1.0, unimplemented)

These are the only smart-light code that exists in v1.0. They compile, they are
covered by tests against a fake, and they have no production implementation.

```kotlin
package com.allinonelighting.domain.smartlight

/** One vendor/protocol adapter. Phase 6 adds exactly one implementation. */
interface SmartLightProvider {
    val id: ProviderId
    val displayNameRes: Int

    /** Cold flow; emits devices as they are found. Cancelling stops discovery. */
    fun discover(): Flow<DiscoveredDevice>

    suspend fun pair(device: DiscoveredDevice, token: PairingToken?): AppResult<SmartLightDevice>
    suspend fun connect(deviceId: DeviceId): AppResult<SmartLightDevice>
    suspend fun forget(deviceId: DeviceId): AppResult<Unit>
    val knownDevices: Flow<List<SmartLightDevice>>
}

interface SmartLightDevice {
    val id: DeviceId
    val displayName: String
    val capabilities: DeviceCapabilities
    val connection: StateFlow<ConnectionState>
    val lightState: StateFlow<LightState>

    suspend fun setPower(on: Boolean): AppResult<Unit>
    suspend fun setBrightness(level: Float): AppResult<Unit>          // 0f..1f
    suspend fun setColor(color: LightColor): AppResult<Unit>
    suspend fun setColorTemperature(kelvin: Int): AppResult<Unit>
}

/** Capability reporting: the UI renders only what the device actually supports. */
data class DeviceCapabilities(
    val supportsPower: Boolean,
    val supportsBrightness: Boolean,
    val supportsColor: Boolean,
    val supportsColorTemperature: Boolean,
    val colorTemperatureRangeKelvin: IntRange?,
    val supportsGroups: Boolean,
    val supportsScenes: Boolean,
    val isLocalControl: Boolean          // false ⇒ commands traverse a vendor cloud
)

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val latencyMs: Long?) : ConnectionState
    data class Unreachable(val since: Long, val reason: SmartLightError) : ConnectionState
}

sealed interface SmartLightError {
    data object NetworkUnavailable : SmartLightError
    data object NotOnSameNetwork : SmartLightError
    data object AuthExpired : SmartLightError
    data object PairingRejected : SmartLightError
    data object DeviceUnreachable : SmartLightError
    data object RateLimited : SmartLightError
    data class Protocol(val detail: String) : SmartLightError
}
```

**`DeviceCapabilities` is the mechanism that keeps `FR-GLOBAL-01` true in Phase 6.**
A bulb that cannot do colour must not render a colour wheel. The UI is built from the
capability record, not from an assumed feature set.

`isLocalControl` is surfaced to the user: a device controlled through a vendor cloud
behaves differently (latency, outages, privacy) and the user deserves to know which
they have.

---

## 4. Discovery and pairing (Phase 6 design)

### Discovery

| Protocol | Mechanism | Permission implication on Android |
|---|---|---|
| Hue Bridge | mDNS `_hue._tcp`, plus the vendor discovery endpoint as fallback | `INTERNET`; mDNS on the local network needs no extra permission, but see below |
| LIFX | UDP broadcast on 56700 | `INTERNET` |
| Matter | Commissioning via the platform's own flow | Platform-mediated |
| BLE | GATT scan | `BLUETOOTH_SCAN`; historically location-adjacent on older APIs |

**Local-network access is itself becoming a gated capability.** Phase 6 must verify the
current requirements on the then-current target SDK before designing the permission
flow, rather than assuming today's rules hold. Tracked as `OQ-09`.

### Pairing

Pairing is **always** user-initiated and always requires a physical or explicit act
of consent on the device side (Hue's link button, a Matter commissioning code, a
vendor account login). The app never attempts silent pairing and never retries a
rejected pairing automatically.

Pairing UI sequence: explain what will happen → user starts → physical action prompt
with a timeout and a visible countdown → success or a specific failure with a retry.

---

## 5. Security and credentials

| Concern | Rule |
|---|---|
| Storage of tokens/keys | Android Keystore-backed encryption. **Never** in `SharedPreferences`, DataStore plaintext, source, or logs. |
| Secrets in source control | Forbidden. No vendor client secret is committed; if an integration needs one it is injected at build time from a local, git-ignored properties file and documented in the implementation guide. |
| Transport | TLS for any cloud call; certificate validation never disabled, not even in debug. For LAN HTTP that a vendor serves without TLS (some bridges), the limitation is documented to the user, and such traffic carries no personal data. |
| Cleartext | `android:usesCleartextTraffic="false"` plus a network security config allowing cleartext **only** to explicitly configured local addresses if a vendor requires it. |
| Logging | Device tokens, bulb serials and network addresses are never logged, including in debug builds. |
| Revocation | "Forget device" deletes the stored credential and, where the protocol supports it, revokes it on the device/bridge. |
| Least privilege | The app requests only the permissions the *chosen* protocol needs, at the moment the user starts pairing — never at launch. |

---

## 6. Privacy

Phase 6 is the first time the app touches a network, which changes its privacy
posture. Required before any smart-light code ships:

- The Play Data Safety declaration is updated to reflect local-network and/or cloud
  access.
- The in-app privacy policy gains a smart-light section stating what is stored
  (device identifiers and credentials, locally) and what is transmitted (commands,
  to the device or its vendor cloud).
- Device names, which users often personalise ("Bedroom"), are treated as user content:
  stored locally, never transmitted anywhere except the vendor's own API, never logged.
- No telemetry about a user's home devices is collected.

---

## 7. Testing without hardware

Phase 6 cannot block on a lab. The strategy:

| Layer | Approach |
|---|---|
| Provider logic | `FakeSmartLightProvider` — scriptable discovery, pairing outcomes, latency, and every `SmartLightError` |
| Protocol encoding | Golden-file tests against captured real request/response payloads |
| Network transport | `MockWebServer` for HTTP providers; a loopback UDP responder for LIFX-style protocols |
| Reconnection | Virtual-clock tests over the backoff schedule (§8) |
| UI | Compose tests driven by the fake, covering: empty, discovering, found, pairing, paired, unreachable, auth-expired, capability-limited (no colour) |
| Real hardware | A documented manual checklist executed against at least one physical device before the feature leaves beta |

A fake alone is never sufficient evidence that an integration works; the manual
hardware checklist is a release gate for Phase 6.

---

## 8. Connection reliability

- Reconnection uses exponential backoff with jitter: 1 s, 2 s, 4 s, 8 s, 16 s, then
  every 30 s, reset on success. No unbounded tight retry.
- Discovery and reconnection run only while a smart-light screen is visible; v1.0's
  no-background-work rule (`NFR-BAT-02`) is relaxed in Phase 6 only with an explicit
  decision recorded in `DECISIONS.md`.
- A command that fails leaves the UI showing the **last known device state**, plus an
  error — never an optimistic state. Same rule as the torch (`FR-GLOBAL-02`).
- Commands are debounced (brightness drags) and the latest-wins, so dragging a slider
  does not queue fifty packets.

---

## 9. Groups and scenes

`[Deferred]` beyond the first working adapter. `DeviceCapabilities.supportsGroups` /
`supportsScenes` exist so the UI can be built capability-first when the time comes.
A plausible future tie-in — applying a 3D simulator configuration (colour temperature,
brightness) to a real bulb — is attractive but must not influence v1.0 architecture
beyond the interfaces already defined.

---

## 10. Phase 6 entry criteria

Smart-light work does not begin until **all** of these are true:

1. v1.0 has shipped and is stable (crash-free ≥ 99.5 %).
2. A target ecosystem is chosen and the team physically owns the hardware (`OQ-08`).
3. The official SDK or protocol documentation for that ecosystem has been read and its
   licensing and terms of use permit our use.
4. The permission and local-network requirements have been verified against the
   then-current target SDK (`OQ-09`).
5. The privacy policy and Play Data Safety updates are drafted.

Until then, the Smart Lights screen says "Coming Soon" and means it.
