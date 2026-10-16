package com.allinonelighting.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allinonelighting.core.di.appContainer
import com.allinonelighting.core.log.AppLogger
import com.allinonelighting.data.prefs.PreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "OnboardingViewModel"

class OnboardingViewModel(
    private val preferences: PreferencesRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val _completed = Channel<Unit>(Channel.BUFFERED)

    /** Emits once onboarding is done, whether finished or skipped. */
    val completed: Flow<Unit> = _completed.receiveAsFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.Skip, OnboardingEvent.Finish -> complete()
        }
    }

    private fun complete() {
        viewModelScope.launch {
            // A failed write must not block navigation: worst case the user sees
            // onboarding once more (T-ONB-06).
            runCatching { preferences.setOnboardingCompleted(true) }
                .onFailure { logger.warn(TAG, "Could not persist onboarding flag", it) }
            _completed.send(Unit)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this as CreationExtras).appContainer
                OnboardingViewModel(container.preferences, container.logger)
            }
        }
    }
}
