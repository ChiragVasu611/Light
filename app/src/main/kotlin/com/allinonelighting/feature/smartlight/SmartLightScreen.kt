package com.allinonelighting.feature.smartlight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.allinonelighting.R
import com.allinonelighting.ui.components.AppBanner
import com.allinonelighting.ui.components.BannerSeverity
import com.allinonelighting.ui.components.ComingSoonBadge
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.components.ScreenContent
import com.allinonelighting.ui.theme.LocalSpacing

/**
 * SCR-SMARTLIGHT — informational only.
 *
 * Hard constraint (FR-SMART-02, asserted by T-SMART-01): this screen contains no
 * Switch, no Slider, no device list and no scan action — not even disabled ones.
 * It makes no network call and requests no permission.
 */
@Composable
fun SmartLightScreen(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val sizes = LocalSizes.current

    val planned = listOf(
        R.string.smartlight_planned_1,
        R.string.smartlight_planned_2,
        R.string.smartlight_planned_3,
        R.string.smartlight_planned_4,
    )

    ScreenContent(
        modifier = modifier,
    ) {
        ComingSoonBadge(stringResource(R.string.badge_coming_soon))

        Text(
            text = stringResource(R.string.smartlight_heading),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.smartlight_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            planned.forEach { res ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(sizes.iconRow),
                    )
                    Text(
                        text = stringResource(res),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        AppBanner(
            title = stringResource(R.string.smartlight_learn_more),
            severity = BannerSeverity.Info,
            body = stringResource(R.string.smartlight_no_control_note),
        )
    }
}
