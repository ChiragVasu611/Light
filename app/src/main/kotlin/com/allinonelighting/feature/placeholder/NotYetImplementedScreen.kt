package com.allinonelighting.feature.placeholder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allinonelighting.R
import com.allinonelighting.ui.components.FullScreenState

/**
 * Honest placeholder for destinations whose feature work lands in Phase 2 or 3.
 *
 * This is NOT a no-op control (FR-GLOBAL-01): the route exists so navigation can be
 * built and tested now, and the screen states plainly that the feature is not yet
 * implemented rather than showing dead controls.
 *
 * @param detail optional second line naming the route argument that was parsed. It
 *   exists so navigation argument plumbing is observable — and testable — before the
 *   real screen that will consume it is written.
 */
@Composable
fun NotYetImplementedScreen(
    title: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    val body = stringResource(R.string.coming_in_phase_body)
    FullScreenState(
        icon = Icons.Filled.Construction,
        title = title,
        body = if (detail != null) "$body\n\n$detail" else body,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
