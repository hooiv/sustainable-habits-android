package com.example.myapplication.core.ui.util

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/**
 * Keyboard options for text input
 */
object KeyboardOptions {
    /**
     * Default keyboard options
     */
    val Default = androidx.compose.foundation.text.KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = true,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default
    )

    /**
     * Keyboard options for email input
     */
    val Email = androidx.compose.foundation.text.KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next
    )

    /**
     * Keyboard options for password input
     */
    val Password = androidx.compose.foundation.text.KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done
    )

    /**
     * Keyboard options for number input
     */
    val Number = androidx.compose.foundation.text.KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    )

    /**
     * Keyboard options for phone input
     */
    val Phone = androidx.compose.foundation.text.KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Next
    )
}
