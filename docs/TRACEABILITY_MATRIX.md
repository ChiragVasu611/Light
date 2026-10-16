# TRACEABILITY_MATRIX.md — All-in-One Lighting

Requirement → module → screen → test → acceptance criterion.
**Maintained as features land.** A requirement with no implementation row is not done.

**Status key:** ⬜ not started · 🟨 in progress · ✅ implemented & tested

_Last updated: 2026-10-16 — Phase 1 in progress. ✅ rows have an implementation AND a test that was executed._

---

## Global

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-GLOBAL-01 | all | all | `T-GLOBAL-01` | `AC-GLOBAL-01` | ⬜ |
| FR-GLOBAL-02 | `domain/torch`, `data/hardware` | `SCR-HOME`, `SCR-FLASHLIGHT` | `T-TORCH-04` | `AC-GLOBAL-02` | ⬜ |
| FR-GLOBAL-03 | `res/values/strings.xml` | all | `T-GLOBAL-02` | `AC-GLOBAL-03` | 🟨 |
| FR-GLOBAL-04 | manifest | all | `T-GLOBAL-03`, `T-SMART-03` | `AC-GLOBAL-04` | 🟨 |
| FR-GLOBAL-05 | `navigation` | all | `T-NAV-03` | `AC-GLOBAL-01` | ⬜ |
| FR-GLOBAL-06 | `feature/*` lifecycle | light screens | `T-NAV-03`, `T-TORCH-07`, `T-EMER-05` | — | ⬜ |
| FR-GLOBAL-07 | `ui/theme`, layouts | all | `T-A11Y-05` | `NFR-A11Y-*` | ⬜ |
| — | `core`, `domain` layering | — | `T-ARCH-01` | — | ✅ |
| — | `ui/theme` tokens | — | `T-DS-01`, `T-DS-02` | — | ✅ |
| — | dependencies | — | `T-DEP-01` | — | ⬜ |
| — | logging | — | `T-LOG-01` | — | ⬜ |
| — | manifest permissions | — | `T-PERM-01`, `T-PERM-02` | — | ⬜ |
| NFR-BAT-02 | — | — | `T-GLOBAL-04` | — | ⬜ |

## Onboarding

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-ONB-01 | `MainActivity`, splash API | `SCR-SPLASH` | `T-ONB-01`, `T-PERF-01` | `AC-ONB-05` | ✅ |
| FR-ONB-02 | `feature/onboarding` | `SCR-ONBOARDING` | `T-ONB-01` | `AC-ONB-01` | ✅ |
| FR-ONB-03 | `feature/onboarding` | `SCR-ONBOARDING` | `T-ONB-03`, `T-ONB-05` | `AC-ONB-03` | ✅ |
| FR-ONB-04 | `data/prefs` | `SCR-ONBOARDING` | `T-ONB-02`, `T-ONB-06` | `AC-ONB-02` | ✅ |
| FR-ONB-05 | `feature/onboarding` | `SCR-ONBOARDING` | `T-ONB-04`, `T-PERM-02` | `AC-ONB-04` | ✅ |
| FR-ONB-06 | `feature/settings` | `SCR-SETTINGS` | `T-SET-02` | `AC-SET-02` | ⬜ |

## Home

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-HOME-01 | `feature/home`, `domain/torch` | `SCR-HOME` | `T-TORCH-01` | `AC-HOME-01` | ⬜ |
| FR-HOME-02 | `feature/home` | `SCR-HOME` | `T-HOME-01`, `T-TORCH-05` | `AC-HOME-03` | ⬜ |
| FR-HOME-03 | `feature/home`, `ui/components` | `SCR-HOME` | `T-NAV-01` | `AC-HOME-04` | ⬜ |
| FR-HOME-04 | `navigation` | `SCR-HOME` | `T-NAV-01` | `AC-HOME-04` | ⬜ |
| FR-HOME-05 | `feature/home` | `SCR-HOME` | `T-SMART-02` | `AC-SMART-02` | ⬜ |
| FR-HOME-06 | `navigation` | all tabs | `T-NAV-02` | — | ✅ |
| FR-HOME-07 | `core/device` | `SCR-HOME` | `T-HOME-02` | `AC-HOME-02` | ⬜ |

