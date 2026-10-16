package com.allinonelighting.core.result

import androidx.annotation.StringRes

/**
 * Every error carries its own user-facing string resource, so there is no code path
 * that can produce a generic "Something went wrong" message (FR-GLOBAL-01 spirit,
 * docs/TECHNICAL_ARCHITECTURE.md §6).
 *
 * Exception class names and stack traces are never surfaced to the user; [diagnostic]
 * is for logs only.
 */
sealed interface AppError {
    @get:StringRes
    val userMessageRes: Int

    /** Developer-facing detail. Logged, never displayed. */
    val diagnostic: String?

    data class Hardware(
        val reason: HardwareReason,
        @StringRes override val userMessageRes: Int,
        override val diagnostic: String? = null,
    ) : AppError

    data class AssetLoad(
        val assetId: String,
        val kind: AssetFailureKind,
        @StringRes override val userMessageRes: Int,
        override val diagnostic: String? = null,
    ) : AppError

    data class Renderer(
        val stage: RendererStage,
        @StringRes override val userMessageRes: Int,
        override val diagnostic: String? = null,
    ) : AppError

    data class Storage(
        @StringRes override val userMessageRes: Int,
        override val diagnostic: String? = null,
    ) : AppError
}

enum class HardwareReason { NoFlashUnit, CameraInUse, DisabledByPolicy, Unknown }

enum class AssetFailureKind { Missing, Corrupt, MissingLightAnchor, CatalogCorrupt }

enum class RendererStage { EngineInit, SwapChain, SceneBuild, Unsupported }
