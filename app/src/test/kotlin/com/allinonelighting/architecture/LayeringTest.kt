package com.allinonelighting.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * T-ARCH-01 — enforces the dependency direction from
 * docs/TECHNICAL_ARCHITECTURE.md §2.
 *
 * The app is a single Gradle module (ADR-001), so layering is enforced here rather
 * than by module boundaries. This test is what makes a later extraction mechanical.
 */
class LayeringTest {

    private val sourceRoot = File("src/main/kotlin/com/allinonelighting")

    private fun sourcesIn(vararg packages: String): List<File> =
        packages.flatMap { pkg ->
            File(sourceRoot, pkg).walkTopDown().filter { it.extension == "kt" }.toList()
        }

    private fun assertNoImports(files: List<File>, forbidden: List<String>, why: String) {
        val violations = files.flatMap { file ->
            file.readLines()
                .filter { it.startsWith("import ") }
                .filter { line -> forbidden.any { line.contains(it) } }
                .map { "${file.relativeTo(sourceRoot)}: ${it.trim()}" }
        }
        assertTrue("$why\n" + violations.joinToString("\n"), violations.isEmpty())
    }

    @Test
    fun `the scanner actually finds source files`() {
        val files = sourcesIn("core", "ui", "feature")
        assertTrue(
            "Expected to scan app sources but found ${files.size}; " +
                "working dir is ${File(".").absolutePath}",
            files.size >= 10,
        )
    }

    @Test
    fun `domain and core do not depend on ui or feature`() {
        val files = sourcesIn("core", "domain")
        assertNoImports(
            files,
            listOf(
                "com.allinonelighting.feature.",
                "com.allinonelighting.ui.",
                "com.allinonelighting.navigation.",
                "com.allinonelighting.render.",
            ),
            "core/domain must not depend on outer layers:",
        )
    }

    @Test
    fun `feature layer does not reach into data or render implementations`() {
        // Features receive interfaces from AppContainer; they must not construct or
        // import concrete implementations.
        val files = sourcesIn("feature")
        assertNoImports(
            files,
            listOf(
                "com.allinonelighting.data.prefs.DataStorePreferencesRepository",
                "com.allinonelighting.render.Filament",
            ),
            "feature must depend on interfaces, not implementations:",
        )
    }

    @Test
    fun `ui theme and components do not depend on features`() {
        val files = sourcesIn("ui")
        assertNoImports(
            files,
            listOf("com.allinonelighting.feature.", "com.allinonelighting.data."),
            "ui must be feature-agnostic:",
        )
    }

    @Test
    fun `domain contains no android framework imports`() {
        val domain = File(sourceRoot, "domain")
        if (!domain.exists()) return // domain lands in Phase 2
        val files = domain.walkTopDown().filter { it.extension == "kt" }.toList()
        assertNoImports(
            files,
            listOf("import android.", "import androidx.compose"),
            "domain must stay JVM-testable with no Android framework types:",
        )
    }
}
