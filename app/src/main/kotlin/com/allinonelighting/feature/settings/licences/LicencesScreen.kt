package com.allinonelighting.feature.settings.licences

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allinonelighting.R
import com.allinonelighting.data.licences.Licence
import com.allinonelighting.ui.components.AppBanner
import com.allinonelighting.ui.components.BannerSeverity
import com.allinonelighting.ui.components.LoadingState
import com.allinonelighting.ui.components.ScreenContent
import com.allinonelighting.ui.theme.LocalRadius
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing
import com.allinonelighting.ui.theme.LocalSurfaces

/**
 * SCR-LICENCES — every third-party component that ships, and the full text of each
 * licence they use (FR-SET-06, AC-SET-04). Everything is bundled; nothing is fetched.
 */
@Composable
fun LicencesScreen(
    modifier: Modifier = Modifier,
    viewModel: LicencesViewModel = viewModel(factory = LicencesViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LicencesContent(state = state, modifier = modifier)
}

/** Stateless body, so UI tests can drive it without a ViewModel. */
@Composable
fun LicencesContent(
    state: LicencesUiState,
    modifier: Modifier = Modifier,
) {
    ScreenContent(modifier = modifier) {
        Text(
            text = stringResource(R.string.licences_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.licences_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SectionHeading(stringResource(R.string.licences_components_heading))
        state.components.forEach { component ->
            InfoCard {
                Text(
                    text = stringResource(component.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(component.licence.nameRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = stringResource(component.detailRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionHeading(stringResource(R.string.licences_texts_heading))
        state.licenceTexts.forEach { (licence, textState) ->
            LicenceTextCard(licence, textState)
        }
    }
}

@Composable
private fun LicenceTextCard(licence: Licence, textState: LicenceTextState) {
    // UI-only state: whether the long text is expanded. Survives rotation.
    var expanded by rememberSaveable(licence) { mutableStateOf(false) }
    val sizes = LocalSizes.current

    InfoCard {
        Text(
            text = stringResource(licence.nameRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(licence.urlRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = { expanded = !expanded },
            // TextButton's own height is 40 dp; NFR-A11Y-01 needs 48.
            modifier = Modifier.heightIn(min = sizes.minTouchTarget),
        ) {
            Text(
                stringResource(
                    if (expanded) R.string.licences_hide_text else R.string.licences_show_text,
                ),
            )
        }
        if (expanded) {
            when (textState) {
                LicenceTextState.Loading ->
                    LoadingState(caption = stringResource(R.string.licences_text_loading))

                is LicenceTextState.Loaded -> Text(
                    text = textState.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                is LicenceTextState.Unavailable -> AppBanner(
                    title = stringResource(textState.messageRes),
                    severity = BannerSeverity.Error,
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val surfaces = LocalSurfaces.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = surfaces.card.background,
        border = BorderStroke(surfaces.card.borderWidth, surfaces.card.border),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            content = content,
        )
    }
}
