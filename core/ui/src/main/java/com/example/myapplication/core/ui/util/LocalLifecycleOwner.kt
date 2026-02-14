package com.example.myapplication.core.ui.util

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.LifecycleOwner

/**
 * CompositionLocal containing the current LifecycleOwner
 */
val LocalLifecycleOwner = compositionLocalOf<LifecycleOwner> { 
    error("LocalLifecycleOwner not provided") 
}
