# UI_DESIGN_SYSTEM.md — All-in-One Lighting

Derived from the supplied concept board (reviewed in full) and expressed as
implementable Compose/Material 3 tokens. **Rule: no composable may use a raw colour,
dp or sp value that is not defined here.**

---

## 1. Visual identity

The board reads as: near-black navy ground, slightly lifted navy card surfaces,
a single saturated electric blue for primary action and selection, cyan reserved
for glow and emphasis, white headings, muted blue-grey body text, generous rounding,
and photographic/3D light imagery as the hero of each feature screen.

Glow is used as an *accent on the light sources themselves*, never as chrome. Cards
do not glow. Buttons do not glow. Only lights glow.

The app is **dark-only** in v1.0. A light theme is `[Deferred]` — a lighting tool is
used in the dark, and a light theme would double the design and test surface for
little benefit. (`ADR-010`)

---

## 2. Colour tokens

### 2.1 Raw palette (`ui/theme/Color.kt`)

| Token | Hex | Role |
|---|---|---|
| `Ink900` | `#05070F` | App background, deepest ground |
| `Ink800` | `#0A0F1D` | Scrim / behind-sheet ground |
| `Ink700` | `#111828` | Card surface (default) |
| `Ink600` | `#182036` | Card surface raised / pressed |
| `Ink500` | `#222C47` | Divider, inactive track, outline |
| `Ink400` | `#38435F` | Disabled container |
| `Slate300` | `#8A97B4` | Secondary text, inactive icon |
| `Slate200` | `#B6C0D6` | Tertiary emphasis text |
| `White` | `#FFFFFF` | Primary text, active icon |
| `Blue500` | `#1F6FE8` | **Primary** — filled buttons, selected segment, active nav |
| `Blue400` | `#5B9BFF` | Primary hover/pressed, focus ring |
| `BlueGlow` | `#1B3A6B` | Primary container, selected card tint |
| `Cyan400` | `#4BD8FF` | Accent — glow, highlights, 3D rim |
| `Amber400` | `#FFB648` | Warm light, candle, warning |
| `Red500` | `#E5484D` | Emergency, destructive, error |
| `Green500` | `#3DD68C` | Success, "Ready" state |

### 2.2 Semantic mapping (Material 3 `darkColorScheme`)

| M3 slot | Token | Notes |
|---|---|---|
| `primary` | `Blue500` | |
| `onPrimary` | `White` | contrast 4.8:1 on `Blue500` |
| `primaryContainer` | `BlueGlow` | selected card background |
| `onPrimaryContainer` | `White` | |
| `secondary` | `Cyan400` | accent only; never a button fill |
| `onSecondary` | `Ink900` | |
| `background` | `Ink900` | |
| `onBackground` | `White` | |
| `surface` | `Ink700` | cards, sheets |
| `onSurface` | `White` | |
| `surfaceVariant` | `Ink600` | nested surfaces, slider tracks |
| `onSurfaceVariant` | `Slate300` | supporting text |
| `surfaceContainerHigh` | `Ink600` | bottom sheets, dialogs |
| `outline` | `Ink500` | card borders, dividers |
| `outlineVariant` | `Ink400` | |
| `error` | `Red500` | |
| `onError` | `White` | |
| `scrim` | `Ink800` @ 72 % | |

### 2.3 Feature accent colours

Each lighting tool carries one accent used for its icon tile only — never for text
or large fills.

| Feature | Accent | Icon tile background |
|---|---|---|
| Phone Flashlight | `Blue500` | `BlueGlow` |
| Screen Light | `Cyan400` | `Cyan400` @ 14 % |
| Emergency | `Red500` | `Red500` @ 14 % |
| Candle / Night Light | `Amber400` | `Amber400` @ 14 % |
| More Tools | `Slate200` | `Ink600` |
| 3D Simulator | `Cyan400` | `Cyan400` @ 14 % |
| Smart Lights | `Slate300` | `Ink600` (muted — it is Coming Soon) |

### 2.4 Contrast obligations (`NFR-A11Y-02`)

