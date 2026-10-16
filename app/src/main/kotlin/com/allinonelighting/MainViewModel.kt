package com.allinonelighting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allinonelighting.core.device.SystemAnimationSettings
import com.allinonelighting.core.device.resolveReduceMotion
import com.allinonelighting.core.di.appContainer
import com.allinonelighting.data.prefs.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Activity-wide state the theme depends on: the effective reduce-motion flag
 * (NFR-A11Y-04), combining the system animation scale and the in-app setting.
 */
class MainViewModel(
    preferences: PreferencesRepository,
    systemAnimations: SystemAnimationSettings,
) : ViewModel() {

    val reduceMotion: StateFlow<Boolean> = preferences.preferences
        .map { resolveReduceMotion(systemAnimations.animationsDisabled(), it.reduceMotion) }
        // Starts from the system value so the first frame is right even before the
        // store has been read.
        .stateIn(viewModelScope, SharingStarted.Eagerly, systemAnimations.animationsDisabled())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer
                MainViewModel(container.preferences, container.systemAnimations)
            }
        }
    }
}
