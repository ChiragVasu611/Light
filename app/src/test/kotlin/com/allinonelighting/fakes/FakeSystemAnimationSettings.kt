package com.allinonelighting.fakes

import com.allinonelighting.core.device.SystemAnimationSettings

/** Scriptable stand-in for the system animator duration scale. */
class FakeSystemAnimationSettings(
    var disabled: Boolean = false,
) : SystemAnimationSettings {
    override fun animationsDisabled(): Boolean = disabled
}
