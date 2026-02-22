package com.example.myapplication.features.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
// Using the custom blur implementation from BlurUtils.kt via glowEffect extension
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Displays a user's level and experience progress with animations
 */
@Composable
fun ExperienceBar(
    currentXp: Int,
    maxXp: Int,
    level: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onLevelUp: () -> Unit = {}
) {
    var animatedXp by remember { mutableStateOf(0) }
    var showLevelUpEffect by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Animate XP changes
    LaunchedEffect(currentXp) {
        if (animatedXp > 0 && currentXp < animatedXp) {
            // Reset on level up
            animatedXp = 0
            delay(300)
        }

        val startXp = animatedXp
        val targetXp = currentXp

        if (startXp < targetXp) {
            val duration = 1000 // ms
            val startTime = System.currentTimeMillis()

            while (animatedXp < targetXp) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val progress = (elapsedTime / duration.toFloat()).coerceIn(0f, 1f)

                // Use easing for smoother animation
                val easedProgress = androidx.compose.animation.core.EaseOutQuart.transform(progress)
                animatedXp = startXp + ((targetXp - startXp) * easedProgress).toInt()

                if (animatedXp >= maxXp) {
                    showLevelUpEffect = true
                    onLevelUp()
                    break
                }

                delay(16) // ~60fps
            }

            animatedXp = targetXp
        }
    }

    // Level up animation
    val levelUpScale by animateFloatAsState(
        targetValue = if (showLevelUpEffect) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 500,
            easing = androidx.compose.animation.core.EaseOutBack
        ),
        finishedListener = {
            if (showLevelUpEffect) {
                coroutineScope.launch {
                    delay(500)
                    showLevelUpEffect = false
                }
            }
        },
        label = "levelUpScale"
    )

    // Progress calculation
    val progress = (animatedXp.toFloat() / maxXp).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Level indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(levelUpScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showLevelUpEffect) {
                // Add particle effect for level up
                ParticleSystem(
                    modifier = Modifier.matchParentSize(),
                    particleCount = 20,
                    particleColor = Color.White,
                    particleShape = ParticleShape.STAR,
                    glowEffect = true
                )
            }

            Text(
                text = level.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // XP Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // XP Text
            Text(
                text = "$animatedXp / $maxXp XP",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Displays a badge/achievement with animation effects
 */
@Composable
fun AchievementBadge(
    title: String,
    description: String,
    isUnlocked: Boolean,
    iconVector: ImageVector = Icons.Default.EmojiEvents,
    modifier: Modifier = Modifier,
    onBadgeClick: () -> Unit = {}
) {
    val badgeColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    // Animation for unlocked badges
    val infiniteTransition = rememberInfiniteTransition(label = "badgeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = androidx.compose.animation.core.EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeRotation"
    )

    Box(
        modifier = modifier
            .clickable(onClick = onBadgeClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect for unlocked badges
        if (isUnlocked) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .alpha(glowAlpha)
                    .glowEffect(8.dp, Color(0xFFFFD700))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                badgeColor,
                                badgeColor.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Badge container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(100.dp)
        ) {
            // Badge icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        if (isUnlocked) {
                            rotationZ = rotation
                        }
                    }
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isUnlocked) {
                                listOf(
                                    badgeColor,
                                    badgeColor.copy(alpha = 0.7f)
                                )
                            } else {
                                listOf(
                                    badgeColor,
                                    badgeColor.copy(alpha = 0.5f)
                                )
                            }
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = if (isUnlocked) {
                                listOf(
                                    Color.White,
                                    Color.Yellow,
                                    Color.White
                                )
                            } else {
                                listOf(
                                    Color.Gray.copy(alpha = 0.5f),
                                    Color.Gray.copy(alpha = 0.7f)
                                )
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = if (isUnlocked) Color.White else Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )

                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.7f)
                            .background(Color.Black.copy(alpha = 0.5f))
                    )

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badge title
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else Color.Gray,
                textAlign = TextAlign.Center
            )

            // Badge description
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Displays an animation when a badge is unlocked
 */
@Composable
fun BadgeUnlockAnimation(
    badge: Badge,
    xpAwarded: Int = 0,
    onAnimationComplete: () -> Unit = {}
) {
    var animationState by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Animation states:
    // 0 - Initial state
    // 1 - Badge appears
    // 2 - Badge grows
    // 3 - XP appears
    // 4 - Final state

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = when (animationState) {
            0 -> 0.1f
            1 -> 0.5f
            2, 3, 4 -> 1.2f
            else -> 1.0f
        },
        animationSpec = tween(
            durationMillis = 500,
            easing = androidx.compose.animation.core.EaseOutBack
        ),
        label = "badgeScale"
    )

    // Alpha animation
    val alpha by animateFloatAsState(
        targetValue = when (animationState) {
            0 -> 0f
            1, 2, 3, 4 -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.LinearEasing
        ),
        label = "badgeAlpha"
    )

    // XP text alpha
    val xpAlpha by animateFloatAsState(
        targetValue = when (animationState) {
            0, 1, 2 -> 0f
            3, 4 -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.LinearEasing
        ),
        label = "xpAlpha"
    )

    // Start animation sequence
    LaunchedEffect(Unit) {
        animationState = 1
        delay(500)
        animationState = 2
        delay(500)
        animationState = 3
        delay(1500)
        animationState = 4
        delay(500)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Badge container
        Column(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .padding(32.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .glowEffect(16.dp, Color(0xFFFFD700)),
                contentAlignment = Alignment.Center
            ) {
                // Particle effect
                ParticleSystem(
                    modifier = Modifier.matchParentSize(),
                    particleCount = 30,
                    particleColor = Color.White,
                    particleShape = ParticleShape.STAR,
                    glowEffect = true
                )

                // Badge icon
                val iconVector = when (badge.type) {
                    BadgeType.STREAK -> Icons.Default.LocalFireDepartment
                    BadgeType.COMPLETION -> Icons.Default.CheckCircle
                    BadgeType.CATEGORY -> Icons.Default.Category
                    BadgeType.SPECIAL -> Icons.Default.EmojiEvents
                }

                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badge title
            Text(
                text = "Badge Unlocked!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge name
            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge description
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // XP awarded
            if (xpAwarded > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .alpha(xpAlpha)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "+$xpAwarded XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Displays a reward item
 */
@Composable
fun RewardItem(
    reward: Reward,
    currentXp: Int,
    onRewardClaim: (Reward) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canClaim = currentXp >= reward.xpCost && !reward.isUnlocked

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reward icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (reward.isUnlocked)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = reward.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (reward.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Unlocked",
                            tint = Color.White
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${reward.xpCost} XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { onRewardClaim(reward) },
                            enabled = canClaim,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Claim")
                        }
                    }
                }
            }
        }
    }
}