| Pair | Measured | Required | Verdict |
|---|---|---|---|
| `White` on `Ink900` | 20.12:1 | 4.5 | pass (AAA) |
| `White` on `Ink700` | 17.72:1 | 4.5 | pass |
| `White` on `Ink600` | 16.17:1 | 4.5 | pass |
| `White` on `BlueGlow` | 11.27:1 | 4.5 | pass |
| `White` on `Blue500` | **4.67:1** | 4.5 | pass — see note |
| `Slate200` on `Ink700` | 9.70:1 | 4.5 | pass |
| `Slate300` on `Ink900` | 6.87:1 | 4.5 | pass |
| `Slate300` on `Ink700` | 6.05:1 | 4.5 | pass |
| `Cyan400` on `Ink900` | 12.04:1 | 4.5 | pass |
| `Amber400` on `Ink700` | 10.16:1 | 3.0 | pass (non-text/large) |
| `Green500` on `Ink700` | 9.45:1 | 3.0 | pass |
| `Blue400` on `Ink700` | 6.39:1 | 3.0 | pass |
| `Red500` on `Ink900` | 5.14:1 | 3.0 | pass |
| `Red500` on `Ink700` | 4.53:1 | 3.0 | pass |
| `Blue500` on `Ink900` | 4.31:1 | 3.0 | pass (UI component, WCAG 1.4.11) |
| `Blue500` on `Ink700` | 3.79:1 | 3.0 | pass (UI component) |
| `Ink400` disabled text on `Ink700` | 2.0:1 | — | **intentional fail** — disabled content is exempt, but every disabled control must also carry a visible reason in `Slate300` |

> **`Blue500` was darkened from `#2E7DF7` to `#1F6FE8` on 2026-10-16.** The original
> measured **3.89:1** against white, failing the 4.5:1 body minimum — and it is the
> primary button fill with white labels. `T-A11Y-02` caught this before any button was
> built. `#1F6FE8` is the closest passing colour to the concept board's blue.
> Button labels are `labelLarge` (15 sp, SemiBold), which is below WCAG's large-text
> threshold (14 pt bold ≈ 18.66 px), so the 4.5:1 body minimum genuinely applies.
>
> **`Blue500` is a fill colour and must never be used for body text.**

These are **measured** values, emitted by `T-A11Y-02` on 2026-10-16, not estimates.
The test fails the build if any pairing regresses.

---

## 3. Typography

Font: **Inter** (variable, bundled) with a system `SansSerif` fallback. Inter is
SIL OFL 1.1 — licence recorded in `docs/licences/`. [Recommended]

| Style | Size / Line | Weight | Letter | Used for |
|---|---|---|---|---|
| `displayLarge` | 36 / 44 sp | 700 | −0.5 | Splash title only |
| `headlineMedium` | 26 / 32 sp | 700 | −0.3 | Onboarding headline |
| `titleLarge` | 20 / 26 sp | 600 | 0 | Top app bar title |
| `titleMedium` | 17 / 24 sp | 600 | 0 | Card title, list row title |
| `bodyLarge` | 15 / 22 sp | 400 | 0 | Body copy, dialog text |
| `bodyMedium` | 14 / 20 sp | 400 | 0 | Card supporting line |
| `labelLarge` | 15 / 20 sp | 600 | +0.1 | Button label |
| `labelMedium` | 13 / 16 sp | 600 | +0.2 | Segment label, chip |
| `labelSmall` | 11 / 14 sp | 600 | +0.4 | Bottom-nav label, badge, "COMING SOON" |

Rules:
- All sizes in `sp`; never `dp` for text.
- `maxLines` + `TextOverflow.Ellipsis` on every single-line label; no `maxLines`
  on body copy.
- Headings use `FontWeight.SemiBold`/`Bold`; never synthetic bold.
- At font scale ≥ 1.5, `titleMedium` card titles may wrap to 2 lines and cards grow;
  cards must not have fixed heights (`NFR-A11Y-*`).

---

## 4. Spacing, radius, elevation

### Spacing scale (`Spacing.kt`) — use these names, never literals

