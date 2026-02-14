package com.example.myapplication.features.biometric

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.data.model.BiometricReading
import com.example.myapplication.core.data.model.BiometricType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Biometric Integration Screen
 * Displays biometric data and allows for biometric measurements
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricIntegrationScreen(
    navController: NavController,
    habitId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: BiometricIntegrationViewModel = hiltViewModel()
) {
    val heartRate by viewModel.heartRate.collectAsState()
    val bloodPressure by viewModel.bloodPressure.collectAsState()
    val sleepData by viewModel.sleepData.collectAsState()
    val stressLevel by viewModel.stressLevel.collectAsState()
    val biometricReadings by viewModel.biometricReadings.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Set current habit ID if provided
    LaunchedEffect(habitId) {
        habitId?.let {
            viewModel.setCurrentHabitId(it)
        }
        viewModel.loadBiometricData()
    }

    // Show error message if any
    errorMessage?.let {
        LaunchedEffect(it) {
            // Show error message
            delay(3000)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Integration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isMonitoring) {
                        IconButton(
                            onClick = { viewModel.stopMonitoring() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Monitoring",
                                tint = Color.Red
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.startMonitoring(lifecycleOwner) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Monitoring",
                                tint = Color.Green
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Current biometric data
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Biometric Data",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Heart rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Heart Rate:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$heartRate BPM",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Blood pressure
                    bloodPressure?.let { (systolic, diastolic) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color.Blue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Blood Pressure:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$systolic/$diastolic mmHg",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Stress level
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.Magenta
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stress Level:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(stressLevel * 10).toInt()} / 10",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Measurement buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Measurements",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.measureHeartRate()
                                }
                            },
                            enabled = !isMeasuring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Heart Rate")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.measureBloodPressure()
                                }
                            },
                            enabled = !isMeasuring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Blood Pressure")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.measureStressLevel()
                                }
                            },
                            enabled = !isMeasuring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Magenta
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stress Level")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.analyzeSleepData()
                                }
                            },
                            enabled = !isMeasuring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sleep Analysis")
                        }
                    }

                    // Show progress indicator when measuring
                    if (isMeasuring) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }
                }

            // Biometric readings history
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Biometric History",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (biometricReadings.isEmpty()) {
                        Text(
                            text = "No biometric readings available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(biometricReadings) { reading ->
                                BiometricReadingItem(reading = reading)
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

/**
 * Biometric reading item
 */
@Composable
fun BiometricReadingItem(reading: BiometricReading) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val date = remember(reading.timestamp) { dateFormat.format(Date(reading.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on biometric type
        val (icon, tint) = when (reading.type) {
            BiometricType.HEART_RATE -> Icons.Default.Favorite to Color.Red
            BiometricType.BLOOD_PRESSURE_SYSTOLIC, BiometricType.BLOOD_PRESSURE_DIASTOLIC ->
                Icons.Default.Speed to Color.Blue
            BiometricType.STRESS_LEVEL -> Icons.Default.Psychology to Color.Magenta
            BiometricType.SLEEP_QUALITY -> Icons.Default.Bedtime to Color.DarkGray
            else -> Icons.Default.HealthAndSafety to MaterialTheme.colorScheme.primary
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = reading.type.name.replace("_", " ").lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${reading.value} ${reading.unit} - $date",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
