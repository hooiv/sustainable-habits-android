package com.example.myapplication.features.advanced

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.biometric.BiometricData
import com.example.myapplication.data.nlp.NlpIntent
import com.example.myapplication.data.quantum.QuantumParticle
import com.example.myapplication.data.quantum.QuantumVisualization
import com.example.myapplication.data.spatial.SpatialObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Biometric monitoring component
 */
@Composable
fun BiometricMonitoringCard(
    biometricData: BiometricData,
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Biometric Integration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Monitoring status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isMonitoring) Color.Green else Color.Red)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heart rate visualization
            HeartRateVisualization(
                heartRate = biometricData.heartRate,
                confidence = biometricData.heartRateConfidence,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Biometric data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BiometricDataItem(
                    icon = Icons.Default.Favorite,
                    label = "Heart Rate",
                    value = "${biometricData.heartRate} BPM",
                    color = MaterialTheme.colorScheme.primary
                )
                
                BiometricDataItem(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Steps",
                    value = "${biometricData.stepCount}",
                    color = MaterialTheme.colorScheme.secondary
                )
                
                BiometricDataItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Calories",
                    value = "${biometricData.caloriesBurned}",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BiometricMeterItem(
                    label = "Stress Level",
                    value = biometricData.stressLevel,
                    color = lerp(Color.Green, Color.Red, biometricData.stressLevel)
                )
                
                BiometricMeterItem(
                    label = "Sleep Quality",
                    value = biometricData.sleepQuality ?: 0f,
                    color = lerp(Color.Red, Color.Green, biometricData.sleepQuality ?: 0f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start/Stop button
            Button(
                onClick = if (isMonitoring) onStopMonitoring else onStartMonitoring,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonitoring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isMonitoring) "Stop Monitoring" else "Start Monitoring")
            }
        }
    }
}

/**
 * Heart rate visualization
 */
@Composable
fun HeartRateVisualization(
    heartRate: Int,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedHeartbeat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 60000 / max(heartRate, 1),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            
            // Draw baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1.dp.toPx()
            )
            
            // Draw heart rate waveform
            val path = Path()
            path.moveTo(0f, centerY)
            
            for (x in 0 until width.toInt() step 4) {
                val progress = (x.toFloat() / width + animatedHeartbeat) % 1f
                val y = when {
                    progress < 0.1f -> centerY - height * 0.1f * sin(progress * PI.toFloat() * 10)
                    progress < 0.2f -> centerY - height * 0.4f * sin((progress - 0.1f) * PI.toFloat() * 5)
                    progress < 0.3f -> centerY + height * 0.2f * sin((progress - 0.2f) * PI.toFloat() * 5)
                    progress < 0.4f -> centerY - height * 0.1f * sin((progress - 0.3f) * PI.toFloat() * 5)
                    else -> centerY
                }
                path.lineTo(x.toFloat(), y)
            }
            
            // Draw the path
            drawPath(
                path = path,
                color = MaterialTheme.colorScheme.primary.copy(alpha = confidence),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Heart rate text
        Text(
            text = "$heartRate BPM",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Biometric data item
 */
@Composable
fun BiometricDataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Biometric meter item
 */
@Composable
fun BiometricMeterItem(
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp * value)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Spatial computing visualization
 */
@Composable
fun SpatialComputingCard(
    spatialObjects: List<SpatialObject>,
    isSpatialTrackingActive: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onPlaceObject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Spatial Computing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Tracking status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isSpatialTrackingActive) Color.Green else Color.Red)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Spatial visualization
            SpatialVisualization(
                spatialObjects = spatialObjects,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Object count
            Text(
                text = "${spatialObjects.size} objects in spatial environment",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = if (isSpatialTrackingActive) onStopTracking else onStartTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpatialTrackingActive) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isSpatialTrackingActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (isSpatialTrackingActive) "Stop Tracking" else "Start Tracking")
                }
                
                Button(
                    onClick = onPlaceObject,
                    enabled = isSpatialTrackingActive
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Place Object")
                }
            }
        }
    }
}

