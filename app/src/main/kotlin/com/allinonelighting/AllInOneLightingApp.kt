package com.allinonelighting

import android.app.Application

/**
 * Application entry point.
 *
 * Deliberately does almost nothing: no hardware queries, no disk reads, no asset
 * parsing. Startup budget is NFR-PERF-01 and the container builds lazily.
 * See docs/PERFORMANCE_AND_APP_SIZE.md §2.
 */
class AllInOneLightingApp : Application()
