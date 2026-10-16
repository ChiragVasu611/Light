package com.allinonelighting.architecture

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * T-PERM-01 / T-DEP-01 / T-GLOBAL-04 / T-LOG-01 — guards that keep the app's
 * privacy and background-work posture from drifting
 * (docs/DATA_PRIVACY_AND_SECURITY.md §1, §3, §7).
 */
class ManifestAndDependencyTest {

    private val sourceRoot = File("src/main/kotlin/com/allinonelighting")

    /**
     * v1.0 declares no runtime permissions. The one entry allowed here is injected by
     * AndroidX for its own self-scoped runtime receivers; it is signature-level,
     * app-scoped, and never shown to the user.
     */
    private val allowedPermissions = setOf(
        "com.allinonelighting.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
        "com.allinonelighting.debug.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
    )

    private fun mergedReleaseManifest(): File? =
        File("build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml")
            .takeIf { it.exists() }

    @Test
    fun `release manifest declares no unapproved permission`() {
        val manifest = mergedReleaseManifest()
        // Only meaningful after assembleRelease has run; CI runs it (stage 5).
        assumeTrue("merged release manifest not built", manifest != null)

        val declared = Regex("""<uses-permission[^>]*android:name="([^"]+)"""")
            .findAll(manifest!!.readText())
            .map { it.groupValues[1] }
            .toSet()

        val unapproved = declared - allowedPermissions
        assertTrue(
            "Unapproved permissions in the release manifest. Update " +
                "docs/DATA_PRIVACY_AND_SECURITY.md §1 before adding one:\n$unapproved",
            unapproved.isEmpty(),
        )
    }

    @Test
    fun `release manifest declares no INTERNET permission`() {
        val manifest = mergedReleaseManifest()
        assumeTrue("merged release manifest not built", manifest != null)
        assertTrue(
            "v1.0 is fully offline (FR-GLOBAL-04, AC-SMART-03)",
            !manifest!!.readText().contains("android.permission.INTERNET"),
        )
    }

    @Test
    fun `source declares no camera permission`() {
        // setTorchMode() needs no CAMERA permission (FR-TORCH-08, ADR-006).
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(
            "CAMERA must not be requested for torch control (ADR-006)",
            !manifest.contains("android.permission.CAMERA"),
        )
    }

    @Test
    fun `no background work scheduler is used`() {
        // NFR-BAT-02: v1.0 schedules no background work at all.
        val forbidden = listOf(
            "androidx.work.",
            "android.app.AlarmManager",
            "android.app.job.JobScheduler",
        )
        val violations = sourceRoot.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                file.readLines()
                    .filter { line -> forbidden.any { line.contains(it) } }
                    .map { "${file.name}: ${it.trim()}" }
            }
            .toList()
        assertTrue(
            "v1.0 schedules no background work (NFR-BAT-02):\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun `android util Log is only used inside AppLogger`() {
        val violations = sourceRoot.walkTopDown()
            .filter { it.extension == "kt" }
            .filterNot { it.name == "AppLogger.kt" }
            .flatMap { file ->
                file.readLines()
                    .filter { it.contains("android.util.Log") || Regex("""\bLog\.[dwiev]\(""").containsMatchIn(it) }
                    .map { "${file.name}: ${it.trim()}" }
            }
            .toList()
        assertTrue(
            "Log through AppLogger only (docs/DATA_PRIVACY_AND_SECURITY.md §7):\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun `no networking or analytics library is referenced in source`() {
        val forbidden = listOf(
            "okhttp3", "retrofit2", "com.google.firebase",
            "com.google.android.gms.analytics", "io.ktor.client",
        )
        val violations = sourceRoot.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                file.readLines()
                    .filter { line -> forbidden.any { line.contains(it) } }
                    .map { "${file.name}: ${it.trim()}" }
            }
            .toList()
        assertTrue(
            "v1.0 makes no network calls and collects no analytics:\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