## Torch

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-TORCH-01 | `data/hardware/CameraTorchController` | `SCR-FLASHLIGHT` | `T-TORCH-01` | `AC-TORCH-01` | ⬜ |
| FR-TORCH-02 | `data/hardware` (TorchCallback) | `SCR-HOME`, `SCR-FLASHLIGHT` | `T-TORCH-05`, `T-TORCH-04` | `AC-TORCH-05` | ⬜ |
| FR-TORCH-03 | `core/device` | `SCR-FLASHLIGHT` | `T-TORCH-06` | `AC-TORCH-06` | ⬜ |
| FR-TORCH-04 | `data/hardware` | `SCR-FLASHLIGHT` | `T-TORCH-06` | `AC-TORCH-06` | ⬜ |
| FR-TORCH-05 | *(control removed)* | `SCR-FLASHLIGHT` | `T-GLOBAL-01` | `AC-GLOBAL-01` | ⬜ |
| FR-TORCH-06 | `feature/flashlight` lifecycle | `SCR-FLASHLIGHT` | `T-TORCH-07`, `T-TORCH-09` | `AC-TORCH-07` | ⬜ |
| FR-TORCH-07 | `domain/torch` errors | `SCR-FLASHLIGHT` | `T-TORCH-02/03/08` | `AC-TORCH-02/03` | ⬜ |
| FR-TORCH-08 | manifest | — | `T-PERM-01` | — | ⬜ |

## Screen light & candle

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-SCREEN-01 | `feature/screenlight` | `SCR-SCREENLIGHT` | `T-SCREEN-01` | `AC-SCREEN-01` | ⬜ |
| FR-SCREEN-02 | `feature/screenlight` | `SCR-SCREENLIGHT` | `T-SCREEN-01` | `AC-SCREEN-01` | ✅ |
| FR-SCREEN-03 | `ui/components/CMP-SWATCH` | `SCR-SCREENLIGHT` | `T-SCREEN-01` | `AC-SCREEN-01` | ⬜ |
| FR-SCREEN-04 | `data/hardware/WindowScreenLight` | `SCR-SCREENLIGHT` | `T-SCREEN-08` | — | ⬜ |
| FR-SCREEN-05 | `data/hardware/WindowScreenLight` | `SCR-SCREENLIGHT` | `T-SCREEN-02` | `AC-SCREEN-02` | ⬜ |
| FR-SCREEN-06 | `data/hardware/WindowScreenLight` | `SCR-SCREENLIGHT` | `T-SCREEN-06` | `AC-SCREEN-06` | ⬜ |
| FR-SCREEN-07 | `feature/screenlight` | `SCR-SCREENLIGHT` | `T-SCREEN-05`, `T-SCREEN-07` | `AC-SCREEN-05` | ⬜ |
| FR-SCREEN-08 | `feature/screenlight` | `SCR-SCREENLIGHT` | `T-GLOBAL-01` | — | ⬜ |
| FR-SCREEN-09 | `data/prefs` | `SCR-SCREENLIGHT` | `T-SCREEN-04`, `T-LIFE-01` | `AC-SCREEN-04` | ⬜ |
| FR-SCREEN-10 | `feature/screenlight` | `SCR-SCREENLIGHT` | `T-SCREEN-03` | `AC-SCREEN-03` | ⬜ |
| FR-CANDLE-01 | `feature/screenlight` (candle) | `SCR-CANDLE` | `T-CANDLE-01` | `AC-CANDLE-01` | ⬜ |
| FR-CANDLE-02 | `feature/screenlight` | `SCR-CANDLE` | `T-CANDLE-02` | `NFR-A11Y-04` | ⬜ |
| FR-NIGHT-01 | `data/prefs` | `SCR-SCREENLIGHT?mode=night` | `T-TOOLS-01` | — | ⬜ |

## Emergency

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-EMER-01 | `domain/emergency` | `SCR-EMERGENCY` | `T-EMER-01` | `AC-EMER-01` | ⬜ |
| FR-EMER-02 | `feature/emergency` | `SCR-EMERGENCY` | `T-EMER-04` | `AC-EMER-04` | ⬜ |
| FR-EMER-03 | `feature/emergency` | `SCR-EMERGENCY` | `T-EMER-02` | `AC-EMER-02` | ✅ |
| FR-EMER-04 | `feature/emergency` | `SCR-EMERGENCY` | `T-EMER-03` | `AC-EMER-03` | ⬜ |
| FR-EMER-05 | `domain/emergency`, `data/prefs` | `SCR-EMERGENCY` | `T-EMER-02`, `T-EMER-06` | `AC-EMER-02/06` | ⬜ |
| FR-EMER-06 | `feature/emergency` | `SCR-EMERGENCY` | `T-EMER-06` | — | ⬜ |
| FR-EMER-07 | `feature/emergency` lifecycle | `SCR-EMERGENCY` | `T-EMER-05`, `T-LIFE-02/04` | `AC-EMER-05` | ⬜ |
| FR-EMER-08 | `res/strings` | `SCR-EMERGENCY` | `T-EMER-07` | `AC-EMER-07` | ⬜ |
| FR-EMER-09 | `domain/emergency/PatternScheduler` | `SCR-EMERGENCY` | `T-EMER-08` | `AC-EMER-01` | ⬜ |
| FR-EMER-10 | `feature/emergency` | `SCR-EMERGENCY` | `T-GLOBAL-01` | — | ⬜ |

