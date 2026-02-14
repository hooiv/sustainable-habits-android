package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.myapplication.features.onboarding.OnboardingScreen
import com.example.myapplication.features.splash.SplashScreen
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/**
 * Sealed class representing the possible start destinations for the app.
 * Determines whether to show Splash, Onboarding, or the Main navigation.
 */
sealed class StartDestination {
    object Splash : StartDestination()
    object Onboarding : StartDestination()
    object Main : StartDestination()

    companion object {
        val Saver: Saver<StartDestination, String> = Saver(
            save = {
                when (it) {
                    Splash -> "Splash"
                    Onboarding -> "Onboarding"
                    Main -> "Main"
                }
            },
            restore = {
                when (it) {
                    "Splash" -> Splash
                    "Onboarding" -> Onboarding
                    "Main" -> Main
                    else -> throw IllegalArgumentException("Unknown StartDestination: $it")
                }
            }
        )
    }
}

/**
 * Main entry point for the application.
 * Handles theme setup, splash/onboarding flow, and delegates to AppNavigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApplication)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var startDestination by rememberSaveable(stateSaver = StartDestination.Saver) {
                        mutableStateOf<StartDestination>(StartDestination.Splash)
                    }

                    LaunchedEffect(Unit) {
                        delay(1500) // Splash duration
                        val prefs = context.getSharedPreferences("onboarding", MODE_PRIVATE)
                        val completed = prefs.getBoolean("completed", false)
                        startDestination = if (completed) StartDestination.Main else StartDestination.Onboarding
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (startDestination) {
                            StartDestination.Splash -> SplashScreen()
                            StartDestination.Onboarding -> OnboardingScreen(
                                onFinish = {
                                    context.getSharedPreferences("onboarding", MODE_PRIVATE)
                                        .edit().putBoolean("completed", true).apply()
                                    startDestination = StartDestination.Main
                                }
                            )
                            StartDestination.Main -> AppNavigation()
                        }
                    }
                }
            }
        }
    }
}