/**
 * Spatial visualization
 */
@Composable
fun SpatialVisualization(
    spatialObjects: List<SpatialObject>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            
            // Draw grid
            val gridSize = 20
            val gridColor = Color.Gray.copy(alpha = 0.2f)
            
            for (i in 0..gridSize) {
                val x = width * i / gridSize
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            for (i in 0..gridSize) {
                val y = height * i / gridSize
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw objects
            spatialObjects.forEach { obj ->
                // Calculate position on screen (simplified 3D to 2D projection)
                val x = centerX + (obj.position.x * 20).toFloat() % width
                val y = centerY + (obj.position.z * 20).toFloat() % height
                val size = (30 * obj.scale).toFloat().coerceIn(10f, 50f)
                
                // Draw object
                when (obj.type) {
                    com.example.myapplication.data.spatial.SpatialObjectType.HABIT -> {
                        // Draw habit as cube
                        drawRect(
                            color = Color(obj.color).copy(alpha = 0.7f),
                            topLeft = Offset(x - size/2, y - size/2),
                            size = Size(size, size),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        // Draw inner cube with animation
                        val innerSize = size * (0.6f + 0.2f * sin(animationProgress * 2 * PI.toFloat()))
                        drawRect(
                            color = Color(obj.color).copy(alpha = 0.3f),
                            topLeft = Offset(x - innerSize/2, y - innerSize/2),
                            size = Size(innerSize, innerSize)
                        )
                    }
                    com.example.myapplication.data.spatial.SpatialObjectType.COMPLETION -> {
                        // Draw completion as circle
                        drawCircle(
                            color = Color(obj.color).copy(alpha = 0.7f),
                            center = Offset(x, y),
                            radius = size / 2,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        // Draw inner circle with animation
                        val innerRadius = size / 2 * (0.6f + 0.2f * sin(animationProgress * 2 * PI.toFloat()))
                        drawCircle(
                            color = Color(obj.color).copy(alpha = 0.3f),
                            center = Offset(x, y),
                            radius = innerRadius
                        )
                    }
                    com.example.myapplication.data.spatial.SpatialObjectType.STREAK -> {
                        // Draw streak as star
                        val path = Path()
                        val outerRadius = size / 2
                        val innerRadius = outerRadius * 0.4f
                        val numPoints = 5
                        
                        for (i in 0 until numPoints * 2) {
                            val radius = if (i % 2 == 0) outerRadius else innerRadius
                            val angle = i * PI.toFloat() / numPoints
                            val px = x + radius * cos(angle + animationProgress * 2 * PI.toFloat())
                            val py = y + radius * sin(angle + animationProgress * 2 * PI.toFloat())
                            
                            if (i == 0) {
                                path.moveTo(px, py)
                            } else {
                                path.lineTo(px, py)
                            }
                        }
                        path.close()
                        
                        drawPath(
                            path = path,
                            color = Color(obj.color).copy(alpha = 0.7f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        drawPath(
                            path = path,
                            color = Color(obj.color).copy(alpha = 0.3f)
                        )
                    }
                    else -> {
                        // Draw other objects as diamond
                        val path = Path()
                        path.moveTo(x, y - size/2)
                        path.lineTo(x + size/2, y)
                        path.lineTo(x, y + size/2)
                        path.lineTo(x - size/2, y)
                        path.close()
                        
                        drawPath(
                            path = path,
                            color = Color(obj.color).copy(alpha = 0.7f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        drawPath(
                            path = path,
                            color = Color(obj.color).copy(alpha = 0.3f)
                        )
                    }
                }
                
                // Draw label
                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint()
                    paint.color = android.graphics.Color.WHITE
                    paint.textSize = 8.sp.toPx()
                    paint.textAlign = Paint.Align.CENTER
                    drawText(obj.label, x, y + size/2 + 15, paint)
                }
            }
        }
    }
}
