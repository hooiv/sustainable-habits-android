package com.example.myapplication.features.ai.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.model.AISuggestion
import com.example.myapplication.core.data.model.SuggestionType
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)


/**
 * A component that displays an AI assistant with animated avatar and suggestions
 */
@Composable
fun AIAssistantCard(
    modifier: Modifier = Modifier,
    suggestions: List<AISuggestion> = emptyList(),
    onSuggestionClick: (AISuggestion) -> Unit = {},
    onAskQuestion: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var userQuestion by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // AI Assistant header with avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated AI avatar
                AIAnimatedAvatar(
                    modifier = Modifier.size(60.dp),
                    isThinking = isThinking
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Assistant info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Habit AI Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isThinking) "Thinking..." else "How can I help you today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Expand/collapse button
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // AI suggestions
                if (suggestions.isNotEmpty()) {
                    Text(
                        text = "Personalized Suggestions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            AISuggestionItem(
                                suggestion = suggestion,
                                onClick = { onSuggestionClick(suggestion) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Ask AI section
                Text(
                    text = "Ask AI Assistant",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text field for question
                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        placeholder = { Text("Type your question...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send button
                    Button(
                        onClick = {
                            if (userQuestion.isNotBlank()) {
                                isThinking = true
                                coroutineScope.launch {
                                    onAskQuestion(userQuestion)
                                    delay(2000) // Simulate AI thinking
                                    userQuestion = ""
                                    isThinking = false
                                }
                            }
                        },
                        enabled = userQuestion.isNotBlank() && !isThinking
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }

                // Quick suggestions
                @OptIn(ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSuggestionChip(
                        text = "Suggest a new habit",
                        onClick = {
                            isThinking = true
                            coroutineScope.launch {
                                onAskQuestion("Suggest a new habit for me")
                                delay(2000) // Simulate AI thinking
                                isThinking = false
                            }
                        }
                    )

                    QuickSuggestionChip(
                        text = "Optimize my schedule",
                        onClick = {
                            isThinking = true
                            coroutineScope.launch {
                                onAskQuestion("Help me optimize my habit schedule")
                                delay(2000) // Simulate AI thinking
                                isThinking = false
                            }
                        }
                    )

                    QuickSuggestionChip(
                        text = "Improve motivation",
                        onClick = {
                            isThinking = true
                            coroutineScope.launch {
                                onAskQuestion("How can I stay motivated?")
                                delay(2000) // Simulate AI thinking
                                isThinking = false
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Animated AI avatar with thinking animation
 */
@Composable
fun AIAnimatedAvatar(
    modifier: Modifier = Modifier,
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarAnimation")

    // Pulse animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarScale"
    )

    // Color animation
    val colorTransition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorTransition"
    )

    // Thinking animation
    val thinkingAlpha by animateFloatAsState(
        targetValue = if (isThinking) 1f else 0f,
        animationSpec = tween(500),
        label = "thinkingAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Main avatar circle with gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(if (isThinking) scale else 1f)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(
                            cos(colorTransition * 2 * PI.toFloat()).toFloat() * 100f + 100f,
                            sin(colorTransition * 2 * PI.toFloat()).toFloat() * 100f + 100f
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // AI icon
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "AI Assistant",
            tint = Color.White,
            modifier = Modifier
                .size(30.dp)
                .alpha(1f - thinkingAlpha)
        )

        // Thinking animation
        if (isThinking) {
            ParticleSystem(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(thinkingAlpha),
                particleCount = 20,
                particleColor = Color.White,
                particleSize = 2.dp,
                maxSpeed = 0.5f,
                particleShape = ParticleShape.CIRCLE,
                particleEffect = ParticleEffect.VORTEX,
                glowEffect = true
            )
        }
    }
}

/**
 * Individual AI suggestion item
 */
@Composable
fun AISuggestionItem(
    suggestion: AISuggestion,
    onClick: () -> Unit
) {
    val confidenceColor = when {
        suggestion.confidence > 0.8f -> MaterialTheme.colorScheme.primary
        suggestion.confidence > 0.5f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Suggestion type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(confidenceColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (suggestion.type) {
                        SuggestionType.NEW_HABIT -> Icons.Default.AddCircle
                        SuggestionType.HABIT_IMPROVEMENT -> Icons.Default.Upgrade
                        SuggestionType.STREAK_PROTECTION -> Icons.Default.Shield
                        SuggestionType.SCHEDULE_OPTIMIZATION -> Icons.Default.Schedule
                        SuggestionType.HABIT_CHAIN -> Icons.Default.Link
                        SuggestionType.MOTIVATION -> Icons.Default.EmojiEvents
                        SuggestionType.INSIGHT -> Icons.Default.Lightbulb
                    },
                    contentDescription = suggestion.type.name,
                    tint = confidenceColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Suggestion content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            // Confidence indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(confidenceColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(suggestion.confidence * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Quick suggestion chip for common AI queries
 */
@Composable
fun QuickSuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
