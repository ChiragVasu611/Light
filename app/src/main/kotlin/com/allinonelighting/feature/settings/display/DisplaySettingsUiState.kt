package com.allinonelighting.feature.settings.display

import com.allinonelighting.domain.simulator.QualityMode
import com.allinonelighting.domain.simulator.SimulatorEnvironment

/** SCR-SETTINGS-DISPLAY state. Every value shown comes from the store, never a guess. */
sealed interface DisplaySettingsUiState {

    /** Before the first preferences read resolves — a few milliseconds in practice. */
    data object Loading : DisplaySettingsUiState

    data class Content(
        val environment: SimulatorEnvironment,
        /** Null until the user picks one; the simulator then chooses by device class. */
        val quality: QualityMode?,
        val reduceMotion: ReduceMotionSetting,
    ) : DisplaySettingsUiState
}

sealed interface ReduceMotionSetting {
    val enabled: Boolean

    /** The user's own in-app choice; the switch is actuable. */
    data class UserChoice(override val enabled: Boolean) : ReduceMotionSetting

    /**
     * The system animation scale is 0, which always wins (docs/UI_DESIGN_SYSTEM.md §7).
     * The switch shows on, is disabled, and says why.
     */
    data object ForcedBySystem : ReduceMotionSetting {
        override val enabled: Boolean = true
    }
}

sealed interface DisplaySettingsEvent {
    data class SelectEnvironment(val environment: SimulatorEnvironment) : DisplaySettingsEvent
    data class SelectQuality(val quality: QualityMode) : DisplaySettingsEvent
    data class SetReduceMotion(val enabled: Boolean) : DisplaySettingsEvent
}
