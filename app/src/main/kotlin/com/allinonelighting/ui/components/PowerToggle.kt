package com.allinonelighting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.Palette

/** What the power control is allowed to show. There is no "pending on" state. */
enum class PowerState { Off, On, Unavailable, Checking }

/**
 * CMP-POWER — docs/UI_DESIGN_SYSTEM.md §6.
 *
 * The control renders [state] and nothing else. It never optimistically shows On
 * after a tap; the owning state holder flips to On only when the OS confirms it
 * (FR-GLOBAL-02, ADR-004).
 */
@Composable
fun PowerToggle(
    state: PowerState,
    contentDescriptionText: String,
    stateDescriptionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = LocalSizes.current.powerButton,
) {
    val sizes = LocalSizes.current
    val actionable = state == PowerState.Off || state == PowerState.On

    val background = when (state) {
        PowerState.On -> MaterialTheme.colorScheme.primary
        PowerState.Unavailable, PowerState.Checking -> Palette.Ink400
        PowerState.Off -> MaterialTheme.colorScheme.surfaceVariant
    }
    val tint = when (state) {
        PowerState.On -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(size)
            .background(background, CircleShape)
            .then(
                // Cyan ring marks the active state in addition to the fill, so state
                // is never conveyed by colour alone.
                if (state == PowerState.On) {
                    Modifier.border(sizes.hairline * 2, Palette.Cyan400.copy(alpha = 0.5f), CircleShape)
                } else {
                    Modifier
                },
            )
            .semantics {
                contentDescription = contentDescriptionText
                stateDescription = stateDescriptionText
                role = Role.Switch
                if (!actionable) disabled()
            }
            .then(if (actionable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.PowerSettingsNew,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size / 2),
        )
    }
}
