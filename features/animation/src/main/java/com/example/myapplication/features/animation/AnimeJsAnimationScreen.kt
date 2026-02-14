package com.example.myapplication.features.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.ui.components.AppBar
import kotlinx.coroutines.launch

/**
 * Anime.js Animation Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeJsAnimationScreen(
    navController: NavController,
    viewModel: AnimeJsAnimationViewModel = hiltViewModel()
) {
    val isReady by viewModel.isReady.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentAnimation by viewModel.currentAnimation.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            AppBar(
                title = "Anime.js Animations",
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Animation container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF121212), RoundedCornerShape(16.dp))
            ) {
                // WebView
                AnimeJsWebView(
                    integration = viewModel.animeJsIntegration,
                    onReady = {
                        coroutineScope.launch {
                            viewModel.onWebViewReady()
                        }
                    }
                )
                
                // Loading indicator
                if (!isReady) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Error message
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Animation controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Animation Types",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Animation type buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimationTypeButton(
                        type = AnimationType.DEFAULT,
                        icon = Icons.Default.Animation,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.DEFAULT) }
                    )
                    
                    AnimationTypeButton(
                        type = AnimationType.STAGGER,
                        icon = Icons.Default.GridView,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.STAGGER) }
                    )
                    
                    AnimationTypeButton(
                        type = AnimationType.TEXT,
                        icon = Icons.Default.TextFields,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.TEXT) }
                    )
                    
                    AnimationTypeButton(
                        type = AnimationType.HABITS,
                        icon = Icons.Default.ViewList,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.HABITS) }
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimationTypeButton(
                        type = AnimationType.PROGRESS,
                        icon = Icons.Default.PieChart,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.PROGRESS) }
                    )
                    
                    AnimationTypeButton(
                        type = AnimationType.PULSE,
                        icon = Icons.Default.RadioButtonChecked,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.PULSE) }
                    )
                    
                    AnimationTypeButton(
                        type = AnimationType.ROTATE,
                        icon = Icons.Default.Refresh,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.ROTATE) }
                    )
                    
                    AnimationTypeButton(
                        type = AnimationType.FADE,
                        icon = Icons.Default.Opacity,
                        currentType = currentAnimation,
                        onClick = { viewModel.setAnimationType(AnimationType.FADE) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Special animation buttons
                Text(
                    text = "Special Animations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.animateHabits() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Animate Habits"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Habits")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { viewModel.animateProgress() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = "Animate Progress"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Progress")
                    }
                }
            }
        }
    }
}

/**
 * Animation type button
 */
@Composable
fun AnimationTypeButton(
    type: AnimationType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentType: AnimationType,
    onClick: () -> Unit
) {
    val isSelected = type == currentType
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
