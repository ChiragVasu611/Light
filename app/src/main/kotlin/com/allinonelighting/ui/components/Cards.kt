package com.allinonelighting.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.allinonelighting.ui.theme.FeatureAccent
import com.allinonelighting.ui.theme.LocalRadius
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing
import com.allinonelighting.ui.theme.LocalSurfaces

/** Icon tile used across cards and rows — docs/UI_DESIGN_SYSTEM.md §5. */
@Composable
fun AccentIconTile(
    icon: ImageVector,
    accent: FeatureAccent,
    modifier: Modifier = Modifier,
    tileSize: Dp = LocalSizes.current.tile,
    iconSize: Dp = LocalSizes.current.iconRow,
) {
    val radius = LocalRadius.current
    Box(
        modifier = modifier
            .size(tileSize)
            .background(accent.container, RoundedCornerShape(radius.sm)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            // Decorative: the parent card merges semantics and carries the label.
            contentDescription = null,
            tint = accent.tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * CMP-CARD-HERO — the Home flashlight card.
 *
 * [statusLine] is both the visible supporting text and the node's stateDescription,
 * so TalkBack announces hardware state changes without re-focusing (FR-HOME-02).
 */
@Composable
fun HeroCard(
    title: String,
    statusLine: String,
    icon: ImageVector,
    accent: FeatureAccent,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current
    val surfaces = LocalSurfaces.current
    val style = when {
        !enabled -> surfaces.cardDisabled
        selected -> surfaces.cardSelected
        else -> surfaces.card
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = title
                stateDescription = statusLine
                if (!enabled) disabled()
            }
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(radius.xl),
        color = style.background,
        border = BorderStroke(style.borderWidth, style.border),
    ) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            AccentIconTile(
                icon = icon,
                accent = accent,
                tileSize = sizes.tileHero,
                iconSize = sizes.iconHero,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // The trailing control owns its own semantics; the card must not
            // re-announce them.
            Box(modifier = Modifier.clearAndSetSemantics { }) { trailing() }
        }
    }
}

/** CMP-CARD-TOOL — a grid tile. Height grows with font scale; never fixed. */
@Composable
fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: FeatureAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current
    val surfaces = LocalSurfaces.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = sizes.toolCardMinHeight)
            .semantics(mergeDescendants = true) { contentDescription = "$title. $subtitle" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(radius.lg),
        color = surfaces.card.background,
        border = BorderStroke(surfaces.card.borderWidth, surfaces.card.border),
    ) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            AccentIconTile(icon, accent)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
