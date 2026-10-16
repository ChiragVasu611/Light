package com.allinonelighting.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.feature.settings.SettingsScreen
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T-A11Y-01 / T-A11Y-03 — touch-target size and labelling (NFR-A11Y-01, NFR-A11Y-03).
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val compose = createComposeRule()

    private val minimumTouchTargetDp = 48

    @Test
    fun every_clickable_node_meets_the_minimum_touch_target() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                Column {
                    SettingsScreen(
                        onOpenDisplay = {},
                        onOpenPermissions = {},
                        onOpenAbout = {},
                        onReplayIntro = {},
                    )
                }
            }
        }

        val density = compose.density
        val clickables = compose.onRoot()
            .fetchSemanticsNode()
            .let { collectClickable(it) }

        assertTrue("expected clickable nodes to assert on", clickables.isNotEmpty())

        val tooSmall = clickables.mapNotNull { node ->
            val w = with(density) { node.size.width.toDp().value }
            val h = with(density) { node.size.height.toDp().value }
            val label = node.config.getOrNull(SemanticsProperties.ContentDescription)
                ?.firstOrNull()
                ?: node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.toString()
                ?: "<unlabelled>"
            if (w + 0.5f < minimumTouchTargetDp || h + 0.5f < minimumTouchTargetDp) {
                "$label is ${w.toInt()}x${h.toInt()}dp"
            } else {
                null
            }
        }

        assertTrue(
            "Clickable targets below ${minimumTouchTargetDp}dp (NFR-A11Y-01):\n" +
                tooSmall.joinToString("\n"),
            tooSmall.isEmpty(),
        )
    }

    @Test
    fun every_clickable_node_has_an_accessible_label() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                SettingsScreen(
                    onOpenDisplay = {},
                    onOpenPermissions = {},
                    onOpenAbout = {},
                    onReplayIntro = {},
                )
            }
        }

        val unlabelled = collectClickable(compose.onRoot().fetchSemanticsNode())
            .filter { node ->
                node.config.getOrNull(SemanticsProperties.ContentDescription).isNullOrEmpty() &&
                    node.config.getOrNull(SemanticsProperties.Text).isNullOrEmpty()
            }

        assertTrue(
            "Clickable nodes with no label (NFR-A11Y-03): ${unlabelled.size}",
            unlabelled.isEmpty(),
        )
    }

    private fun collectClickable(
        node: androidx.compose.ui.semantics.SemanticsNode,
    ): List<androidx.compose.ui.semantics.SemanticsNode> {
        val self = if (node.config.contains(SemanticsActions.OnClick)) listOf(node) else emptyList()
        return self + node.children.flatMap { collectClickable(it) }
    }
}
