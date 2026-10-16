package com.allinonelighting.ui.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Semantic colour mapping — docs/UI_DESIGN_SYSTEM.md §2.2.
 * Asserted field-by-field by T-DS-01 so a token rename cannot silently change the UI.
 *
 * The app is dark-only in v1.0 (ADR-010); there is no light scheme.
 */
val AppColorScheme = darkColorScheme(
    primary = Palette.Blue500,
    onPrimary = Palette.White,
    primaryContainer = Palette.BlueGlow,
    onPrimaryContainer = Palette.White,
    secondary = Palette.Cyan400,
    onSecondary = Palette.Ink900,
    background = Palette.Ink900,
    onBackground = Palette.White,
    surface = Palette.Ink700,
    onSurface = Palette.White,
    surfaceVariant = Palette.Ink600,
    onSurfaceVariant = Palette.Slate300,
    surfaceContainerHigh = Palette.Ink600,
    outline = Palette.Ink500,
    outlineVariant = Palette.Ink400,
    error = Palette.Red500,
    onError = Palette.White,
    scrim = Palette.Ink800,
)

/**
 * @param reduceMotion the effective reduce-motion flag. The app passes
 *   MainViewModel's value, which combines the system animator scale with the in-app
 *   setting. Tests pass a fixed value; null reads only the device setting.
 */
@Composable
fun AppTheme(
    reduceMotion: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemReduceMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    val effectiveReduceMotion = reduceMotion ?: systemReduceMotion

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalRadius provides Radius(),
        LocalMotion provides if (effectiveReduceMotion) Motion.None else Motion(),
        LocalReduceMotion provides effectiveReduceMotion,
        LocalSurfaces provides Surfaces(),
        LocalSizes provides Sizes(),
    ) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
