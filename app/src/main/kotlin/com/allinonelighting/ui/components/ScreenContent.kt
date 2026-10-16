package com.allinonelighting.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing

/**
 * Standard scrolling body for a single-column screen.
 *
 * Caps content width and centres it on wider windows, per
 * docs/UI_DESIGN_SYSTEM.md §8. Without the cap, a list row on a 621 dp tablet
 * stretches edge to edge and leaves a long dead run between the subtitle and the
 * trailing chevron — observed on a Samsung SM-X110 before this existed.
 *
 * Compact windows are unaffected: they are narrower than the cap, so content fills
 * as before.
 */
@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = LocalSizes.current.maxContentWidth,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(LocalSpacing.current.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
