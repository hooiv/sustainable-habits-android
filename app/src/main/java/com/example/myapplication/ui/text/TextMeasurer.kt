package com.example.myapplication.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle

/**
 * A class to measure text dimensions
 */
class TextMeasurer(
    private val density: androidx.compose.ui.unit.Density,
    private val fontFamilyResolver: androidx.compose.ui.text.font.FontFamily.Resolver
) {
    /**
     * Measure the size of the given text with the given style
     */
    fun measure(
        text: String,
        style: TextStyle
    ): androidx.compose.ui.text.TextLayoutResult {
        val textLayoutResult = androidx.compose.ui.text.TextMeasurer(
            density = density,
            fontFamilyResolver = fontFamilyResolver
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
    return remember(density, fontFamilyResolver) {
        TextMeasurer(density, fontFamilyResolver)
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
    drawText(
        textLayoutResult = textLayoutResult,
        color = color,
        topLeft = topLeft
    )
}
