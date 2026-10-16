package com.allinonelighting.fakes

import com.allinonelighting.data.prefs.PreferencesRepository
import com.allinonelighting.data.prefs.ScreenLightPreferences
import com.allinonelighting.data.prefs.ScreenLightVariant
import com.allinonelighting.data.prefs.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * In-memory preferences for tests. Hand-written rather than mocked so call order and
 * state are readable (docs/TESTING_AND_ACCEPTANCE.md §2).
 */
class FakePreferencesRepository(
    initial: UserPreferences = UserPreferences(),
) : PreferencesRepository {

    private val state = MutableStateFlow(initial)
    private val screenLightState =
        MutableStateFlow(ScreenLightVariant.entries.associateWith { it.default })

    /** When true, every write throws, simulating a full or unwritable disk. */
    var failWrites: Boolean = false

    /** Every write that was attempted, in order. */
    val writes = mutableListOf<String>()

    override val preferences: Flow<UserPreferences> = state.asStateFlow()

    override fun screenLight(variant: ScreenLightVariant): Flow<ScreenLightPreferences> =
        screenLightState.map { it.getValue(variant) }

    private inline fun update(label: String, block: (UserPreferences) -> UserPreferences) {
        writes += label
        if (failWrites) throw IOException("fake write failure")
        state.value = block(state.value)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        update("onboardingCompleted=$completed") { it.copy(onboardingCompleted = completed) }

    override suspend fun setKeepTorchInBackground(enabled: Boolean) =
        update("keepTorch=$enabled") { it.copy(keepTorchInBackground = enabled) }

    override suspend fun setPhotosensitivityAcknowledged(acknowledged: Boolean) =
        update("photoAck=$acknowledged") { it.copy(photosensitivityAcknowledged = acknowledged) }

    override suspend fun setReduceMotion(enabled: Boolean?) =
        update("reduceMotion=$enabled") { it.copy(reduceMotion = enabled) }

    override suspend fun setSimEnvironment(environment: String) =
        update("environment=$environment") { it.copy(simEnvironment = environment) }

    override suspend fun setSimQuality(quality: String) =
        update("quality=$quality") { it.copy(simQuality = quality) }

    override suspend fun setSimLastObject(objectId: String) =
        update("lastObject=$objectId") { it.copy(simLastObject = objectId) }

    override suspend fun setScreenLight(
        variant: ScreenLightVariant,
        value: ScreenLightPreferences,
    ) {
        writes += "screenLight[${variant.key}]=${value.colorArgb}/${value.brightness}"
        if (failWrites) throw IOException("fake write failure")
        screenLightState.value = screenLightState.value + (variant to value)
    }
}
