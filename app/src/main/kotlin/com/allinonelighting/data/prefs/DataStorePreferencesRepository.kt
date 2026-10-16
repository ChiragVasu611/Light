package com.allinonelighting.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.allinonelighting.core.log.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val TAG = "PreferencesRepository"

/**
 * The corruption handler is deliberate: a malformed store resets to defaults rather
 * than crashing the app on launch (docs/DATA_PRIVACY_AND_SECURITY.md §5).
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

class DataStorePreferencesRepository(
    private val context: Context,
    private val logger: AppLogger,
) : PreferencesRepository {

    private object Keys {
        val SchemaVersion = intPreferencesKey("schema_version")
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val KeepTorchBackground = booleanPreferencesKey("keep_torch_background")
        val PhotosensitivityAck = booleanPreferencesKey("photosensitivity_ack")
        val ReduceMotion = booleanPreferencesKey("reduce_motion")
        val SimEnvironment = stringPreferencesKey("sim_environment")
        val SimQuality = stringPreferencesKey("sim_quality")
        val SimLastObject = stringPreferencesKey("sim_last_object")

        fun screenLightColor(variant: String) = intPreferencesKey("screenlight_${variant}_color")
        fun screenLightBrightness(variant: String) =
            floatPreferencesKey("screenlight_${variant}_brightness")
    }

    /** Bumped only when a migration is needed; written from day one as a baseline. */
    private val currentSchemaVersion = 1

    private fun <T> Flow<T>.recoveringFromIo(fallback: T): Flow<T> = catch { cause ->
        if (cause is IOException) {
            logger.warn(TAG, "Preference read failed; using defaults", cause)
            emit(fallback)
        } else {
            throw cause
        }
    }

    override val preferences: Flow<UserPreferences> =
        context.dataStore.data
            .map { prefs ->
                UserPreferences(
                    onboardingCompleted = prefs[Keys.OnboardingCompleted] ?: false,
                    keepTorchInBackground = prefs[Keys.KeepTorchBackground] ?: false,
                    photosensitivityAcknowledged = prefs[Keys.PhotosensitivityAck] ?: false,
                    reduceMotion = prefs[Keys.ReduceMotion],
                    simEnvironment = prefs[Keys.SimEnvironment]
                        ?.takeIf { it in UserPreferences.VALID_ENVIRONMENTS }
                        ?: UserPreferences.ENVIRONMENT_DARK,
                    simQuality = prefs[Keys.SimQuality]
                        ?.takeIf { it in UserPreferences.VALID_QUALITIES },
                    simLastObject = prefs[Keys.SimLastObject] ?: UserPreferences.DEFAULT_OBJECT,
                )
            }
            .recoveringFromIo(UserPreferences())

    override fun screenLight(variant: ScreenLightVariant): Flow<ScreenLightPreferences> =
        context.dataStore.data
            .map { prefs ->
                ScreenLightPreferences(
                    colorArgb = prefs[Keys.screenLightColor(variant.key)]
                        ?: variant.default.colorArgb,
                    // Coerced: a corrupted value must not produce an unusable screen.
                    brightness = (prefs[Keys.screenLightBrightness(variant.key)]
                        ?: variant.default.brightness)
                        .coerceIn(
                            ScreenLightPreferences.MIN_BRIGHTNESS,
                            ScreenLightPreferences.MAX_BRIGHTNESS,
                        ),
                )
            }
            .recoveringFromIo(variant.default)

    private suspend fun write(block: (MutablePreferences) -> Unit) {
        try {
            context.dataStore.edit { prefs ->
                prefs[Keys.SchemaVersion] = currentSchemaVersion
                block(prefs)
            }
        } catch (e: IOException) {
            // A failed write must never crash or block navigation; worst case the
            // user sees a default again. See SCR-ONBOARDING / T-ONB-06.
            logger.warn(TAG, "Preference write failed", e)
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        write { it[Keys.OnboardingCompleted] = completed }

    override suspend fun setKeepTorchInBackground(enabled: Boolean) =
        write { it[Keys.KeepTorchBackground] = enabled }

    override suspend fun setPhotosensitivityAcknowledged(acknowledged: Boolean) =
        write { it[Keys.PhotosensitivityAck] = acknowledged }

    override suspend fun setReduceMotion(enabled: Boolean?) = write { prefs ->
        if (enabled == null) prefs.remove(Keys.ReduceMotion) else prefs[Keys.ReduceMotion] = enabled
    }

    override suspend fun setSimEnvironment(environment: String) {
        require(environment in UserPreferences.VALID_ENVIRONMENTS) { "bad environment" }
        write { it[Keys.SimEnvironment] = environment }
    }

    override suspend fun setSimQuality(quality: String) {
        require(quality in UserPreferences.VALID_QUALITIES) { "bad quality" }
        write { it[Keys.SimQuality] = quality }
    }

    override suspend fun setSimLastObject(objectId: String) =
        write { it[Keys.SimLastObject] = objectId }

    override suspend fun setScreenLight(
        variant: ScreenLightVariant,
        value: ScreenLightPreferences,
    ) = write { prefs ->
        prefs[Keys.screenLightColor(variant.key)] = value.colorArgb
        prefs[Keys.screenLightBrightness(variant.key)] = value.brightness
    }
}
