package com.example.myapplication.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A wrapper around the standard Icon composable with some default styling
 */
@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
