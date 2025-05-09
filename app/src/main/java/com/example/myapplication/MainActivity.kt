package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Box

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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var startDestination by rememberSaveable(stateSaver = StartDestination.Saver) { mutableStateOf<StartDestination>(StartDestination.Splash) }

                    LaunchedEffect(Unit) {
                        delay(1500) // Splash duration
                        // TODO: Replace with DataStore or SharedPreferences check for onboarding completion
                        val prefs = context.getSharedPreferences("onboarding", MODE_PRIVATE)
                        val completed = prefs.getBoolean("completed", false)
                        startDestination = if (completed) StartDestination.Main else StartDestination.Onboarding
                    }

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

@Composable
fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MyApp",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // Simple onboarding with 2 steps (expand as needed)
    var page by rememberSaveable { mutableStateOf(0) }
    val pages = listOf(
        "Welcome to MyApp! Track your habits easily.",
        "Stay motivated with reminders and stats."
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pages[page],
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(32.dp))
            Row {
                if (page > 0) {
                    Button(onClick = { page-- }) { Text("Back") }
                    Spacer(Modifier.width(16.dp))
                }
                if (page < pages.lastIndex) {
                    Button(onClick = { page++ }) { Text("Next") }
                } else {
                    Button(onClick = onFinish) { Text("Get Started") }
                }
            }
        }
    }
}