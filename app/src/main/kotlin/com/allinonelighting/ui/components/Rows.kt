package com.allinonelighting.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.allinonelighting.ui.theme.FeatureAccent
import com.allinonelighting.ui.theme.LocalRadius
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing
import com.allinonelighting.ui.theme.LocalSurfaces

/**
 * CMP-ROW — settings and More Tools list row.
 *
 * [badge] renders a trailing "Coming soon" style marker; when present it is folded
 * into the row's accessibility label so the state is never colour-only (FR-SMART-02).
 */
@Composable
fun AppRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    accent: FeatureAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    showChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current
    val surfaces = LocalSurfaces.current

    val label = buildString {
        append(title)
        if (subtitle != null) append(". ").append(subtitle)
        if (badge != null) append(". ").append(badge)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = sizes.rowMinHeight)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(radius.md),
        color = surfaces.card.background,
        border = BorderStroke(surfaces.card.borderWidth, surfaces.card.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            AccentIconTile(icon, accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (badge != null) ComingSoonBadge(badge, mergedIntoParent = true)
            trailing?.invoke()
            if (showChevron && trailing == null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(sizes.iconRow),
                )
            }
        }
    }
}

/**
 * CMP-ROW, switch variant — a settings row whose whole surface toggles a value.
 *
 * The row, not the Switch, owns the interaction (`toggleable` with Role.Switch), so the
 * touch target is the full row and TalkBack announces one control with its state.
 * When [enabled] is false, [subtitle] must say why (FR-GLOBAL-01: disabled controls
 * carry a visible reason).
 */
@Composable
fun AppSwitchRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    accent: FeatureAccent,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current
    val surfaces = LocalSurfaces.current

    val label = if (subtitle != null) "$title. $subtitle" else title

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = sizes.rowMinHeight)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        shape = RoundedCornerShape(radius.md),
        color = surfaces.card.background,
        border = BorderStroke(surfaces.card.borderWidth, surfaces.card.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            AccentIconTile(icon, accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // onCheckedChange = null: the Switch is display-only; the row toggles.
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                // Explicit: M3's unchecked track defaults to surfaceContainerHighest,
                // which the dark-only scheme leaves at Material's baseline purple.
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}
