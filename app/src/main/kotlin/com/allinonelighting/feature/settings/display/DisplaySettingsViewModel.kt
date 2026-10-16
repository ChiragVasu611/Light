package com.allinonelighting.feature.settings.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allinonelighting.core.device.SystemAnimationSettings
import com.allinonelighting.core.di.appContainer
import com.allinonelighting.core.log.AppLogger
import com.allinonelighting.data.prefs.PreferencesRepository
import com.allinonelighting.domain.simulator.QualityMode
import com.allinonelighting.domain.simulator.SimulatorEnvironment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "DisplaySettingsViewModel"

/**
 * SCR-SETTINGS-DISPLAY (FR-SET-03).
 *
 * State is derived from the preferences store only. A selection is not reflected
 * until the store emits it, so a failed write leaves the previous value showing
 * rather than a choice that was never saved.
 */
class DisplaySettingsViewModel(
    private val preferences: PreferencesRepository,
    private val systemAnimations: SystemAnimationSettings,
    private val logger: AppLogger,
) : ViewModel() {

    val state: StateFlow<DisplaySettingsUiState> = preferences.preferences
        .map { prefs ->
            DisplaySettingsUiState.Content(
                environment = SimulatorEnvironment.fromKey(prefs.simEnvironment),
                quality = QualityMode.fromKey(prefs.simQuality),
                reduceMotion = if (systemAnimations.animationsDisabled()) {
                    ReduceMotionSetting.ForcedBySystem
                } else {
                    ReduceMotionSetting.UserChoice(enabled = prefs.reduceMotion == true)
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DisplaySettingsUiState.Loading,
        )

    fun onEvent(event: DisplaySettingsEvent) {
        when (event) {
            is DisplaySettingsEvent.SelectEnvironment ->
                persist("environment") { preferences.setSimEnvironment(event.environment.key) }

            is DisplaySettingsEvent.SelectQuality ->
                persist("quality") { preferences.setSimQuality(event.quality.key) }

            is DisplaySettingsEvent.SetReduceMotion -> {
                // The switch is disabled in this case; this guards a stale event
                // racing a system-setting change.
                if (systemAnimations.animationsDisabled()) return
                persist("reduceMotion") { preferences.setReduceMotion(event.enabled) }
            }
        }
    }

    private fun persist(what: String, write: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { write() }
                .onFailure { logger.warn(TAG, "Could not save $what", it) }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer
                DisplaySettingsViewModel(
                    preferences = container.preferences,
                    systemAnimations = container.systemAnimations,
                    logger = container.logger,
                )
            }
        }
    }
}
