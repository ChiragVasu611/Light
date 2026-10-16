package com.allinonelighting.core.di

import android.content.Context
import com.allinonelighting.BuildConfig
import com.allinonelighting.core.coroutines.DefaultDispatcherProvider
import com.allinonelighting.core.coroutines.DispatcherProvider
import com.allinonelighting.core.log.AppLogger
import com.allinonelighting.core.log.DebugLogger
import com.allinonelighting.core.log.ReleaseLogger
import com.allinonelighting.core.time.AppTimeSource
import com.allinonelighting.core.time.SystemTimeSource
import com.allinonelighting.data.prefs.DataStorePreferencesRepository
import com.allinonelighting.data.prefs.PreferencesRepository

/**
 * The application's object graph (ADR-003 — manual DI, no Hilt).
 *
 * Tests substitute a fake implementation. Revisit this choice if the graph exceeds
 * ~25 entries or needs scopes beyond process/ViewModel.
 */
interface AppContainer {
    val logger: AppLogger
    val dispatchers: DispatcherProvider
    val timeSource: AppTimeSource
    val preferences: PreferencesRepository
}

/**
 * Every member is lazy: Application.onCreate must do no disk or hardware work
 * (docs/PERFORMANCE_AND_APP_SIZE.md §2).
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val logger: AppLogger by lazy {
        if (BuildConfig.DEBUG) DebugLogger else ReleaseLogger
    }

    override val dispatchers: DispatcherProvider = DefaultDispatcherProvider

    override val timeSource: AppTimeSource = SystemTimeSource

    override val preferences: PreferencesRepository by lazy {
        DataStorePreferencesRepository(context.applicationContext, logger)
    }
}