| Token | dp |
|---|---|
| `xxs` | 2 |
| `xs` | 4 |
| `sm` | 8 |
| `md` | 12 |
| `lg` | 16 |
| `xl` | 20 |
| `xxl` | 24 |
| `xxxl` | 32 |
| `huge` | 48 |

Layout defaults: screen horizontal padding `lg` (16), section gap `xxl` (24),
intra-card padding `lg` (16), gap between grid cells `md` (12).

### Corner radii (`Radius.kt`)

| Token | dp | Applied to |
|---|---|---|
| `rSm` | 8 | Chips, small icon tiles |
| `rMd` | 14 | List rows, segmented container |
| `rLg` | 20 | Standard cards, dialogs |
| `rXl` | 28 | Hero card, bottom sheets (top corners) |
| `rPill` | 999 | Pills, filter chips, FAB-style buttons |

### Elevation

Material 3 tonal elevation is **disabled** — on a near-black ground it produces muddy
grey. Depth comes from explicit surface tokens plus a 1 dp `outline` hairline.

| Surface | Background | Border |
|---|---|---|
| Card (rest) | `Ink700` | 1 dp `Ink500` @ 60 % |
| Card (pressed) | `Ink600` | 1 dp `Ink500` |
| Card (selected) | `BlueGlow` | 1 dp `Blue500` |
| Dialog / Sheet | `Ink600` | 1 dp `Ink500` |
| Bottom nav | `Ink800` | 1 dp top `Ink500` @ 50 % |

Shadows: none. `shadowElevation = 0.dp` everywhere. [Recommended — matches the board,
where separation reads as tone, not drop shadow.]

---

## 5. Iconography

- Source: `androidx.compose.material.icons` extended set where a match exists;
  custom vector drawables otherwise, authored on a 24 dp grid with 2 dp stroke.
- Sizes: `16` (inline), `20` (list row), `24` (default, nav), `28` (hero card).
- Icon tiles: a rounded square `40 × 40` dp (`rSm`) filled with the feature accent
  at 14 % opacity, icon at 20 dp in full accent colour.
- Decorative icons take `contentDescription = null`; the parent row merges semantics.

---

## 6. Components

### CMP-CARD-HERO — Hero feature card (Home flashlight)
- Full width, `rXl`, padding `lg`, `Ink700`, hairline border.
- Left: 48 dp icon tile. Centre: title `titleMedium` + status `bodyMedium` in
  `Slate300`. Right: `CMP-POWER` control.
- Selected/active state: background `BlueGlow`, border `Blue500`.
- Disabled: background `Ink700`, title `Slate300`, status in `Slate300` carrying the
  reason, power control not actuable, whole row `semantics { disabled() }`.

### CMP-CARD-TOOL — Grid tool card
- Square-ish, min height 92 dp (grows with font scale), `rLg`, `Ink700`, padding `md`.
- Icon tile top-left, title `titleMedium`, subtitle `labelMedium` in `Slate300`.
- Entire card is one clickable with a merged semantic label `"<title>. <subtitle>"`.

### CMP-ROW — Settings / More Tools list row
- Height ≥ 64 dp, `rMd`, `Ink700`, padding horizontal `lg`.
- 20 dp leading icon in a 40 dp tile · title `titleMedium` · subtitle `bodyMedium`
  `Slate300` · trailing chevron 20 dp `Slate300`.
- Grouped rows separated by `sm` (8 dp) gaps rather than dividers.

### CMP-SEGMENTED — Segmented control (Narrow/Medium/Wide, Warm/Neutral/Cool, Fast/Medium/Slow)
- Container: `Ink600`, `rPill`, padding `xxs`, full width.
- Segment: equal weight, min height 40 dp, `rPill`.
- Selected: `Blue500` fill, `White` `labelMedium`. Unselected: transparent,
  `Slate300`.
- Semantics: `Role.RadioButton` per segment, `selectableGroup()` on the container.
- Selection moves with a 180 ms `fastOutSlowIn` offset animation; suppressed under
  reduce-motion.

### CMP-CHIP — Filter chip (All / Portable / Household / Decor)
- `rPill`, height 34 dp, horizontal padding `md`.
- Selected: `Blue500` fill, `White`. Unselected: `Ink600` fill, `Slate300`, 1 dp
  `Ink500` border.

