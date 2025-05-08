package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * An animated circular progress indicator with customizable appearance
 */
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    thickness: Dp = 12.dp,
    animationDuration: Int = 1000,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    capStyle: StrokeCap = StrokeCap.Round,
    startAngle: Float = 270f,
    showProgress: Boolean = true,
    progressTextColor: Color = MaterialTheme.colorScheme.onSurface,
    progressTextSize: Dp = 24.dp,
    animateRotation: Boolean = true
) {
    // Constrain progress between 0f and 1f
    val validProgress = progress.coerceIn(0f, 1f)
    
    // Animate the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = validProgress,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )
    
    // Animate rotation for loading indicator effect
    val rotationAngle by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .padding(thickness / 2),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = thickness.toPx(),
                    cap = capStyle
                )
            )
            
            // Foreground progress arc
            rotate(degrees = if (animateRotation) rotationAngle else 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = startAngle,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(
                        width = thickness.toPx(),
                        cap = capStyle
                    )
                )
            }
        }
        
        if (showProgress) {
            // Progress text in center
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = progressTextColor,
                fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { progressTextSize.toSp() },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * A fancy version of the circular progress with glow and gradient effects
 */
@Composable
fun GlowingCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    thickness: Dp = 16.dp,
    animationDuration: Int = 1000,
    gradientColors: List<Color> = listOf(
        Color(0xFF6200EA),
        Color(0xFF3700B3),
        Color(0xFF0288D1)
    ),
    glowColor: Color = Color(0x4D3700B3),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    capStyle: StrokeCap = StrokeCap.Round,
    startAngle: Float = 270f,
    showProgress: Boolean = true
) {
    // Constrain progress between 0f and 1f
    val validProgress = progress.coerceIn(0f, 1f)
    
    // Animate the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = validProgress,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )
    
    // Create a rotating angle for the gradient
    val rotationAngle by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )
    
    // Pulse animation for glow effect
    val glowRadius by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = thickness.value * 1.2f,
        targetValue = thickness.value * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .padding(thickness),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Create sweep gradient for progress arc
            val gradientBrush = Brush.sweepGradient(
                colors = gradientColors,
                center = Offset(size.toPx() / 2, size.toPx() / 2)
            )
            
            // Glow effect - draw a blurred circle underneath
            if (animatedProgress > 0.05f) {
                drawCircle(
                    color = glowColor,
                    radius = size.toPx() / 2,
                    style = Stroke(width = glowRadius.dp.toPx()),
                    alpha = 0.5f * animatedProgress,
                    blendMode = BlendMode.SrcOver
                )
            }
            
            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = thickness.toPx(),
                    cap = capStyle
                )
            )
            
            // Foreground progress arc with gradient
            rotate(degrees = rotationAngle) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngle,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(
                        width = thickness.toPx(),
                        cap = capStyle
                    )
                )
            }
        }
        
        if (showProgress) {
            // Progress text in center with pulsing animation
            val textScale by rememberInfiniteTransition(label = "textPulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "textScale"
            )
            
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = MaterialTheme.colorScheme.primary,
                fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { (24.dp * (if (animatedProgress > 0.95f) textScale else 1f)).toSp() },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    scaleX = if (animatedProgress > 0.95f) textScale else 1f
                    scaleY = if (animatedProgress > 0.95f) textScale else 1f
                }
            )
        }
    }
}

/**
 * A segmented circular progress that breaks the progress into discrete segments
 */
@Composable
fun SegmentedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    segments: Int = 12,
    gapDegrees: Float = 4f,
    segmentThickness: Dp = 16.dp,
    animationDuration: Int = 1500,
    segmentColor: Color = MaterialTheme.colorScheme.primary,
    inactiveSegmentColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    capStyle: StrokeCap = StrokeCap.Round
) {
    // Constrain progress between 0f and 1f
    val validProgress = progress.coerceIn(0f, 1f)
    
    // Calculate total angle occupied by segments and gaps
    val totalAngle = 360f - (gapDegrees * segments)
    val segmentAngle = totalAngle / segments
    
    // Calculate number of segments to fill
    val filledSegmentsFloat = validProgress * segments
    val filledSegments = filledSegmentsFloat.toInt()
    val partialSegmentProgress = filledSegmentsFloat - filledSegments
    
    // Animate segments filling up
    val animatedFilledSegments by animateIntAsState(
        targetValue = filledSegments,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "filledSegments"
    )
    
    val animatedPartialProgress by animateFloatAsState(
        targetValue = partialSegmentProgress,
        animationSpec = tween(
            durationMillis = animationDuration / 2,
            easing = FastOutSlowInEasing
        ),
        label = "partialProgress"
    )
    
    // Staggered animation for segments
    val staggeredAnimations = remember { 
        List(segments) { index ->
            Animatable(initialValue = 0f)
        }
    }
    
    // Start staggered animations
    LaunchedEffect(validProgress) {
        staggeredAnimations.forEachIndexed { index, anim ->
            launch {
                val delayDuration = index * (animationDuration / segments / 3L)
                delay(delayDuration)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animationDuration / 2,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .padding(segmentThickness / 2),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // For each segment, calculate its angle and draw it
            for (i in 0 until segments) {
                val startAngle = (i * (segmentAngle + gapDegrees)) - 90f  // Start from top
                
                // Background segment
                drawArc(
                    color = inactiveSegmentColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = false,
                    style = Stroke(
                        width = segmentThickness.toPx(),
                        cap = capStyle
                    )
                )
                
                // Active segments
                if (i < animatedFilledSegments || (i == animatedFilledSegments && animatedPartialProgress > 0f)) {
                    // Full or partial segment
                    val sweepAngle = if (i < animatedFilledSegments) {
                        segmentAngle * staggeredAnimations[i].value
                    } else {
                        segmentAngle * animatedPartialProgress * staggeredAnimations[i].value
                    }
                    
                    drawArc(
                        color = segmentColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = segmentThickness.toPx(),
                            cap = capStyle
                        )
                    )
                }
            }
        }
        
        // Progress text in center
        Text(
            text = "${(validProgress * 100).toInt()}%",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { 24.dp.toSp() },
            fontWeight = FontWeight.Bold
        )
    }
}