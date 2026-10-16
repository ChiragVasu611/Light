package com.allinonelighting.feature.settings.licences

import androidx.annotation.StringRes
import com.allinonelighting.R
import com.allinonelighting.data.licences.Licence

/**
 * One attributed third-party component on SCR-LICENCES.
 *
 * @param modules the Maven coordinates this entry accounts for. `"group"` covers that
 *   group and its sub-groups (`androidx` covers `androidx.compose.ui`);
 *   `"group:artifactPrefix"` covers artifacts in exactly that group whose name starts
 *   with the prefix.
 * @param excludes patterns, in the same form, carved out of [modules] because they
 *   ship under a different licence and have their own entry.
 */
data class OpenSourceComponent(
    @param:StringRes val nameRes: Int,
    @param:StringRes val detailRes: Int,
    val licence: Licence,
    val modules: List<String>,
    val excludes: List<String> = emptyList(),
) {
    /** True when this entry accounts for the module `group:artifact`. */
    fun covers(module: String): Boolean =
        modules.any { matches(module, it) } && excludes.none { matches(module, it) }

    private fun matches(module: String, pattern: String): Boolean {
        val group = module.substringBefore(':')
        val artifact = module.substringAfter(':', missingDelimiterValue = "")
        return if (':' in pattern) {
            group == pattern.substringBefore(':') &&
                artifact.startsWith(pattern.substringAfter(':'))
        } else {
            group == pattern || group.startsWith("$pattern.")
        }
    }
}

/**
 * DataStore bundles a repackaged protobuf-lite. It is published under the androidx
 * group but licensed BSD-3-Clause, not Apache 2.0 (its POM and the LICENSE.txt it puts
 * in the APK both say so).
 */
private const val DATASTORE_PROTOBUF = "androidx.datastore:datastore-preferences-external-protobuf"

/**
 * Exactly what ships in the release build — no more, no less.
 *
 * Filament (Apache 2.0) and the Inter font (SIL OFL 1.1) are named by the spec but are
 * not in the build yet (Phases 3 and 4). Their entries are added in the same change
 * that adds them; LicenceCoverageTest fails the build until that happens, and fails
 * it if an entry is listed for something that does not ship (ADR-023).
 */
val OpenSourceComponents: List<OpenSourceComponent> = listOf(
    OpenSourceComponent(
        nameRes = R.string.oss_androidx_name,
        detailRes = R.string.oss_androidx_detail,
        licence = Licence.Apache2,
        modules = listOf("androidx"),
        excludes = listOf(DATASTORE_PROTOBUF),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_protobuf_name,
        detailRes = R.string.oss_protobuf_detail,
        licence = Licence.Bsd3ClauseProtobuf,
        modules = listOf(DATASTORE_PROTOBUF),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_kotlin_name,
        detailRes = R.string.oss_kotlin_detail,
        licence = Licence.Apache2,
        modules = listOf("org.jetbrains.kotlin"),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_coroutines_name,
        detailRes = R.string.oss_coroutines_detail,
        licence = Licence.Apache2,
        modules = listOf("org.jetbrains.kotlinx:kotlinx-coroutines"),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_serialization_name,
        detailRes = R.string.oss_serialization_detail,
        licence = Licence.Apache2,
        modules = listOf("org.jetbrains.kotlinx:kotlinx-serialization"),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_jetbrains_annotations_name,
        detailRes = R.string.oss_jetbrains_annotations_detail,
        licence = Licence.Apache2,
        modules = listOf("org.jetbrains:annotations"),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_okio_name,
        detailRes = R.string.oss_okio_detail,
        licence = Licence.Apache2,
        modules = listOf("com.squareup.okio"),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_guava_name,
        detailRes = R.string.oss_guava_detail,
        licence = Licence.Apache2,
        modules = listOf("com.google.guava:listenablefuture"),
    ),
    OpenSourceComponent(
        nameRes = R.string.oss_jspecify_name,
        detailRes = R.string.oss_jspecify_detail,
        licence = Licence.Apache2,
        modules = listOf("org.jspecify"),
    ),
)
