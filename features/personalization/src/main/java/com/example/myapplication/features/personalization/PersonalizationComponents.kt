package com.example.myapplication.features.personalization

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

/**
 * Data class representing a theme
 */
data class AppTheme(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val isDark: Boolean = false,
    val fontScale: Float = 1f,
    val animationSpeed: Float = 1f,
    val useSystemTheme: Boolean = false
)

/**
 * Data class representing a user preference
 */
data class UserPreference(
    val key: String,
    val value: Any,
    val type: PreferenceType
)

/**
 * Enum for different types of preferences
 */
enum class PreferenceType {
    BOOLEAN,
    INTEGER,
    FLOAT,
    STRING,
    COLOR,
    ENUM
}

/**
 * A component that displays a theme selector with previews
 */
@Composable
fun ThemeSelector(
    themes: List<AppTheme>,
    currentThemeId: String,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose Theme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(themes) { theme ->
                ThemePreviewCard(
                    theme = theme,
                    isSelected = theme.id == currentThemeId,
                    onClick = { onThemeSelected(theme) }
                )
            }
        }
    }
}

/**
 * Individual theme preview card
 */
@Composable
fun ThemePreviewCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(240.dp)
            .scale(cardScale)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backgroundColor)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Theme name
            Text(
                text = theme.name,
                color = if (theme.isDark) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Color palette preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorCircle(color = theme.primaryColor)
                ColorCircle(color = theme.secondaryColor)
                ColorCircle(color = theme.tertiaryColor)
            }
            
            // UI element previews
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Button",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Card Preview",
                    color = if (theme.isDark) Color.White else Color.Black,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Selected indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Simple color circle for theme preview
 */
@Composable
fun ColorCircle(color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

/**
 * A component that allows customizing animation speed
 */
@Composable
fun AnimationSpeedCustomizer(
    currentSpeed: Float,
    onSpeedChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var animationVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Animation Speed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Speed slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SlowMotionVideo,
                contentDescription = "Slow",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Slider(
                value = currentSpeed,
                onValueChange = onSpeedChanged,
                valueRange = 0.5f..2f,
                steps = 5,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Fast",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = when {
                currentSpeed < 0.75f -> "Slow"
                currentSpeed < 1.25f -> "Normal"
                currentSpeed < 1.75f -> "Fast"
                else -> "Very Fast"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        // Preview button
        Button(
            onClick = {
                animationVisible = true
                coroutineScope.launch {
                    delay(3000)
                    animationVisible = false
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text("Preview Animation")
        }
        
        // Animation preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (animationVisible) {
                val animDuration = (1000 / currentSpeed).toInt().coerceAtLeast(100)
                
                val infiniteTransition = rememberInfiniteTransition(label = "preview")
                val translateAnim by infiniteTransition.animateFloat(
                    initialValue = -100f,
                    targetValue = 100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = animDuration,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "translate"
                )
                
                val scaleAnim by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = animDuration,
                            easing = AnimeEasing.EaseInOutQuad
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                
                Box(
                    modifier = Modifier
                        .offset(x = translateAnim.dp)
                        .scale(scaleAnim)
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Text(
                    text = "Click Preview to see animation",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * A component that allows customizing font size
 */
@Composable
fun FontSizeCustomizer(
    currentScale: Float,
    onScaleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Font Size",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Font size slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "A",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Slider(
                value = currentScale,
                onValueChange = onScaleChanged,
                valueRange = 0.8f..1.4f,
                steps = 3,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            
            Text(
                text = "A",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Preview text with different sizes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Heading Example",
                fontSize = (20 * currentScale).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Subheading example text",
                fontSize = (16 * currentScale).sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "This is an example of body text with the selected font size. You can see how it looks with your current settings.",
                fontSize = (14 * currentScale).sp,
                lineHeight = (20 * currentScale).sp
            )
        }
    }
}

/**
 * A component that allows customizing notification preferences
 */
@Composable
fun NotificationPreferences(
    reminderEnabled: Boolean,
    onReminderEnabledChanged: (Boolean) -> Unit,
    achievementNotificationsEnabled: Boolean,
    onAchievementNotificationsEnabledChanged: (Boolean) -> Unit,
    friendActivityNotificationsEnabled: Boolean,
    onFriendActivityNotificationsEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Notification Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Habit reminders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Habit Reminders",
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Receive notifications for your scheduled habits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = reminderEnabled,
                onCheckedChange = onReminderEnabledChanged
            )
        }
        
        // Achievement notifications
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Achievement Alerts",
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Get notified when you earn badges and achievements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = achievementNotificationsEnabled,
                onCheckedChange = onAchievementNotificationsEnabledChanged
            )
        }
        
        // Friend activity notifications
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Friend Activity",
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Get notified about your friends' progress and challenges",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = friendActivityNotificationsEnabled,
                onCheckedChange = onFriendActivityNotificationsEnabledChanged
            )
        }
    }
}
