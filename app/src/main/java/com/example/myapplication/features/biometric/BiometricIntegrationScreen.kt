package com.example.myapplication.features.biometric

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.BiometricReading
import com.example.myapplication.data.model.BiometricType
import com.example.myapplication.ui.components.AppScaffold
import kotlinx.coroutines.launch
import java.util.*

/**
 * Biometric Integration Screen
 * Displays biometric data and allows for biometric measurements
 */
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
    
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize with specific habit if provided
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.setCurrentHabitId(habitId)
        }
        viewModel.loadBiometricData()
    }
    
    AppScaffold(
        title = "Biometric Integration",
        navController = navController,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Error message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Heart rate monitor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                HeartRateMonitor(
                    currentHeartRate = heartRate,
                    historyData = biometricReadings
                        .filter { it.type == BiometricType.HEART_RATE }
                        .map { Pair(Date(it.timestamp), it.value.toInt()) },
                    onMeasure = {
                        coroutineScope.launch {
                            viewModel.measureHeartRate()
                        }
                    }
                )
            }
            
            // Sleep quality visualization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                SleepQualityVisualizer(
                    sleepData = sleepData,
                    sleepScore = viewModel.calculateSleepScore(sleepData),
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Biometric readings list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                BiometricComponents.BiometricReadingsList(
                    readings = biometricReadings.take(10),
                    modifier = Modifier.padding(16.dp),
                    onReadingClick = { reading ->
                        // Handle reading click
                    }
                )
            }
            
            // Measurement controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Biometric Measurements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                            enabled = !isMeasuring
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Heart Rate")
                        }
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.measureStressLevel()
                                }
                            },
                            enabled = !isMeasuring
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stress")
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
                                    viewModel.measureBloodPressure()
                                }
                            },
                            enabled = !isMeasuring
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Blood Pressure")
                        }
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.analyzeSleepData()
                                }
                            },
                            enabled = !isMeasuring
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sleep")
                        }
                    }
                }
            }
        }
    }
}
