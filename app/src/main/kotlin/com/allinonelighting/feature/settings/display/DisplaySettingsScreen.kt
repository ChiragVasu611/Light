package com.allinonelighting.feature.settings.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allinonelighting.R
import com.allinonelighting.domain.simulator.QualityMode
import com.allinonelighting.domain.simulator.SimulatorEnvironment
import com.allinonelighting.ui.components.AppBanner
import com.allinonelighting.ui.components.AppSwitchRow
import com.allinonelighting.ui.components.BannerSeverity
import com.allinonelighting.ui.components.ScreenContent
import com.allinonelighting.ui.components.SegmentOption
import com.allinonelighting.ui.components.SegmentedControl
import com.allinonelighting.ui.theme.FeatureAccent
import com.allinonelighting.ui.theme.LocalSpacing

/** SCR-SETTINGS-DISPLAY (FR-SET-03). */
@Composable
fun DisplaySettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: DisplaySettingsViewModel = viewModel(factory = DisplaySettingsViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DisplaySettingsContent(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

/** Stateless body, so UI tests can drive it without a ViewModel. */
@Composable
fun DisplaySettingsContent(
    state: DisplaySettingsUiState,
    onEvent: (DisplaySettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_display_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )

        when (state) {
            // The store resolves in milliseconds. A spinner here would only flash, so
            // nothing is drawn until real values exist — never placeholder values.
            DisplaySettingsUiState.Loading -> Unit
            is DisplaySettingsUiState.Content -> DisplaySettingsBody(state, onEvent)
        }
    }
}

@Composable
private fun DisplaySettingsBody(
    state: DisplaySettingsUiState.Content,
    onEvent: (DisplaySettingsEvent) -> Unit,
) {
    AppBanner(
        title = stringResource(R.string.display_simulator_pending_title),
        body = stringResource(R.string.display_simulator_pending_body),
        severity = BannerSeverity.Info,
    )

    Group(
        title = stringResource(R.string.display_environment_heading),
        explanation = stringResource(R.string.display_environment_body),
    ) {
        SegmentedControl(
            options = listOf(
                SegmentOption(
                    SimulatorEnvironment.Dark,
                    stringResource(R.string.display_environment_dark),
                ),
                SegmentOption(
                    SimulatorEnvironment.Studio,
                    stringResource(R.string.display_environment_studio),
                ),
            ),
            selected = state.environment,
            onSelect = { onEvent(DisplaySettingsEvent.SelectEnvironment(it)) },
        )
    }

    Group(
        title = stringResource(R.string.display_quality_heading),
        explanation = stringResource(R.string.display_quality_body),
    ) {
        // Typed as nullable so "not chosen yet" selects no segment rather than
        // pretending a default was picked.
        SegmentedControl<QualityMode?>(
            options = listOf(
                SegmentOption(
                    QualityMode.Performance,
                    stringResource(R.string.display_quality_performance),
                ),
                SegmentOption(
                    QualityMode.Balanced,
                    stringResource(R.string.display_quality_balanced),
                ),
                SegmentOption(QualityMode.High, stringResource(R.string.display_quality_high)),
            ),
            selected = state.quality,
            onSelect = { quality ->
                if (quality != null) onEvent(DisplaySettingsEvent.SelectQuality(quality))
            },
        )
        if (state.quality == null) {
            Text(
                text = stringResource(R.string.display_quality_automatic),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AppBanner(
            title = stringResource(R.string.display_quality_thermal_title),
            body = stringResource(R.string.display_quality_thermal_body),
            severity = BannerSeverity.Info,
        )
    }

    Group(title = stringResource(R.string.display_motion_heading), explanation = null) {
        val forced = state.reduceMotion is ReduceMotionSetting.ForcedBySystem
        AppSwitchRow(
            title = stringResource(R.string.settings_reduce_motion),
            subtitle = stringResource(
                if (forced) {
                    R.string.display_reduce_motion_forced
                } else {
                    R.string.settings_reduce_motion_subtitle
                },
            ),
            icon = Icons.Filled.Animation,
            accent = FeatureAccent.Neutral,
            checked = state.reduceMotion.enabled,
            enabled = !forced,
            onCheckedChange = { onEvent(DisplaySettingsEvent.SetReduceMotion(it)) },
        )
    }
}

@Composable
private fun Group(
    title: String,
    explanation: String?,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        if (explanation != null) {
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}
