package com.allinonelighting.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.allinonelighting.ui.theme.LocalRadius
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing
import com.allinonelighting.ui.theme.LocalSurfaces

/**
 * CMP-BADGE — "Coming soon" marker.
 *
 * The badge is announced by default: AC-SMART-02 requires the "Coming soon" state to
 * reach a screen reader, and state must never be colour-only.
 *
 * @param mergedIntoParent set true only when an ancestor already folds this text into
 *   its own accessibility label (as [AppRow] does), where announcing it again is noise.
 */
@Composable
fun ComingSoonBadge(
    text: String,
    modifier: Modifier = Modifier,
    mergedIntoParent: Boolean = false,
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current
    val surfaces = LocalSurfaces.current

    Surface(
        modifier = modifier
            .heightIn(min = sizes.badgeHeight)
            .then(if (mergedIntoParent) Modifier.clearAndSetSemantics { } else Modifier),
        shape = RoundedCornerShape(radius.pill),
        color = surfaces.dialog.background,
        border = BorderStroke(surfaces.card.borderWidth, surfaces.card.border),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xxs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