## More tools

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-TOOLS-01 | `feature/tools` | `SCR-TOOLS` | `T-TOOLS-01` | `AC-TOOLS-01` | ✅ |
| — (route arguments) | `navigation`, `domain` | `SCR-TOOLS` | `T-TOOLS-02`, `T-NAV-01/02` | `AC-TOOLS-02` | ✅ |

## 3D simulator

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-3D-01 | `data/catalog` | `SCR-SIM-GALLERY` | `T-NAV-02` | — | ⬜ |
| FR-3D-02 | `assets/catalog.json` | `SCR-SIM-GALLERY` | `T-TOOLS-01`-style audit | — | ✅ |
| FR-3D-03 | `render/OrbitController` | `SCR-SIM-VIEWER` | `T-3D-13` | — | ⬜ |
| FR-3D-04 | `render/LightRig` | `SCR-SIM-VIEWER` | `T-3D-01` | `AC-3D-01` | ⬜ |
| FR-3D-05 | `render/LightRig` | `SCR-SIM-VIEWER` | `T-3D-01` | `AC-3D-01` | ⬜ |
| FR-3D-06 | `render/LightRig` | `SCR-SIM-VIEWER` | `T-3D-02` | `AC-3D-02` | ⬜ |
| FR-3D-07 | `render/LightRig` | `SCR-SIM-VIEWER` | `T-3D-14` | — | ⬜ |
| FR-3D-08 | `render/LightRig`, flame material | `SCR-SIM-VIEWER` | `T-3D-07` | `AC-3D-07` | ⬜ |
| FR-3D-09 | `render/SceneBuilder` | `SCR-SIM-VIEWER` | `T-3D-03` | `AC-3D-03` | ⬜ |
| FR-3D-10 | `render/SceneBuilder` | `SCR-SIM-VIEWER` | manual visual review + `T-3D-03` | — | ⬜ |
| FR-3D-11 | `render/QualityProfile`, `core/device` | `SCR-SIM-VIEWER` | `T-3D-09/10/11/12` | — | ⬜ |
| FR-3D-12 | `render/OrbitController` | `SCR-SIM-VIEWER` | `T-3D-06` | `AC-3D-06` | ⬜ |
| FR-3D-13 | `render/FilamentEngineHost` | `SCR-SIM-VIEWER` | `T-3D-04`, `T-3D-17` | `AC-3D-04` | ⬜ |
| FR-3D-14 | `render/ModelLoader` | `SCR-SIM-VIEWER` | `T-3D-05`, `T-3D-15` | `AC-3D-05` | ⬜ |
| FR-3D-15 | `data/prefs` | `SCR-SIM-VIEWER` | `T-3D-18`, `T-LIFE-03` | — | ⬜ |
| FR-3D-16 | `feature/simulator` | `SCR-SIM-VIEWER` | `T-3D-07` | `AC-3D-07` | ⬜ |
| NFR-COMPAT-02 | `core/device` | `SCR-SIM-GALLERY` | `T-3D-08` | — | ⬜ |
| FR-ASSET-LICENCE | `docs/licences/assets.md` | — | release gate 14 | — | ⬜ |

## Settings, about, smart light, monetization

