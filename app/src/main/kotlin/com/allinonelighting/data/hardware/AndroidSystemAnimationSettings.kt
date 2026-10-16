package com.allinonelighting.data.hardware

import android.content.Context
import android.provider.Settings
import com.allinonelighting.core.device.SystemAnimationSettings

/**
 * Reads [Settings.Global.ANIMATOR_DURATION_SCALE] on every call, so a change made in
 * system settings while the app is running is picked up the next time state is
 * derived. It is a single key-value lookup, cheap enough not to cache.
 */
class AndroidSystemAnimationSettings(context: Context) : SystemAnimationSettings {

    private val resolver = context.applicationContext.contentResolver

    override fun animationsDisabled(): Boolean =
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}
