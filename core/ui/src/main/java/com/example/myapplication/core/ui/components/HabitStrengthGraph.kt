package com.example.myapplication.core.ui.components

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun HabitStrengthGraph(
    dataPoints: List<Float>, // 0.0 to 1.0 values
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    if (dataPoints.isEmpty()) return

    // Animation for drawing the path
    val progress = remember { Animatable(0f) }
    
    // State for touch interaction
    var touchedIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(dataPoints) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        )
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            // Calculate nearest index based on width
                            val stepX = size.width / (dataPoints.size - 1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, dataPoints.lastIndex)
                            touchedIndex = index
                            tryAwaitRelease()
                            touchedIndex = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (dataPoints.size - 1)

            // 1. Calculate Points
            val points = dataPoints.mapIndexed { index, ratio ->
                val x = index * stepX
                val y = height - (ratio * height)
                PointF(x, y)
            }

            // 2. Create Smooth Path (Cubic Bezier)
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        
                        // Control points for smooth curve
                        val controlPoint1 = PointF((p0.x + p1.x) / 2, p0.y)
                        val controlPoint2 = PointF((p0.x + p1.x) / 2, p1.y)
                        
                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    }
                }
            }

            // 3. Create Fill Path
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            // 4. Draw with Clip (Animation)
            clipRect(right = width * progress.value) {
                // Draw Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw Line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            
            // 5. Draw Touch Highlight
            touchedIndex?.let { index ->
                val point = points[index]
                val value = dataPoints[index]
                
                // Vertical Line
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(point.x, 0f),
                    end = Offset(point.x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Data Point Circle
                drawCircle(
                    color = lineColor,
                    radius = 6.dp.toPx(),
                    center = Offset(point.x, point.y)
                )
                
                // Tooltip
                val label = "${(value * 100).toInt()}%"
                val textLayoutResult = textMeasurer.measure(label, style = labelStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = (point.x - textLayoutResult.size.width / 2).coerceIn(0f, width - textLayoutResult.size.width),
                        y = (point.y - 30.dp.toPx()).coerceAtLeast(0f)
                    )
                )
            }
        }
    }
}
