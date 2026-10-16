package com.allinonelighting.data.licences

import android.content.Context
import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.allinonelighting.R
import com.allinonelighting.core.coroutines.DispatcherProvider
import com.allinonelighting.core.log.AppLogger
import com.allinonelighting.core.result.AppError
import com.allinonelighting.core.result.AppResult
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG = "LicenceTextSource"

/** Every licence used by a shipped third-party component (SCR-LICENCES). */
enum class Licence(
    @param:StringRes val nameRes: Int,
    @param:StringRes val urlRes: Int,
    @param:RawRes val textRes: Int,
) {
    Apache2(
        nameRes = R.string.licence_apache_2_0_name,
        urlRes = R.string.licence_apache_2_0_url,
        textRes = R.raw.licence_apache_2_0,
    ),

    /** Protocol Buffers' own copy, which carries Google's copyright line. */
    Bsd3ClauseProtobuf(
        nameRes = R.string.licence_bsd_3_clause_name,
        urlRes = R.string.licence_bsd_3_clause_url,
        textRes = R.raw.licence_bsd_3_clause_protobuf,
    ),
}

/** Supplies the full text of a [Licence]; the text is bundled, never fetched. */
interface LicenceTextSource {
    suspend fun read(licence: Licence): AppResult<String>
}

class RawResourceLicenceTextSource(
    context: Context,
    private val dispatchers: DispatcherProvider,
    private val logger: AppLogger,
) : LicenceTextSource {

    private val resources = context.applicationContext.resources

    override suspend fun read(licence: Licence): AppResult<String> =
        withContext(dispatchers.io) {
            try {
                val raw = resources.openRawResource(licence.textRes)
                    .bufferedReader()
                    .use { it.readText() }
                AppResult.Success(reflowLicenceText(raw))
            } catch (e: IOException) {
                logger.warn(TAG, "Could not read licence text for $licence", e)
                AppResult.Failure(unavailable(e))
            } catch (e: Resources.NotFoundException) {
                logger.warn(TAG, "Licence text resource missing for $licence", e)
                AppResult.Failure(unavailable(e))
            }
        }

    private fun unavailable(cause: Exception) = AppError.Storage(
        userMessageRes = R.string.licences_text_unavailable,
        diagnostic = cause.message,
    )
}

/**
 * Licence files are hard-wrapped at ~80 columns for terminals. On a phone that
 * produces ragged half-lines, so lines within a paragraph are joined, paragraphs are
 * kept apart, and a bulleted clause (`* ...`) starts its own line. Wording is
 * untouched.
 */
fun reflowLicenceText(raw: String): String =
    raw.replace("\r\n", "\n")
        .split(Regex("""\n\s*\n"""))
        .map { paragraph ->
            paragraph.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .fold(StringBuilder()) { out, line ->
                    when {
                        out.isEmpty() -> out.append(line)
                        line.startsWith("* ") -> out.append('\n').append(line)
                        else -> out.append(' ').append(line)
                    }
                }
                .toString()
        }
        .filter { it.isNotEmpty() }
        .joinToString("\n\n")
