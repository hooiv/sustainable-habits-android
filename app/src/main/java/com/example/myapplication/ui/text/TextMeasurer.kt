package com.example.myapplication.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
// Imports for drawing text
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

/**
 * A class to measure text dimensions
 */
class TextMeasurer(
    private val density: androidx.compose.ui.unit.Density,
    private val fontFamilyResolver: androidx.compose.ui.text.font.FontFamily.Resolver,
    private val layoutDirection: LayoutDirection
) {
    /**
     * Measure the size of the given text with the given style
     */
    fun measure(
        text: String,
        style: TextStyle
    ): androidx.compose.ui.text.TextLayoutResult {
        val textLayoutResult = androidx.compose.ui.text.TextMeasurer(
            defaultDensity = density, // Corrected parameter name
            defaultFontFamilyResolver = fontFamilyResolver, // Corrected parameter name
            defaultLayoutDirection = layoutDirection // Added parameter
        ).measure(
            text = text,
            style = style
        )
        return textLayoutResult
    }
}

/**
 * Remember a TextMeasurer instance
 */
@Composable
fun rememberTextMeasurer(): TextMeasurer {
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val layoutDirection = LocalLayoutDirection.current
    return remember(density, fontFamilyResolver, layoutDirection) {
        TextMeasurer(density, fontFamilyResolver, layoutDirection)
    }
}

/**
 * Extension function to draw text in a DrawScope
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawText(
    text: String,
    topLeft: androidx.compose.ui.geometry.Offset,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black
) {
    val textLayoutResult = textMeasurer.measure(text, style)
    // Draw the text using the Canvas API
    drawContext.canvas.nativeCanvas.drawText(
        text,
        topLeft.x,
        topLeft.y + textLayoutResult.size.height,
        android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = style.fontSize.toPx()
        }
    )
}
