package com.allinonelighting.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * T-DS-02 — no raw design literals outside the theme package
 * (docs/UI_DESIGN_SYSTEM.md §11).
 *
 * Colours, spacings and radii must come from tokens so a design change lands in one
 * place. ui/theme defines the tokens and is therefore exempt.
 */
class DesignTokenTest {

    private val sourceRoot = File("src/main/kotlin/com/allinonelighting")
    private val exemptDirs = setOf("ui/theme")

    private fun tokenConsumingSources(): List<File> =
        sourceRoot.walkTopDown()
            .filter { it.extension == "kt" }
            .filterNot { file ->
                val rel = file.relativeTo(sourceRoot).path.replace(File.separatorChar, '/')
                exemptDirs.any { rel.startsWith(it) }
            }
            .toList()

    @Test
    fun `the scanner actually finds source files`() {
        // Guards against the whole suite passing vacuously if the working directory
        // or package layout changes.
        val files = tokenConsumingSources()
        assertTrue(
            "Expected to scan app sources but found ${files.size}; " +
                "working dir is ${File(".").absolutePath}",
            files.size >= 10,
        )
    }

    @Test
    fun `no raw Color literals outside the theme package`() {
        val pattern = Regex("""Color\(0x[0-9A-Fa-f]{6,8}\)""")
        val violations = tokenConsumingSources().flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> pattern.containsMatchIn(line) }
                .map { (i, line) -> "${file.name}:${i + 1}  ${line.trim()}" }
        }
        assertTrue(
            "Use MaterialTheme.colorScheme or a FeatureAccent instead of a raw colour:\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun `no raw dp literals outside the theme package`() {
        // 0.dp is allowed (it means "none", not a design decision).
        val pattern = Regex("""\b(?!0\.dp)\d+(\.\d+)?\.dp\b""")
        val violations = tokenConsumingSources().flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> pattern.containsMatchIn(line) }
                .map { (i, line) -> "${file.name}:${i + 1}  ${line.trim()}" }
        }
        assertTrue(
            "Use LocalSpacing / LocalRadius / LocalSizes instead of a dp literal:\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun `no raw sp literals outside the theme package`() {
        val pattern = Regex("""\b\d+(\.\d+)?\.sp\b""")
        val violations = tokenConsumingSources().flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> pattern.containsMatchIn(line) }
                .map { (i, line) -> "${file.name}:${i + 1}  ${line.trim()}" }
        }
        assertTrue(
            "Use MaterialTheme.typography instead of an sp literal:\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
