package com.allinonelighting.architecture

import com.allinonelighting.data.licences.Licence
import com.allinonelighting.feature.settings.licences.OpenSourceComponents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * T-SET-07 — the licences screen matches what actually ships (FR-SET-06, ADR-023).
 *
 * The module list is written by the `writeReleaseModules` Gradle task, which every
 * Test task depends on, so the file always exists when this runs.
 */
class LicenceCoverageTest {

    private val modulesFile = File("build/licences/release-runtime-modules.txt")

    private fun shippedModules(): List<String> {
        assertTrue(
            "Missing ${modulesFile.path}; the writeReleaseModules task must run first",
            modulesFile.exists(),
        )
        return modulesFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    @Test
    fun `the module list is real, not empty`() {
        val modules = shippedModules()
        assertTrue("expected a populated release classpath, got ${modules.size}", modules.size >= 20)
        assertTrue(modules.any { it.startsWith("androidx.compose.") })
    }

    @Test
    fun `every shipped module is attributed by exactly one entry`() {
        // Exactly one, not "at least one": a broad entry (androidx = Apache 2.0) must
        // not silently claim a module that ships under another licence.
        val problems = shippedModules().mapNotNull { module ->
            val owners = OpenSourceComponents.filter { it.covers(module) }
            when (owners.size) {
                1 -> null
                0 -> "$module: no entry"
                else -> "$module: claimed by ${owners.map { it.modules }}"
            }
        }
        assertTrue(
            "Every module in the release build needs exactly one entry in " +
                "OpenSourceComponents, with its own licence:\n" + problems.joinToString("\n"),
            problems.isEmpty(),
        )
    }

    @Test
    fun `DataStore's repackaged protobuf is attributed under BSD, not Apache`() {
        val module = "androidx.datastore:datastore-preferences-external-protobuf"
        assertTrue("expected $module in the release build", module in shippedModules())
        val owner = OpenSourceComponents.single { it.covers(module) }
        assertEquals(Licence.Bsd3ClauseProtobuf, owner.licence)
    }

    @Test
    fun `every licences entry corresponds to something that ships`() {
        val modules = shippedModules()
        val stale = OpenSourceComponents.filter { component ->
            modules.none { component.covers(it) }
        }
        assertTrue(
            "These licence entries match nothing in the release build. Attributing a " +
                "component that does not ship is inaccurate; remove them or add the " +
                "dependency:\n" + stale.joinToString("\n") { it.modules.toString() },
            stale.isEmpty(),
        )
    }

    @Test
    fun `coverage matching is by group boundary, not raw prefix`() {
        val kotlin = OpenSourceComponents.first { "org.jetbrains.kotlin" in it.modules }
        assertTrue(kotlin.covers("org.jetbrains.kotlin:kotlin-stdlib"))
        // kotlinx is a different project with its own entries.
        assertTrue(!kotlin.covers("org.jetbrains.kotlinx:kotlinx-coroutines-core"))

        val androidx = OpenSourceComponents.first { "androidx" in it.modules }
        assertTrue(androidx.covers("androidx.compose.ui:ui"))
        assertTrue(!androidx.covers("androidxfake:thing"))
    }
}
