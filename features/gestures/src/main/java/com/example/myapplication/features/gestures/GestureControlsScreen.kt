package com.example.myapplication.features.gestures

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen for gesture controls configuration and demonstration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureControlsScreen(
    navController: NavController,
    viewModel: GestureControlsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // State
    var selectedTab by remember { mutableStateOf(0) }
    var showGestureDemo by remember { mutableStateOf(false) }
    var showRadialMenu by remember { mutableStateOf(false) }
    var showSwipeArea by remember { mutableStateOf(false) }
    var showCustomGestures by remember { mutableStateOf(false) }

    // Sample gesture actions
    val gestureActions = remember {
        listOf(
            GestureAction(
                name = "Complete Habit",
                description = "Complete the current habit",
                icon = Icons.Default.Check,
                action = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        viewModel.showToast("Habit completed")
                        delay(500)
                    }
                }
            ),
            GestureAction(
                name = "Skip Habit",
                description = "Skip the current habit",
                icon = Icons.Default.SkipNext,
                action = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        viewModel.showToast("Habit skipped")
                        delay(500)
                    }
                }
            ),
            GestureAction(
                name = "Add Note",
                description = "Add a note to the current habit",
                icon = Icons.Default.Edit,
                action = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        viewModel.showToast("Adding note")
                        delay(500)
                    }
                }
            ),
            GestureAction(
                name = "Set Reminder",
                description = "Set a reminder for the current habit",
                icon = Icons.Default.Alarm,
                action = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        viewModel.showToast("Setting reminder")
                        delay(500)
                    }
                }
            ),
            GestureAction(
                name = "View Stats",
                description = "View stats for the current habit",
                icon = Icons.Default.BarChart,
                action = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        viewModel.showToast("Viewing stats")
                        delay(500)
                    }
                }
            )
        )
    }

    // Sample swipe actions
    val swipeActions = remember {
        mapOf(
            "Left" to {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.showToast("Swiped Left: Skip habit")
            },
            "Right" to {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.showToast("Swiped Right: Complete habit")
            },
            "Up" to {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.showToast("Swiped Up: View details")
            },
            "Down" to {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.showToast("Swiped Down: Hide habit")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gesture Controls") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = "Gestures") },
                    label = { Text("Gestures") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Customize") },
                    label = { Text("Customize") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    // Gestures Demo Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Gesture Controls",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Explore and test different gesture controls to interact with your habits more efficiently.",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            GestureCard(
                                title = "Radial Menu",
                                description = "Long press to open a radial menu with quick actions",
                                icon = Icons.Default.RadioButtonChecked,
                                isActive = showRadialMenu,
                                onToggle = { showRadialMenu = it }
                            )
                        }

                        item {
                            GestureCard(
                                title = "Swipe Gestures",
                                description = "Swipe in different directions to perform actions",
                                icon = Icons.Default.SwipeRight,
                                isActive = showSwipeArea,
                                onToggle = { showSwipeArea = it }
                            )
                        }

                        item {
                            GestureCard(
                                title = "Custom Gestures",
                                description = "Create and use custom gesture patterns",
                                icon = Icons.Default.Gesture,
                                isActive = showCustomGestures,
                                onToggle = { showCustomGestures = it }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Gesture Demo Area",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            // Demo area
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Radial Menu Demo
                                    if (showRadialMenu) {
                                        RadialGestureMenu(
                                            actions = gestureActions,
                                            onActionSelected = { action ->
                                                // Action is handled in the action's lambda
                                            }
                                        )
                                    }

                                    // Swipe Area Demo
                                    if (showSwipeArea) {
                                        SwipeGestureArea(
                                            onSwipeLeft = swipeActions["Left"] ?: {},
                                            onSwipeRight = swipeActions["Right"] ?: {},
                                            onSwipeUp = swipeActions["Up"] ?: {},
                                            onSwipeDown = swipeActions["Down"] ?: {}
                                        ) {

                                        }
                                    }

                                    // Custom Gestures Demo
                                    if (showCustomGestures) {
                                        CustomGestureRecognizer()
                                    }

                                    // Default state
                                    if (!showRadialMenu && !showSwipeArea && !showCustomGestures) {
                                        Text(
                                            text = "Select a gesture type above to try it out",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Settings Tab
                    GestureSettingsTab()
                }
                2 -> {
                    // Customize Tab
                    GestureCustomizationTab(gestureActions)
                }
            }
        }
    }
}

/**
 * Card for a gesture type with toggle
 */
@Composable
fun GestureCard(
    title: String,
    description: String,
    icon: ImageVector,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isActive) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = isActive,
                onCheckedChange = { onToggle(it) }
            )
        }
    }
}

