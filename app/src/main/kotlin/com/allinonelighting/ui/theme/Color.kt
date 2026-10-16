package com.allinonelighting.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw colour tokens. The single source of truth is docs/UI_DESIGN_SYSTEM.md §2.1.
 *
 * Composables must NOT reference these directly — read them through
 * [androidx.compose.material3.MaterialTheme.colorScheme] or [FeatureAccents].
 * Enforced by T-DS-02.
 */
internal object Palette {
    val Ink900 = Color(0xFF05070F)
    val Ink800 = Color(0xFF0A0F1D)
    val Ink700 = Color(0xFF111828)
    val Ink600 = Color(0xFF182036)
    val Ink500 = Color(0xFF222C47)
    val Ink400 = Color(0xFF38435F)

    val Slate300 = Color(0xFF8A97B4)
    val Slate200 = Color(0xFFB6C0D6)
    val White = Color(0xFFFFFFFF)

    val Blue500 = Color(0xFF1F6FE8)
    val Blue400 = Color(0xFF5B9BFF)
    val BlueGlow = Color(0xFF1B3A6B)

    val Cyan400 = Color(0xFF4BD8FF)
    val Amber400 = Color(0xFFFFB648)
    val Red500 = Color(0xFFE5484D)
    val Green500 = Color(0xFF3DD68C)
}

/**
 * Per-feature accent colours (docs/UI_DESIGN_SYSTEM.md §2.3).
 *
 * Used for icon tiles only — never for text or large fills.
 */
enum class FeatureAccent(val tint: Color, val containerAlpha: Float) {
    Flashlight(Palette.Blue500, 1f),
    ScreenLight(Palette.Cyan400, 0.14f),
    Emergency(Palette.Red500, 0.14f),
    Warm(Palette.Amber400, 0.14f),
    Neutral(Palette.Slate200, 1f),
    Simulator(Palette.Cyan400, 0.14f),
    ComingSoon(Palette.Slate300, 1f),
    ;

    /** Background for the 40.dp icon tile that carries this accent. */
    val container: Color
        get() = when (this) {
            Flashlight -> Palette.BlueGlow
            Neutral, ComingSoon -> Palette.Ink600
            else -> tint.copy(alpha = containerAlpha)
        }
}
