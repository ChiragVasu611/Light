package com.allinonelighting.domain.simulator

import com.allinonelighting.data.prefs.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The domain enums and the preferences store must agree on persisted keys, or a
 * selection would be rejected by the store (or silently read back as the default).
 */
class SimulatorSettingKeysTest {

    @Test
    fun `environment keys match the values the store accepts`() {
        assertEquals(
            UserPreferences.VALID_ENVIRONMENTS,
            SimulatorEnvironment.entries.map { it.key }.toSet(),
        )
        assertEquals(UserPreferences.ENVIRONMENT_DARK, SimulatorEnvironment.Default.key)
    }

    @Test
    fun `quality keys match the values the store accepts`() {
        assertEquals(UserPreferences.VALID_QUALITIES, QualityMode.entries.map { it.key }.toSet())
    }

    @Test
    fun `unknown keys resolve safely`() {
        assertEquals(SimulatorEnvironment.Dark, SimulatorEnvironment.fromKey("nope"))
        assertEquals(SimulatorEnvironment.Dark, SimulatorEnvironment.fromKey(null))
        assertNull(QualityMode.fromKey("ultra"))
        assertNull(QualityMode.fromKey(null))
    }
}
