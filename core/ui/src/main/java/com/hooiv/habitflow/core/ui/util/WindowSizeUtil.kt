package com.hooiv.habitflow.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Compact  = phone portrait          (< 600 dp)
 * Medium   = phone landscape/foldable (600–840 dp)
 * Expanded = tablet/desktop          (≥ 840 dp)
 *
 * Mirrors the Material3 WindowWidthSizeClass breakpoints without needing
 * the optional `material3-window-size-class` artifact.
 */
enum class WindowWidthSizeClass { Compact, Medium, Expanded }

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return remember(screenWidthDp) {
        when {
            screenWidthDp < 600  -> WindowWidthSizeClass.Compact
            screenWidthDp < 840  -> WindowWidthSizeClass.Medium
            else                 -> WindowWidthSizeClass.Expanded
        }
    }
}

/**
 * Returns the maximum content width that should be used for form/detail screens.
 *
 * - Compact  : fills all available width (no cap)
 * - Medium   : capped at 560 dp (keeps readable line length on foldables)
 * - Expanded : capped at 720 dp (wide tablets; 2-column layout would be better
 *              but a single centred column is still far better than edge-to-edge)
 */
fun WindowWidthSizeClass.maxContentWidth(): Dp = when (this) {
    WindowWidthSizeClass.Compact  -> Dp.Infinity   // use fillMaxWidth()
    WindowWidthSizeClass.Medium   -> 560.dp
    WindowWidthSizeClass.Expanded -> 720.dp
}
