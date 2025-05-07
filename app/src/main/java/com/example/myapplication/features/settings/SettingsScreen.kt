package com.example.myapplication.features.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.util.FirebaseUtil
import com.example.myapplication.util.ThemePreferenceManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(context: Context) {
    val isDarkModeEnabled by ThemePreferenceManager.isDarkModeEnabled(context).collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDarkModeEnabled,
                onCheckedChange = {
                    coroutineScope.launch {
                        ThemePreferenceManager.setDarkModeEnabled(context, it)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val habitData = mapOf(
                "name" to "Example Habit",
                "description" to "This is a sample habit",
                "category" to "Health",
                "frequency" to "DAILY",
                "goal" to 1
            )
            FirebaseUtil.saveHabitData(userId, habitData,
                onSuccess = { Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show() },
                onFailure = { Toast.makeText(context, "Backup failed: ${it.message}", Toast.LENGTH_SHORT).show() }
            )
        }) {
            Text("Backup Data")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            FirebaseUtil.fetchHabitData(userId,
                onSuccess = { habits ->
                    Toast.makeText(context, "Fetched ${habits.size} habits", Toast.LENGTH_SHORT).show()
                },
                onFailure = { Toast.makeText(context, "Fetch failed: ${it.message}", Toast.LENGTH_SHORT).show() }
            )
        }) {
            Text("Restore Data")
        }
    }
}