### CMP-SLIDER — Brightness / value slider
- Active track `Blue500` 4 dp, inactive `Ink500` 4 dp, thumb 20 dp `White` with a
  2 dp `Blue500` ring.
- Always paired with a label row: leading icon + name on the left, live value
  (e.g. `70%`) right-aligned in `labelMedium` `Slate300`.
- Touch target height 48 dp regardless of visual track height.
- Semantics: `stateDescription = "<value> percent"`, and
  `progressBarRangeInfo` supplied for screen readers.

### CMP-POWER — Power toggle
- 48 dp circle. Off: `Ink600` fill, `Slate300` icon. On: `Blue500` fill, `White` icon,
  plus a 2 dp `Cyan400` ring at 50 % opacity.
- Unavailable: `Ink400` fill, `Slate300` icon, not actuable, `onClick = null`.
- Semantics: `Role.Switch`, `stateDescription` ∈ {On, Off, Unavailable}.

### CMP-COLORWHEEL — HSV colour wheel
- Circular hue ring with a saturation disc, diameter = `min(width − 2×xxl, 280.dp)`.
- Selector: 22 dp white ring, 3 dp stroke, drop-free.
- Drag anywhere inside the disc selects; the gesture claims the pointer so the
  parent scroll cannot steal it.
- **Accessibility:** a wheel is not screen-reader navigable. The wheel is marked
  `clearAndSetSemantics {}` and is always accompanied by the preset row
  (`CMP-SWATCH`) plus hue/saturation sliders in an *Advanced* expander, which are
  fully accessible. [Recommended]

### CMP-SWATCH — Preset colour row
- 36 dp circles, gap `md`, horizontally scrollable.
- Selected: 2 dp `White` ring + 4 dp outer `Ink900` gap.
- Each has `contentDescription` = the colour's localized name (not a hex code).

### CMP-BOTTOMNAV — Bottom navigation
- Height 64 dp + bottom system inset. Background `Ink800`, 1 dp top hairline.
- 4 destinations: Home, 3D, Tools, Settings.
- Active: `Blue500` icon + `labelSmall` in `White`. Inactive: `Slate300` both.
- Active indicator: a 3 dp `Blue500` pill, 24 dp wide, above the icon.

### CMP-DIALOG — Alert dialog
- `Ink600`, `rLg`, padding `xxl`, 1 dp `Ink500`.
- Optional 40 dp icon tile (accent-tinted) above the title.
- Title `titleMedium`, body `bodyLarge` `Slate300`.
- Buttons: confirm = filled `Blue500` pill; dismiss = text button `Slate200`.
- Destructive confirm uses `Red500`.

### CMP-SHEET — Bottom sheet
- `Ink600`, top corners `rXl`, 4×32 dp `Ink500` drag handle, content padding `xl`.
- Used for: colour Advanced controls, quality-mode picker, object info.

### CMP-BANNER — Inline status banner
- Full width, `rMd`, padding `md`, 20 dp leading icon.
- Variants: `info` (`BlueGlow` bg, `Blue400` icon), `warning` (`Amber400` @ 14 %,
  `Amber400` icon), `error` (`Red500` @ 14 %, `Red500` icon).
- Carries a title `labelLarge` and optional body `bodyMedium`; optional trailing
  text action.
- Used for low battery, thermal throttling, camera conflict, screen-light explanation.

### CMP-BADGE — "Coming Soon" badge
- `rPill`, `Ink600` fill, 1 dp `Ink500`, horizontal padding `sm`, height 22 dp,
  `labelSmall` in `Slate300`, all-caps.

### CMP-LOADING — Loading indicator
- Indeterminate: 32 dp `CircularProgressIndicator`, `Blue500`, 3 dp stroke, centred,
  with a `bodyMedium` `Slate300` caption below ("Loading model…").
- Determinate (model load): a 4 dp linear bar, `Blue500` on `Ink500`.
- Skeletons are **not** used; this app has no list-of-remote-content screen.

