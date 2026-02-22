package com.example.myapplication.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.animation.*

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val totalPages = 4
    var page by rememberSaveable { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        // ─── Ambient particle background per page ────────────────────────────
        when (page) {
            0 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 40,
                particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                particleShape = ParticleShape.CIRCLE,
                particleEffect = ParticleEffect.FLOAT,
                maxSpeed = 0.4f
            )
            1 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 40,
                particleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                particleShape = ParticleShape.SQUARE,
                particleEffect = ParticleEffect.WAVE,
                maxSpeed = 0.3f
            )
            2 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 40,
                particleColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                particleShape = ParticleShape.TRIANGLE,
                particleEffect = ParticleEffect.VORTEX,
                maxSpeed = 0.35f
            )
            3 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 40,
                particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                particleShape = ParticleShape.STAR,
                particleEffect = ParticleEffect.PULSE,
                maxSpeed = 0.25f,
                colorVariation = true
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ─── Expanding pill page indicator ───────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalPages) { i ->
                    val isSelected = i == page
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "dot$i"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ─── Animated page content ────────────────────────────────────────
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    if (targetState > initialState)
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    else
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                },
                label = "pageContent"
            ) { targetPage ->
                val accentColor = when (targetPage) {
                    1 -> MaterialTheme.colorScheme.secondary
                    2 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                val emoji = when (targetPage) {
                    0 -> "🌱"; 1 -> "🔔"; 2 -> "📊"; else -> "☁️"
                }
                val title = when (targetPage) {
                    0 -> "Build Better Habits"
                    1 -> "Never Miss a Day"
                    2 -> "See Your Progress"
                    else -> "Sync Across Devices"
                }
                val subtitle = when (targetPage) {
                    0 -> "Track daily, weekly, or custom habits in one place. Every small step compounds into real change."
                    1 -> "Smart reminders nudge you at the right moment. Set your ideal hour and let HabitFlow do the rest."
                    2 -> "Rich charts and insights show you exactly how far you've come — and what to focus on next."
                    else -> "Sign in with Google to back up your habits to the cloud. Safe and in sync, always."
                }
                val features: List<Pair<ImageVector, String>> = when (targetPage) {
                    0 -> listOf(
                        Icons.Default.CheckCircle to "Mark habits complete with one tap",
                        Icons.Default.Repeat to "Daily, weekly, or custom frequency",
                        Icons.Default.Star to "Streaks to keep you consistent"
                    )
                    1 -> listOf(
                        Icons.Default.Notifications to "Personalized reminder times",
                        Icons.Default.Schedule to "Never-miss smart nudges",
                        Icons.Default.Favorite to "Streak protection alerts"
                    )
                    2 -> listOf(
                        Icons.Default.ShowChart to "Completion rate over time",
                        Icons.Default.Star to "Achievement badges & milestones",
                        Icons.Default.FilterList to "Category-level breakdown"
                    )
                    else -> listOf(
                        Icons.Default.Info to "Automatic cloud backup",
                        Icons.Default.Refresh to "Seamless multi-device sync",
                        Icons.Default.Lock to "Private & encrypted data"
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.3f),
                                        accentColor.copy(alpha = 0.06f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Feature bullets card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            features.forEach { (icon, label) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ─── Navigation buttons ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (page > 0) {
                    OutlinedButton(
                        onClick = { page-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Back") }
                } else {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                val nextAccent = when (page) {
                    1 -> MaterialTheme.colorScheme.secondary
                    2 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                Button(
                    onClick = { if (page < totalPages - 1) page++ else onFinish() },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = nextAccent)
                ) {
                    Text(
                        if (page < totalPages - 1) "Next" else "Get Started 🚀",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