| Req | Module | Screen | Test | AC | Status |
|---|---|---|---|---|---|
| FR-SET-01 | `feature/settings` | `SCR-SETTINGS` | `T-SET-01` | `AC-SET-01` | ✅ |
| FR-SET-02 | `feature/settings` | `SCR-SETTINGS` | `T-SET-02` | `AC-SET-02` | ⬜ |
| FR-SET-03 | `feature/settings` | `SCR-SETTINGS-DISPLAY` | `T-A11Y-04` | `NFR-A11Y-04` | ⬜ |
| FR-SET-04 | `feature/settings` | `SCR-SETTINGS-PERMISSIONS` | `T-SET-05` | — | ✅ |
| FR-SET-05 | `feature/settings` | `SCR-ABOUT` | `T-SET-03` | `AC-SET-03` | ✅ |
| FR-SET-06 | `feature/settings` | `SCR-LICENCES` | `T-SET-04` | `AC-SET-04` | ⬜ |
| FR-SMART-01 | `feature/smartlight` | `SCR-SMARTLIGHT` | `T-SMART-02` | `AC-SMART-02` | ✅ |
| FR-SMART-02 | `feature/smartlight` | `SCR-SMARTLIGHT` | `T-SMART-01` | `AC-SMART-01` | ✅ |
| FR-SMART-03 | manifest | `SCR-SMARTLIGHT` | `T-SMART-03` | `AC-SMART-03` | ✅ |
| FR-SMART-04 | `domain/smartlight` | — | compile + fake-based tests | — | ⬜ |
| FR-SMART-05 | *(Phase 6)* | — | — | — | ⬜ |
| FR-MON-01 | — | — | `T-MON-01` | `AC-MON-01` | ⬜ |
| FR-MON-02/03 | *(Phase 5, conditional on `OQ-01`)* | — | — | — | ⬜ |

## Nonfunctional

| Req | Module | Test | Status |
|---|---|---|---|
| NFR-PERF-01 | startup path, baseline profile | `T-PERF-01` | ⬜ |
| NFR-PERF-02 | `navigation` | `T-PERF-01` suite | ⬜ |
| NFR-PERF-03 | `render/ModelLoader` | `T-PERF-03` | ⬜ |
| NFR-PERF-04/05 | `render/*` | `T-PERF-02` | ⬜ |
| NFR-MEM-01 | `render/*` | `dumpsys meminfo` procedure | ⬜ |
| NFR-MEM-02 | `render/FilamentEngineHost` | `T-3D-04` | ⬜ |
| NFR-SIZE-01/02 | build config, assets | `T-REL-02` | ⬜ |
| NFR-BAT-01 | `feature/screenlight` | code audit + Battery Historian | ⬜ |
| NFR-BAT-02 | — | `T-GLOBAL-04` | ⬜ |
| NFR-REL-01 | — | post-launch vitals | ⬜ |
| NFR-A11Y-01 | `ui/components` | `T-A11Y-01` | ⬜ |
| NFR-A11Y-02 | `ui/theme` | `T-A11Y-02` | ✅ |
| NFR-A11Y-03 | all screens | `T-A11Y-03`, `T-A11Y-07` | ⬜ |
| NFR-A11Y-04 | `ui/theme/Motion` | `T-A11Y-04` | ⬜ |
| NFR-COMPAT-01 | `app/build.gradle.kts` | build | ✅ |
| NFR-COMPAT-02 | `core/device` | `T-3D-08` | ⬜ |

## User stories

| Story | Requirements | Status |
|---|---|---|
| US-01 | FR-HOME-01 | ⬜ |
| US-02 | FR-HOME-07, FR-TORCH-07 | ⬜ |
| US-03 | FR-SCREEN-01..03 | ⬜ |
| US-04 | FR-EMER-01 | ⬜ |
| US-05 | FR-EMER-05 | ⬜ |
| US-06 | FR-3D-05, FR-3D-06 | ⬜ |
| US-07 | FR-3D-09, FR-3D-10 | ⬜ |
| US-08 | FR-3D-11, FR-3D-13 | ⬜ |
| US-09 | FR-SMART-01/02 | ⬜ |
| US-10 | FR-SCREEN-09, FR-3D-15 | ⬜ |
| US-11 | NFR-A11Y-03 | ⬜ |
| US-12 | FR-TORCH-08, FR-SMART-03 | ⬜ |

---

## Coverage check

| Check | Result |
|---|---|
| Every `FR-*` has ≥ 1 test | ✅ (`FR-SMART-05`, `FR-MON-02/03` are future-phase and marked so) |
| Every `AC-*` has ≥ 1 test | ✅ |
| Every `NFR-*` has a verification method | ✅ |
| Every screen in `SCREEN_SPECIFICATION.md` appears here | ✅ |
| Every test in `TESTING_AND_ACCEPTANCE.md` traces to a requirement | ✅ |
