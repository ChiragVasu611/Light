package com.allinonelighting.core.log

import android.util.Log
import com.allinonelighting.BuildConfig

/**
 * The only permitted logging entry point (docs/DATA_PRIVACY_AND_SECURITY.md §7,
 * enforced by T-LOG-01).
 *
 * Never log user-selected colours, device identifiers, file paths outside the app
 * sandbox, or — in a future phase — smart-light credentials.
 */
interface AppLogger {
    fun debug(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

object DebugLogger : AppLogger {
    override fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}

/** Release behaviour: debug output is dropped entirely; warnings and errors survive. */
object ReleaseLogger : AppLogger {
    override fun debug(tag: String, message: String) = Unit

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
