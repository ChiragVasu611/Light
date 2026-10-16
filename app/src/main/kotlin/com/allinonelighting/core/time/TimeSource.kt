package com.allinonelighting.core.time

import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

/**
 * Monotonic clock abstraction.
 *
 * Injected everywhere timing matters — notably the emergency pattern scheduler, whose
 * step deadlines must be clock-driven rather than frame-driven (FR-EMER-09). Tests
 * substitute [TestTimeSource] to drive patterns on a virtual clock (T-EMER-01/08).
 */
interface AppTimeSource {
    /** Monotonic reading; only differences are meaningful. */
    fun elapsed(): Duration
}

/** Backed by [System.nanoTime]; unaffected by wall-clock changes. */
object SystemTimeSource : AppTimeSource {
    override fun elapsed(): Duration = System.nanoTime().nanoseconds
}
