package com.allinonelighting.fakes

import com.allinonelighting.core.result.AppError
import com.allinonelighting.core.result.AppResult
import com.allinonelighting.data.licences.Licence
import com.allinonelighting.data.licences.LicenceTextSource

/** Returns canned text, or a storage failure for licences listed in [failing]. */
class FakeLicenceTextSource(
    private val failing: Set<Licence> = emptySet(),
) : LicenceTextSource {

    val reads = mutableListOf<Licence>()

    override suspend fun read(licence: Licence): AppResult<String> {
        reads += licence
        return if (licence in failing) {
            AppResult.Failure(AppError.Storage(userMessageRes = FAILURE_MESSAGE_RES))
        } else {
            AppResult.Success(textFor(licence))
        }
    }

    companion object {
        const val FAILURE_MESSAGE_RES = 42

        fun textFor(licence: Licence) = "full text of ${licence.name}"
    }
}
