package com.allinonelighting

import android.app.Application
import com.allinonelighting.core.di.AppContainer
import com.allinonelighting.core.di.DefaultAppContainer

/**
 * Application entry point.
 *
 * Deliberately does almost nothing: no hardware queries, no disk reads, no asset
 * parsing. The container's members are lazy, so constructing it is free.
 * Startup budget is NFR-PERF-01; see docs/PERFORMANCE_AND_APP_SIZE.md §2.
 */
class AllInOneLightingApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
