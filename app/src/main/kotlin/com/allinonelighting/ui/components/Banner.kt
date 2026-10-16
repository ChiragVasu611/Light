package com.allinonelighting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.allinonelighting.ui.theme.LocalRadius
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing
import com.allinonelighting.ui.theme.Palette

enum class BannerSeverity { Info, Warning, Error }

/**
 * CMP-BANNER — inline, persistent status.
 *
 * Errors use this (or a full-screen error state), never a snackbar: a snackbar
 * auto-dismisses and can be missed, and cannot hold a standing condition like
 * "camera in use" (ADR-020).
 */
@Composable
fun AppBanner(
    title: String,
    severity: BannerSeverity,
    modifier: Modifier = Modifier,
    body: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current

    val tint: Color = when (severity) {
        BannerSeverity.Info -> Palette.Blue400
        BannerSeverity.Warning -> Palette.Amber400
        BannerSeverity.Error -> Palette.Red500
    }
    val container: Color = when (severity) {
        BannerSeverity.Info -> Palette.BlueGlow
        else -> tint.copy(alpha = 0.14f)
    }
    val icon: ImageVector = when (severity) {
        BannerSeverity.Info -> Icons.Filled.Info
        BannerSeverity.Warning -> Icons.Filled.WarningAmber
        BannerSeverity.Error -> Icons.Filled.ErrorOutline
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(radius.md))
            .padding(spacing.md)
            // Announced without stealing focus when a hardware condition changes.
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(sizes.iconRow),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(text = actionLabel, color = tint)
            }
        }
    }
}
