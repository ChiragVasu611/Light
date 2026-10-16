package com.allinonelighting.feature.tools

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.allinonelighting.R
import com.allinonelighting.domain.emergency.EmergencyPattern
import com.allinonelighting.domain.screenlight.ScreenLightMode
import com.allinonelighting.ui.components.AppRow
import com.allinonelighting.ui.theme.FeatureAccent
import com.allinonelighting.ui.components.ScreenContent
import com.allinonelighting.ui.theme.LocalSpacing

/**
 * One row in the More Tools list. Each carries the destination it opens, so the
 * list and the navigation stay in step.
 */
sealed interface ToolDestination {
    data class ScreenLight(val mode: ScreenLightMode) : ToolDestination
    data class Emergency(val pattern: EmergencyPattern) : ToolDestination
    data object Candle : ToolDestination
}

data class ToolEntry(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val accent: FeatureAccent,
    val destination: ToolDestination,
)

/**
 * The tools shipping in v1.0 (FR-TOOLS-01).
 *
 * Party Mode and Music Light are deliberately absent rather than shown disabled: a
 * disabled row invites repeated tapping and implies a near-term arrival we have not
 * committed to (ADR-011). They appear only when implemented.
 */
val LaunchTools = listOf(
    ToolEntry(
        R.string.tool_color_light, R.string.tool_color_light_subtitle,
        Icons.Filled.ColorLens, FeatureAccent.ScreenLight,
        ToolDestination.ScreenLight(ScreenLightMode.Color),
    ),
    ToolEntry(
        R.string.tool_night_light, R.string.tool_night_light_subtitle,
        Icons.Filled.Bedtime, FeatureAccent.Warm,
        ToolDestination.ScreenLight(ScreenLightMode.Night),
    ),
    ToolEntry(
        R.string.tool_sos_light, R.string.tool_sos_light_subtitle,
        Icons.Filled.Sos, FeatureAccent.Emergency,
        ToolDestination.Emergency(EmergencyPattern.Sos),
    ),
    ToolEntry(
        R.string.tool_strobe_light, R.string.tool_strobe_light_subtitle,
        Icons.Filled.Bolt, FeatureAccent.Emergency,
        ToolDestination.Emergency(EmergencyPattern.Strobe),
    ),
    ToolEntry(
        R.string.tool_candle_light, R.string.tool_candle_light_subtitle,
        Icons.Filled.LocalFireDepartment, FeatureAccent.Warm,
        ToolDestination.Candle,
    ),
)

/** SCR-TOOLS — docs/SCREEN_SPECIFICATION.md. */
@Composable
fun ToolsScreen(
    onOpen: (ToolDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    ScreenContent(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_more_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(bottom = spacing.xs)
                .semantics { heading() },
        )
        LaunchTools.forEach { tool ->
            AppRow(
                title = stringResource(tool.titleRes),
                subtitle = stringResource(tool.subtitleRes),
                icon = tool.icon,
                accent = tool.accent,
                onClick = { onOpen(tool.destination) },
            )
        }
    }
}
