package com.allinonelighting.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allinonelighting.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Responsive width cap — docs/UI_DESIGN_SYSTEM.md §8.
 *
 * Found on a Samsung SM-X110 (621 dp, Medium width class): list rows stretched edge
 * to edge, leaving a long dead run between the subtitle and the trailing chevron.
 * The documented rule — cap single-column content and centre it — had never been
 * implemented. This test keeps it implemented.
 */
@RunWith(AndroidJUnit4::class)
class ScreenContentTest {

    @get:Rule
    val compose = createComposeRule()

    private val capDp = 560

    @Test
    fun content_never_exceeds_the_documented_width_cap() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                ScreenContent {
                    Text("row", modifier = Modifier.fillMaxWidth().testTag("row"))
                }
            }
        }

        val node = compose.onNodeWithTag("row").fetchSemanticsNode()
        val widthDp = with(compose.density) { node.size.width.toDp().value }

        // The cap applies minus the container's horizontal padding, so the content
        // is always at or below it — never the full window width on a tablet.
        assertTrue(
            "content was ${widthDp.toInt()}dp, expected <= ${capDp}dp",
            widthDp <= capDp + 0.5f,
        )
    }

    @Test
    fun content_is_centred_when_the_window_is_wider_than_the_cap() {
        compose.setContent {
            AppTheme(reduceMotion = true) {
                ScreenContent {
                    Text("row", modifier = Modifier.fillMaxWidth().testTag("row"))
                }
            }
        }

        val root = compose.onRoot().fetchSemanticsNode()
        val node = compose.onNodeWithTag("row").fetchSemanticsNode()

        val rootWidth = root.size.width
        val leftGap = node.positionInRoot.x
        val rightGap = rootWidth - (node.positionInRoot.x + node.size.width)

        if (rootWidth > with(compose.density) { capDp.dp.toPx() }) {
            // Within a pixel, the gaps either side must match.
            assertTrue(
                "expected centred content, got left=$leftGap right=$rightGap",
                kotlin.math.abs(leftGap - rightGap) <= 1.5f,
            )
        }
        // On a Compact window the cap does not bind and there is nothing to assert.
    }
}
