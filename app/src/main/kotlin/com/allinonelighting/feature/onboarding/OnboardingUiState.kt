package com.allinonelighting.feature.onboarding

import androidx.annotation.StringRes
import com.allinonelighting.R

/** One onboarding page — docs/SCREEN_SPECIFICATION.md SCR-ONBOARDING. */
data class OnboardingPage(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
)

val OnboardingPages = listOf(
    OnboardingPage(R.string.onboarding_1_title, R.string.onboarding_1_body),
    OnboardingPage(R.string.onboarding_2_title, R.string.onboarding_2_body),
    OnboardingPage(R.string.onboarding_3_title, R.string.onboarding_3_body),
)

sealed interface OnboardingEvent {
    data object Skip : OnboardingEvent
    data object Finish : OnboardingEvent
}
