package com.allinonelighting.core.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.allinonelighting.AllInOneLightingApp

/**
 * Resolves the container from a ViewModel factory, so screens never construct
 * dependencies themselves.
 */
val CreationExtras.appContainer: AppContainer
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
        as AllInOneLightingApp).container