### CMP-EMPTY / CMP-ERROR — Full-screen states
- Centred column: 56 dp icon in a 96 dp tinted circle, title `titleLarge`, body
  `bodyLarge` `Slate300` (max 2 sentences, states the cause), primary action pill.
- Error states must name the cause and offer exactly one primary recovery action.

### CMP-SNACKBAR
- `Ink600`, `rMd`, `bodyMedium` `White`, action in `Blue400`.
- Reserved for transient, non-blocking confirmations. **Errors use `CMP-BANNER` or
  `CMP-ERROR`, never a snackbar**, because a snackbar can be missed.

---

## 7. Motion

| Token | Duration | Easing | Use |
|---|---|---|---|
| `motionFast` | 120 ms | `LinearOutSlowIn` | Press feedback, ripple-free state change |
| `motionStd` | 180 ms | `FastOutSlowIn` | Segment selection, chip selection |
| `motionEmphasis` | 280 ms | `FastOutSlowIn` | Screen transition, sheet |
| `motionSlow` | 450 ms | `FastOutSlowIn` | Colour cross-fade on the light surface |

Navigation transitions: horizontal slide + fade, `motionEmphasis`.

**Reduce motion** (`NFR-A11Y-04`): when `Settings.Global.ANIMATOR_DURATION_SCALE == 0`
or the in-app *Reduce motion* toggle is on, every duration above collapses to 0 ms,
the candle flicker becomes a steady colour, and the 3D idle auto-rotate (if enabled)
stops. Provide this through a `LocalReduceMotion` composition local so no composable
reads system settings directly.

---

## 8. Responsive layout

Breakpoints on `WindowSizeClass` width:

| Class | Width | Home grid | Simulator gallery | Detail screens |
|---|---|---|---|---|
| Compact | < 600 dp | 2 columns | 2 columns | single column |
| Medium | 600–839 dp | 3 columns | 3 columns | single column, content max-width 560 dp, centred |
| Expanded | ≥ 840 dp | 4 columns | 4 columns | two-pane: gallery + viewer side by side |

Additional rules:
- At **effective** width < 360 dp (small device **or** large font scale), the Home
  tool grid collapses to 1 column. Compute from
  `LocalConfiguration.screenWidthDp / fontScale` rather than raw width.
- Landscape on Compact: the simulator viewport takes the left 60 %, controls scroll
  on the right 40 %.
- Content never exceeds 640 dp of text measure.

### Insets and edge-to-edge
- `enableEdgeToEdge()` in the Activity; transparent system bars; light-content icons.
- Every screen consumes insets explicitly — no global `Modifier.systemBarsPadding()`
  on a root container.
- Screen Light and the full-screen 3D viewport draw **behind** the status and
  navigation bars; their controls carry `navigationBarsPadding()` +
  `statusBarsPadding()`.
- `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS` on the Screen Light window only.
- Gesture-navigation: the 3D viewport and colour wheel apply
  `Modifier.systemGestureExclusion()` on their left/right 24 dp edges so horizontal
  drags are not stolen by back-gesture. [Recommended]

---

## 9. Accessibility rules

1. Minimum touch target 48 × 48 dp; enforced via `Modifier.minimumInteractiveComponentSize()`
   or explicit sizing. Visual size may be smaller.
2. Every card merges its children's semantics into one node with a label of the form
   `"<title>. <subtitle>. <state>"`.
3. State is announced through `stateDescription`, never through colour alone. The
   flashlight card's `On` state is both a filled control *and* the word "On".
4. Headings use `semantics { heading() }` so screen readers can jump between sections.
5. Focus order follows visual order; `focusGroup()` wraps segmented controls.
6. The colour wheel is excluded from the reader; accessible equivalents are always
   present (see `CMP-COLORWHEEL`).
7. Live-updating values (brightness %) use `liveRegion = LiveRegionMode.Polite`.
8. No information is conveyed by colour alone — the Emergency output selector pairs
   its colour with an icon and a text label.
9. The app must be fully operable with TalkBack and with a hardware keyboard
   (`Tab`/`Enter`).

---

## 10. Content and tone

