package com.allinonelighting.feature.settings.licences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allinonelighting.core.di.appContainer
import com.allinonelighting.core.result.AppResult
import com.allinonelighting.data.licences.LicenceTextSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LicencesViewModel(
    private val licenceTexts: LicenceTextSource,
    components: List<OpenSourceComponent> = OpenSourceComponents,
) : ViewModel() {

    /** Each licence used by at least one component, in first-use order, read once. */
    private val licences = components.map { it.licence }.distinct()

    private val _state = MutableStateFlow(
        LicencesUiState(
            components = components,
            licenceTexts = licences.associateWith { LicenceTextState.Loading },
        ),
    )
    val state: StateFlow<LicencesUiState> = _state.asStateFlow()

    init {
        licences.forEach { licence ->
            viewModelScope.launch {
                val textState = when (val result = licenceTexts.read(licence)) {
                    is AppResult.Success -> LicenceTextState.Loaded(result.value)
                    is AppResult.Failure ->
                        LicenceTextState.Unavailable(result.error.userMessageRes)
                }
                _state.update { it.copy(licenceTexts = it.licenceTexts + (licence to textState)) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LicencesViewModel(appContainer.licenceTexts)
            }
        }
    }
}
