package com.example.myapplication.features.biometric

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.LoadingIndicator
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Screen for biometric visualization
 */
@Composable
fun BiometricVisualizationScreen(
    navController: NavController,
    habitId: String?,
    onNavigateBack: () -> Unit,
    viewModel: BiometricVisualizationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()
    val respirationRate by viewModel.respirationRate.collectAsState()
    val stressLevel by viewModel.stressLevel.collectAsState()
    val energyLevel by viewModel.energyLevel.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val habitName by viewModel.habitName.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            viewModel.startMonitoring()
        }
    }
    
    // Clean up resources when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopMonitoring()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (habitId != null) "Biometrics for $habitName" else "Biometric Visualization"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Biometric visualization
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isMonitoring) {
                            BiometricVisualization(
                                heartRate = heartRate,
                                respirationRate = respirationRate,
                                stressLevel = stressLevel,
                                energyLevel = energyLevel
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Tap the button below to start biometric monitoring",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Biometric data
                if (isMonitoring) {
                    BiometricDataCard(
                        title = "Heart Rate",
                        value = "$heartRate BPM",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFE57373)
                    )
                    
                    BiometricDataCard(
                        title = "Respiration Rate",
                        value = "$respirationRate breaths/min",
                        icon = Icons.Default.Air,
                        color = Color(0xFF64B5F6)
                    )
                    
                    BiometricDataCard(
                        title = "Stress Level",
                        value = "${(stressLevel * 100).toInt()}%",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFFFFB74D)
                    )
                    
                    BiometricDataCard(
                        title = "Energy Level",
                        value = "${(energyLevel * 100).toInt()}%",
                        icon = Icons.Default.BatteryChargingFull,
                        color = Color(0xFF81C784)
                    )
                    
                    // Recommendations
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val recommendation = when {
                                stressLevel > 0.7f -> "Your stress level is high. Consider taking a break or practicing deep breathing exercises."
                                heartRate > 100 -> "Your heart rate is elevated. Try to relax and avoid strenuous activities for now."
                                energyLevel < 0.3f -> "Your energy level is low. Consider taking a short rest or having a healthy snack."
                                else -> "Your biometric readings are within normal ranges. Keep up the good work!"
                            }
                            
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Start/Stop button
                Button(
                    onClick = {
                        if (isMonitoring) {
                            viewModel.stopMonitoring()
                        } else {
                            if (hasCameraPermission) {
                                coroutineScope.launch {
                                    viewModel.startMonitoring()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoring) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isMonitoring) "Stop Monitoring" else "Start Monitoring",
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (isMonitoring) "Stop Monitoring" else "Start Monitoring"
                    )
                }
                
                // Information text
                Text(
                    text = "This feature uses your device's camera and flash to measure heart rate and other biometric data. " +
                            "Place your finger over the camera lens when monitoring is active.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Loading indicator
            if (isLoading) {
                LoadingIndicator(message = "Initializing biometric sensors...")
            }
            
            // Error message
            errorMessage?.let { message ->
                if (message.isNotEmpty()) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
        }
    }
}

/**
 * Biometric data card component
 */
@Composable
fun BiometricDataCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

/**
 * Biometric visualization component
 */
@Composable
fun BiometricVisualization(
    heartRate: Int,
    respirationRate: Int,
    stressLevel: Float,
    energyLevel: Float
) {
    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "biometric")
    val heartBeatAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 60000 / heartRate,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "heartbeat"
    )
    
    val breathingAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 60000 / respirationRate,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathing"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Heart rate visualization
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            
            // Draw heart rate wave
            val path = Path()
            val waveHeight = height * 0.3f
            val waveWidth = width * 0.8f
            val startX = centerX - waveWidth / 2
            
            path.moveTo(startX, centerY)
            
            for (i in 0 until 100) {
                val x = startX + (i / 100f) * waveWidth
                val progress = (i / 100f + heartBeatAnimation.value) % 1f
                val y = centerY + sin(progress * 2 * Math.PI) * waveHeight * 
                        (if (progress > 0.9f || progress < 0.1f) 2f else 0.5f)
                
                path.lineTo(x, y.toFloat())
            }
            
            drawPath(
                path = path,
                color = Color(0xFFE57373),
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Draw stress level circle
            val stressRadius = width * 0.2f * stressLevel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFB74D),
                        Color(0xFFFFB74D).copy(alpha = 0f)
                    ),
                    center = Offset(centerX - width * 0.25f, centerY)
                ),
                radius = stressRadius,
                center = Offset(centerX - width * 0.25f, centerY)
            )
            
            // Draw energy level circle
            val energyRadius = width * 0.2f * energyLevel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF81C784),
                        Color(0xFF81C784).copy(alpha = 0f)
                    ),
                    center = Offset(centerX + width * 0.25f, centerY)
                ),
                radius = energyRadius,
                center = Offset(centerX + width * 0.25f, centerY)
            )
            
            // Draw breathing circle
            val breathingProgress = breathingAnimation.value
            val breathingRadius = width * 0.15f * (0.7f + 0.3f * sin(breathingProgress * 2 * Math.PI).toFloat())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF64B5F6),
                        Color(0xFF64B5F6).copy(alpha = 0f)
                    ),
                    center = Offset(centerX, centerY)
                ),
                radius = breathingRadius,
                center = Offset(centerX, centerY)
            )
        }
    }
}
