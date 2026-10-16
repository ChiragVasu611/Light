package com.allinonelighting.data.prefs

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for persisted settings.
 *
 * Reads never fail a screen: a corrupt or unreadable store falls back to documented
 * defaults (docs/TECHNICAL_ARCHITECTURE.md §6, docs/SCREEN_SPECIFICATION.md SCR-SPLASH).
 */
interface PreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setKeepTorchInBackground(enabled: Boolean)
    suspend fun setPhotosensitivityAcknowledged(acknowledged: Boolean)
    suspend fun setReduceMotion(enabled: Boolean?)
    suspend fun setSimEnvironment(environment: String)
    suspend fun setSimQuality(quality: String)
    suspend fun setSimLastObject(objectId: String)

    fun screenLight(variant: ScreenLightVariant): Flow<ScreenLightPreferences>
    suspend fun setScreenLight(variant: ScreenLightVariant, value: ScreenLightPreferences)
}

enum class ScreenLightVariant(val key: String, val default: ScreenLightPreferences) {
    White("white", ScreenLightPreferences.White),
    Color("color", ScreenLightPreferences.Color),
    Night("night", ScreenLightPreferences.Night),
}
