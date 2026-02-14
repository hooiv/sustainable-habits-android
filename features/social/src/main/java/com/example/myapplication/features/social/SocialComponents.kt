package com.example.myapplication.features.social

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Data class representing a user profile
 */
data class UserProfile(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val level: Int = 1,
    val experiencePoints: Int = 0,
    val joinDate: Date = Date(),
    val habitCount: Int = 0,
    val streakCount: Int = 0,
    val isOnline: Boolean = false,
    val badges: List<String> = emptyList(),
    val bio: String? = null,
    val friendIds: List<String> = emptyList()
)

/**
 * Data class representing a social challenge
 */
data class Challenge(
    val id: String,
    val name: String,
    val description: String,
    val creatorId: String,
    val participantIds: List<String>,
    val startDate: Date,
    val endDate: Date,
    val habitIds: List<String>,
    val isPublic: Boolean = true,
    val rewards: List<String> = emptyList(),
    val leaderboard: Map<String, Int> = emptyMap() // User ID to score
)

/**
 * Displays a user profile card with animations
 */
@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    modifier: Modifier = Modifier,
    isFriend: Boolean = false,
    onAddFriend: () -> Unit = {},
    onRemoveFriend: () -> Unit = {},
    onViewProfile: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar with online indicator
                Box(contentAlignment = Alignment.BottomEnd) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile.avatarUrl != null) {
                            // In a real app, you would load the image here
                            // AsyncImage or similar component would be used
                            Text(
                                text = userProfile.name.first().toString(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            // Fallback to initials
                            Text(
                                text = userProfile.name.first().toString(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Online indicator
                    if (userProfile.isOnline) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = AnimeEasing.EaseInOutQuad),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "onlinePulse"
                        )

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(Color.Green)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Level ${userProfile.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "${userProfile.habitCount} habits • ${userProfile.streakCount} day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Friend action button
                if (isFriend) {
                    IconButton(onClick = onRemoveFriend) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Remove friend",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(onClick = onAddFriend) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add friend",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Bio
                    if (userProfile.bio != null) {
                        Text(
                            text = userProfile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Badges
                    if (userProfile.badges.isNotEmpty()) {
                        Text(
                            text = "Badges",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(userProfile.badges) { badge ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = badge.first().toString(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // View profile button
                    Button(
                        onClick = onViewProfile,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("View Profile")
                    }
                }
            }
        }
    }
}

/**
 * Displays a challenge card with animations
 */
@Composable
fun ChallengeCard(
    challenge: Challenge,
    currentUserId: String,
    userProfiles: Map<String, UserProfile>,
    modifier: Modifier = Modifier,
    onJoinChallenge: () -> Unit = {},
    onLeaveChallenge: () -> Unit = {},
    onViewChallenge: () -> Unit = {}
) {
    val isParticipating = challenge.participantIds.contains(currentUserId)
    val creatorProfile = userProfiles[challenge.creatorId]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewChallenge),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Challenge header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    creatorProfile?.let {
                        Text(
                            text = "Created by ${it.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Join/Leave button
                if (isParticipating) {
                    TextButton(onClick = onLeaveChallenge) {
                        Text("Leave")
                    }
                } else {
                    Button(onClick = onJoinChallenge) {
                        Text("Join")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Challenge description
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Participants
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Participants:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Participant avatars
                Box {
                    val visibleParticipants = challenge.participantIds.take(3)

                    Row {
                        visibleParticipants.forEachIndexed { index, userId ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = (-8 * index).dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                userProfiles[userId]?.let {
                                    Text(
                                        text = it.name.first().toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // More participants indicator
                    if (challenge.participantIds.size > 3) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .offset(x = (-8 * 3).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${challenge.participantIds.size - 3}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