- Sentence case for all labels and titles. All-caps only in `CMP-BADGE`.
- Error messages: state the cause, then the remedy. Max two sentences.
  - Good: *"Camera is in use by another app. Close it and try again."*
  - Bad: *"Something went wrong."*
- Never use "Error:" as a prefix, and never show an exception class name to the user.
- The word "simulated" appears on the 3D simulator; the app never claims physical
  accuracy.
- Emergency copy never implies rescue. Required string:
  *"This is a signalling aid. It does not contact emergency services and does not
  guarantee you will be seen. Call your local emergency number for help."*

---

## 11. Theme implementation contract

```
ui/theme/
  Color.kt       // raw tokens only, internal
  Theme.kt       // AppTheme { } → MaterialTheme(colorScheme, typography, shapes)
  Type.kt        // Typography built from the table in §3
  Shape.kt       // Shapes(small=rSm, medium=rMd, large=rLg)
  Spacing.kt     // @Immutable data class + LocalSpacing
  Radius.kt      // @Immutable data class + LocalRadius
  Motion.kt      // durations + LocalReduceMotion
  Elevation.kt   // surface/border pairs per §4
```

- `AppTheme` sets `LocalSpacing`, `LocalRadius`, `LocalMotion`, `LocalReduceMotion`.
- Composables read `MaterialTheme.colorScheme.*` and `LocalSpacing.current.lg`.
- A unit test (`T-DS-01`) asserts the `darkColorScheme` mapping matches §2.2 exactly,
  so a token rename cannot silently change the UI.
- A static check (`T-DS-02`) fails the build on `Color(0x…)`, `\d+\.dp` or `\d+\.sp`
  literals inside `feature/**` source, with an allowlist for `0.dp` and sizes inside
  `ui/theme`.

---

## 12. Mapping the concept board to this system

| Board element | Implemented as | Deviation |
|---|---|---|
| Splash with glowing bulb | `SCR-SPLASH` using the platform SplashScreen API + an immediate branded frame | No timed hold (FR-ONB-01) |
| Onboarding hero photos | Vector/`WebP` illustrations, text as real `Text` | **Text is never baked into the image** (FR-GLOBAL-03) |
| Home hero flashlight card | `CMP-CARD-HERO` | — |
| 2×2 tool grid | `CMP-CARD-TOOL` | — |
| 3D Simulator wide row | `CMP-ROW` with 28 dp icon | — |
| Bottom nav (4 tabs) | `CMP-BOTTOMNAV` | — |
| Flashlight "Brightness 70%" | `CMP-SLIDER`, **only** when the device reports flash strength levels | Hidden on most devices (FR-TORCH-03) |
| Flashlight "Beam Mode" | **Removed** | No Android API controls torch beam angle (FR-TORCH-05) |
| Screen Light colour wheel | `CMP-COLORWHEEL` + `CMP-SWATCH` | Accessible fallback added |
| Emergency SOS/Strobe/Blink | Pattern selector + **new** Output selector | Output mechanism must be explicit (FR-EMER-02) |
| Emergency "Fast" | Clamped to 5 Hz behind a one-time warning | Photosensitivity (FR-EMER-05) |
| Settings → Audio | **Removed** | No audio feature exists (FR-SET-01) |
| Settings → Permissions (Camera, Storage, Location) | Replaced by an explanatory "no runtime permissions" screen | None of those are requested (FR-SET-04) |
| Camera permission dialog | **Removed** | `setTorchMode` needs no permission (FR-TORCH-08) |
| Low Battery blocking dialog | Downgraded to `CMP-BANNER` | Never block a flashlight (`ADR-007`) |
| Unsupported Device dialog | `CMP-ERROR` inline state, not a modal | Inline is recoverable and non-interrupting |
| Go Pro ₹299 | **Not in v1.0** | `FR-MON-01`, `OQ-01` |
| 3D Asset Store ₹99/₹149 | **Not in v1.0** | `FR-MON-01`, `OQ-01` |
| 3D grid: 6 objects | 5 objects in v1.0 (Screen Light tile removed) | Asset budget, `ADR-012` |
| Smart Lights screen | Informational only, `CMP-BADGE` "Coming Soon" | `FR-SMART-02` |
