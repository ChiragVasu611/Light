package com.allinonelighting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allinonelighting.feature.splash.SplashViewModel
import com.allinonelighting.navigation.AppNavHost
import com.allinonelighting.navigation.Routes
import com.allinonelighting.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels { SplashViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        // The splash is held only until one DataStore read resolves — typically a
        // few milliseconds. No artificial delay (FR-ONB-01, AC-ONB-05).
        splash.setKeepOnScreenCondition {
            splashViewModel.destination.value is SplashViewModel.StartDestination.Resolving
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val destination by splashViewModel.destination.collectAsStateWithLifecycle()
                when (destination) {
                    // The system splash is still covering the window here.
                    SplashViewModel.StartDestination.Resolving -> Unit

                    SplashViewModel.StartDestination.Onboarding ->
                        AppNavHost(
                            startDestination = Routes.ONBOARDING,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        )

                    SplashViewModel.StartDestination.Home ->
                        AppNavHost(
                            startDestination = Routes.HOME,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        )
                }
            }
        }
    }
}
