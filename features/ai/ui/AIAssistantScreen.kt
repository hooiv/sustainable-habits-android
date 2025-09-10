package com.example.myapplication.features.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AI Assistant Screen
 * Provides an AI assistant to help users with habit formation and tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: AIAssistantViewModel = hiltViewModel()
) {
    val suggestions by viewModel.suggestions.collectAsState()
    val responses by viewModel.responses.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamingResponse by viewModel.streamingResponse.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val voiceInputText by viewModel.voiceInputText.collectAsState()
    val voiceAmplitude by viewModel.voiceAmplitude.collectAsState()
    val personalizationSettings by viewModel.personalizationSettings.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Settings state
    var useStreaming by remember { mutableStateOf(personalizationSettings.useStreaming) }
    var useVoice by remember { mutableStateOf(personalizationSettings.useVoice) }

    // Update settings when personalization changes
    LaunchedEffect(personalizationSettings) {
        useStreaming = personalizationSettings.useStreaming
        useVoice = personalizationSettings.useVoice
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Voice toggle
                    IconButton(
                        onClick = {
                            val newValue = !useVoice
                            useVoice = newValue
                            // Save the setting
                            viewModel.savePersonalizationSettings(
                                personalizationSettings.copy(useVoice = newValue)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (useVoice) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (useVoice) "Disable Voice" else "Enable Voice",
                            tint = if (useVoice) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Streaming toggle
                    Switch(
                        checked = useStreaming,
                        onCheckedChange = { newValue ->
                            useStreaming = newValue
                            // Save the setting
                            viewModel.savePersonalizationSettings(
                                personalizationSettings.copy(useStreaming = newValue)
                            )
                        },
                        thumbContent = {
                            Text(
                                text = if (useStreaming) "S" else "B",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    )

                    // Settings button
                    IconButton(
                        onClick = {
                            navController.navigate(com.example.myapplication.navigation.NavRoutes.AI_ASSISTANT_SETTINGS)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI Assistant Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (useVoice) {
                VoiceInputButton(
                    isListening = isListening,
                    voiceAmplitude = voiceAmplitude,
                    voiceInputText = voiceInputText,
                    continuous = false, // Set to true for continuous listening
                    useWakeWord = false, // Set to true to use wake words
                    onStartListening = {
                        viewModel.startVoiceInput(
                            continuous = false,
                            useWakeWord = false
                        )
                    },
                    onStopListening = {
                        viewModel.stopVoiceInput()
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI Assistant Card
            AIAssistantCard(
                suggestions = suggestions,
                onSuggestionClick = { suggestion ->
                    coroutineScope.launch {
                        viewModel.processSuggestion(suggestion, useStreaming, useVoice)
                        // Auto-scroll to bottom when processing starts
                        coroutineScope.launch {
                            delay(100)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                },
                onAskQuestion = { question ->
                    coroutineScope.launch {
                        viewModel.askQuestion(question, useStreaming, useVoice)
                        // Auto-scroll to bottom when processing starts
                        coroutineScope.launch {
                            delay(100)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Voice input display
            if (isListening || voiceInputText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isListening) "Listening..." else "Voice Input",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            if (isListening) {
                                Spacer(modifier = Modifier.width(8.dp))

                                // Animated dots for listening indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(3) { index ->
                                        val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                                        val alpha by infiniteTransition.animateFloat(
                                            initialValue = 0.2f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(500, delayMillis = index * 150),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "dotAlpha$index"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .size(6.dp)
                                                .alpha(alpha)
                                                .background(
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Voice amplitude visualization
                        if (isListening && voiceAmplitude > 0) {
                            Row(
                                modifier = Modifier
                                    .height(24.dp)
                                    .fillMaxWidth(0.7f)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(7) { index ->
                                    val barHeight = if (index % 2 == 0) {
                                        (voiceAmplitude * 20).coerceIn(3f, 20f)
                                    } else {
                                        (voiceAmplitude * 15).coerceIn(2f, 15f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .width(4.dp)
                                            .height(barHeight.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                    alpha = 0.5f + (voiceAmplitude * 0.5f)
                                                ),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = voiceInputText.ifEmpty { "Speak now..." },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Streaming response
            if (isStreaming) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AI is responding...",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = streamingResponse.ifEmpty { "..." },
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Blinking cursor
                        if (streamingResponse.isNotEmpty()) {
                            BlinkingCursor()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Responses section
            if (responses.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AI Responses",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        responses.forEach { response ->
                            ResponseItem(
                                question = response.first,
                                answer = response.second,
                                isSpeaking = isSpeaking && responses.last() == response && useVoice,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (isProcessing && !isStreaming) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI is thinking...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Add extra space at the bottom for better scrolling
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Auto-scroll to bottom when streaming or processing
    LaunchedEffect(isStreaming, isProcessing, responses.size) {
        if (isStreaming || isProcessing || responses.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
}

/**
 * Response item component
 */
@Composable
fun ResponseItem(
    question: String,
    answer: String,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "You asked: $question",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )

                if (isSpeaking) {
                    SpeakingIndicator()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = answer,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Voice input button with amplitude visualization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputButton(
    isListening: Boolean,
    voiceAmplitude: Float = 0f,
    voiceInputText: String = "",
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    continuous: Boolean = false,
    useWakeWord: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isListening) 72.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "size"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "color"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isListening) 8.dp else 4.dp,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )

    // Amplitude-based animation
    val amplitudeRippleSize = if (isListening) {
        // Scale amplitude (0-1) to a reasonable ripple size
        0.5f + (voiceAmplitude * 0.5f)
    } else {
        0f
    }

    // Background ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val backgroundRippleSize by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple size"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Voice input text display
        if (isListening && voiceInputText.isNotEmpty()) {
            Text(
                text = voiceInputText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(0.8f)
            )
        }

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Background ripple effect when listening
            if (isListening) {
                Canvas(
                    modifier = Modifier
                        .size(animatedSize * 2.5f)
                        .alpha((1f - backgroundRippleSize) * 0.2f)
                ) {
                    drawCircle(
                        color = animatedColor,
                        radius = size.minDimension / 2 * backgroundRippleSize,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Amplitude-based ripple
            if (isListening && voiceAmplitude > 0.1f) {
                Canvas(
                    modifier = Modifier
                        .size(animatedSize * 2f)
                        .alpha(voiceAmplitude * 0.5f)
                ) {
                    drawCircle(
                        color = animatedColor,
                        radius = size.minDimension / 2 * amplitudeRippleSize,
                        style = Stroke(width = 3.dp.toPx())
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
                containerColor = animatedColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = animatedElevation
                ),
                modifier = Modifier.size(animatedSize)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Rounded.Close else Icons.Rounded.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start listening",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Mode indicators
        if (continuous || useWakeWord) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (continuous) {
                    Surface(
                        modifier = Modifier.padding(end = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Continuous",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (useWakeWord) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Wake Word",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Blinking cursor for streaming text
 */
@Composable
fun BlinkingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Text(
        text = "▌",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = Modifier.padding(start = 2.dp)
    )
}

/**
 * Speaking indicator animation
 */
@Composable
fun SpeakingIndicator() {
    // Extract color outside of Canvas to avoid @Composable invocation error
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Animated sound waves
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Multiple circles with different animation phases
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "wave$index")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = index * 300, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "scale$index"
                )

                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = index * 300, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha$index"
                )

                Canvas(
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(alpha)
                ) {
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2 * scale,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Center icon
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Speaking",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Animated dots
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, delayMillis = index * 150, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dotScale$index"
                )

                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .scale(scale)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
