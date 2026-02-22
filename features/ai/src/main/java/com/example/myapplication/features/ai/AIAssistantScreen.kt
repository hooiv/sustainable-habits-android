package com.example.myapplication.features.ai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.features.ai.viewmodel.AIAssistantViewModel
import android.content.Context
import android.os.PowerManager
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
    val personalizationSettings by viewModel.personalizationSettings.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Settings state
    var useStreaming by remember { mutableStateOf(personalizationSettings.useStreaming) }
    // Update settings when personalization changes
    LaunchedEffect(personalizationSettings) {
        useStreaming = personalizationSettings.useStreaming
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                            navController.navigate(com.example.myapplication.core.ui.navigation.NavRoutes.AI_ASSISTANT_SETTINGS)
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
                        viewModel.processSuggestion(suggestion, useStreaming)
                    }
                    coroutineScope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                onAskQuestion = { question ->
                    coroutineScope.launch {
                        viewModel.askQuestion(question, useStreaming)
                    }
                    coroutineScope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
            Text(
                text = "You asked: $question",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

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
 * Blinking cursor for streaming text
 */
@Composable
fun BlinkingCursor() {
    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isPowerSaveMode = powerManager.isPowerSaveMode

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPowerSaveMode) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Text(
        text = "▌",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 2.dp)
            .graphicsLayer { this.alpha = alpha }
    )
}

