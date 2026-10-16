package com.allinonelighting.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allinonelighting.core.di.appContainer
import com.allinonelighting.data.prefs.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * SCR-SPLASH — resolves the start destination from one DataStore read.
 *
 * There is no artificial delay, animation gate, or minimum display duration
 * (FR-ONB-01, AC-ONB-05).
 */
class SplashViewModel(preferences: PreferencesRepository) : ViewModel() {

    sealed interface StartDestination {
        data object Resolving : StartDestination
        data object Onboarding : StartDestination
        data object Home : StartDestination
    }

    private val _destination = MutableStateFlow<StartDestination>(StartDestination.Resolving)
    val destination: StateFlow<StartDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // A read failure falls back to showing onboarding rather than blocking.
            val completed = runCatching { preferences.preferences.first().onboardingCompleted }
                .getOrDefault(false)
            _destination.value =
                if (completed) StartDestination.Home else StartDestination.Onboarding
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SplashViewModel((this as CreationExtras).appContainer.preferences)
            }
        }
    }
}