/**
 * Settings tab for gesture controls
 */
@Composable
fun GestureSettingsTab() {
    var gestureEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var gestureSpeed by remember { mutableStateOf(0.5f) }
    var gestureSensitivity by remember { mutableStateOf(0.7f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gesture Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSwitch(
                title = "Enable Gestures",
                description = "Turn on/off all gesture controls",
                checked = gestureEnabled,
                onCheckedChange = { gestureEnabled = it }
            )
        }

        item {
            SettingsSwitch(
                title = "Haptic Feedback",
                description = "Vibrate when gestures are recognized",
                checked = hapticFeedbackEnabled,
                onCheckedChange = { hapticFeedbackEnabled = it }
            )
        }

        item {
            Text(
                text = "Gesture Speed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Slider(
                value = gestureSpeed,
                onValueChange = { gestureSpeed = it },
                valueRange = 0f..1f,
                steps = 10,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Slow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Fast",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        item {
            Text(
                text = "Gesture Sensitivity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Slider(
                value = gestureSensitivity,
                onValueChange = { gestureSensitivity = it },
                valueRange = 0f..1f,
                steps = 10,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Low",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "High",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Settings switch component
 */
@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Customization tab for gesture controls
 */
@Composable
fun GestureCustomizationTab(gestureActions: List<GestureAction>) {
    var editingAction by remember { mutableStateOf<GestureAction?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Customize Gestures",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Assign actions to different gestures and customize their behavior",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(gestureActions) { action ->
            ActionAssignmentCard(
                action = action,
                onEdit = { editingAction = action }
            )
        }

        item {
            Button(
                onClick = { /* Add new gesture action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Add Custom Gesture")
            }
        }
    }

    // Edit dialog
    if (editingAction != null) {
        AlertDialog(
            onDismissRequest = { editingAction = null },
            title = { Text("Edit Gesture Action") },
            text = {
                Column {
                    Text("Customize the action for this gesture")
                    Spacer(modifier = Modifier.height(16.dp))
                    // Action editing controls would go here
                }
            },
            confirmButton = {
                TextButton(onClick = { editingAction = null }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Card for assigning actions to gestures
 */
@Composable
fun ActionAssignmentCard(
    action: GestureAction,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = action.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
}

/**
 * Custom gesture recognizer component
 */
@Composable
fun CustomGestureRecognizer() {
    val hapticFeedback = LocalHapticFeedback.current
    var gesturePoints by remember { mutableStateOf(listOf<Offset>()) }
    var currentGesture by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.05f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        gesturePoints = listOf(it)
                        currentGesture = null
                    },
                    onDragEnd = {
                        // Analyze the gesture
                        if (gesturePoints.size > 5) {
                            val gesture = analyzeGesture(gesturePoints)
                            currentGesture = gesture
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDrag = { change, _ ->
                        gesturePoints = gesturePoints + change.position
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Draw the gesture path
        if (gesturePoints.isNotEmpty()) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Drawing code would go here
            }
        }

        // Show recognized gesture
        AnimatedVisibility(
            visible = currentGesture != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            currentGesture?.let {
                Card(
                    modifier = Modifier
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Recognized: $it",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Instructions
        if (gesturePoints.isEmpty() && currentGesture == null) {
            Text(
                text = "Draw a gesture pattern here",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Analyze a gesture pattern
 */
private fun analyzeGesture(points: List<Offset>): String {
    // This would contain actual gesture recognition logic
    // For demo purposes, we'll return a random gesture
    return listOf("Circle", "Square", "Triangle", "Zigzag", "Letter S").random()
}
