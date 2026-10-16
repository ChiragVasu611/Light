package com.allinonelighting.feature.settings.licences

import androidx.annotation.StringRes
import com.allinonelighting.data.licences.Licence

/**
 * SCR-LICENCES state. The component list is static and always present (AC-SET-04);
 * only the bundled licence texts are read from disk, so each carries its own state.
 */
data class LicencesUiState(
    val components: List<OpenSourceComponent>,
    val licenceTexts: Map<Licence, LicenceTextState>,
)

sealed interface LicenceTextState {
    data object Loading : LicenceTextState
    data class Loaded(val text: String) : LicenceTextState
    data class Unavailable(@param:StringRes val messageRes: Int) : LicenceTextState
}
