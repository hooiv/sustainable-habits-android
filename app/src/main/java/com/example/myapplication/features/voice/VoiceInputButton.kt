package com.example.myapplication.features.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Voice input button with animations
 */
@Composable
fun VoiceInputButton(
    isListening: Boolean,
    voiceAmplitude: Float = 0f,
    voiceInputText: String = "",
    continuous: Boolean = false,
    useWakeWord: Boolean = false,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Button size animation
    val buttonSize by animateDpAsState(
        targetValue = if (isListening) 72.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonSize"
    )
    
    // Button color animation
    val buttonColor by animateColorAsState(
        targetValue = if (isListening) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(300),
        label = "buttonColor"
    )
    
    // Content color animation
    val contentColor by animateColorAsState(
        targetValue = if (isListening) 
            MaterialTheme.colorScheme.onPrimary 
        else 
            MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = tween(300),
        label = "contentColor"
    )
    
    Box(
        modifier = modifier
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animated ripple effect when listening
        if (isListening) {
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "ripple$index")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, delayMillis = index * 400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleScale$index"
                )
                
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, delayMillis = index * 400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleAlpha$index"
                )
                
                Canvas(
                    modifier = Modifier
                        .size(buttonSize * 2)
                        .alpha(alpha)
                ) {
                    drawCircle(
                        color = buttonColor.copy(alpha = 0.3f),
                        radius = (size.minDimension / 2) * scale,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
        
        // Voice amplitude visualization
        if (isListening && voiceAmplitude > 0) {
            val animatedAmplitude by animateFloatAsState(
                targetValue = voiceAmplitude,
                animationSpec = tween(100),
                label = "amplitude"
            )
            
            Canvas(
                modifier = Modifier
                    .size(buttonSize * 1.5f)
            ) {
                drawCircle(
                    color = buttonColor.copy(alpha = animatedAmplitude * 0.3f),
                    radius = (size.minDimension / 2) * (0.8f + animatedAmplitude * 0.4f)
                )
            }
        }
        
        // Main button
        FloatingActionButton(
            onClick = {
                if (isListening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
            },
            modifier = Modifier
                .size(buttonSize),
            containerColor = buttonColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Rounded.Close else Icons.Rounded.Mic,
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Voice input text display
        if (isListening && voiceInputText.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .widthIn(max = 250.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = voiceInputText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        // Mode indicators
        if (isListening) {
            Row(
                modifier = Modifier
                    .padding(top = 100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (continuous) {
                    Text(
                        text = "Continuous",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (continuous && useWakeWord) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                if (useWakeWord) {
                    Text(
                        text = "Wake Word",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
