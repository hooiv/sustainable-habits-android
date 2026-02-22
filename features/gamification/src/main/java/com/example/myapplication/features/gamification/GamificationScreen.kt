package com.example.myapplication.features.gamification

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.ui.components.LoadingIndicator
import kotlinx.coroutines.launch

/**
 * Screen for displaying gamification features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen(
    viewModel: GamificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val currentXp by viewModel.currentXp.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val xpForNextLevel by viewModel.xpForNextLevel.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val unlockedBadges by viewModel.unlockedBadges.collectAsState()
    val availableRewards by viewModel.availableRewards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showBadgeUnlockAnimation by viewModel.showBadgeUnlockAnimation.collectAsState()
    val recentlyUnlockedBadge by viewModel.recentlyUnlockedBadge.collectAsState()
    val xpAwarded by viewModel.xpAwarded.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Selected tab
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Badges", "Rewards", "Stats")

    // Handle badge unlock animation
    LaunchedEffect(showBadgeUnlockAnimation) {
        if (showBadgeUnlockAnimation) {
            // Animation will automatically dismiss itself
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gamification") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Level and XP progress
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Level $currentLevel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExperienceBar(
                            currentXp = currentXp,
                            maxXp = xpForNextLevel,
                            level = currentLevel,
                            onLevelUp = {
                                // Handle level up
                            }
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab content
                when (selectedTab) {
                    0 -> BadgesTab(
                        allBadges = allBadges,
                        unlockedBadges = unlockedBadges
                    )
                    1 -> RewardsTab(
                        rewards = availableRewards,
                        currentXp = currentXp,
                        onRewardClaim = { reward ->
                            // Handle reward claim
                            coroutineScope.launch {
                                // Claim reward logic would go here
                            }
                        }
                    )
                    2 -> StatsTab(
                        currentLevel = currentLevel,
                        totalXp = currentXp + (currentLevel - 1) * 100,
                        unlockedBadges = unlockedBadges.size,
                        totalBadges = allBadges.size
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                LoadingIndicator()
            }

            // Error message
            errorMessage?.let { message ->
                if (message.isNotEmpty()) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { /* Dismiss */ }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }

            // Badge unlock animation
            AnimatedVisibility(
                visible = showBadgeUnlockAnimation && recentlyUnlockedBadge != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                recentlyUnlockedBadge?.let { badge ->
                    BadgeUnlockAnimation(
                        badge = badge,
                        xpAwarded = xpAwarded,
                        onAnimationComplete = {
                            // Reset animation state
                            coroutineScope.launch {
                                // viewModel.dismissBadgeUnlockAnimation()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tab for displaying badges
 */
@Composable
fun BadgesTab(
    allBadges: List<Badge>,
    unlockedBadges: List<Badge>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Unlocked badges section
        if (unlockedBadges.isNotEmpty()) {
            Text(
                text = "Unlocked Badges (${unlockedBadges.size}/${allBadges.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(unlockedBadges) { badge ->
                    AchievementBadge(
                        title = badge.title,
                        description = badge.description,
                        isUnlocked = true,
                        iconVector = when (badge.type) {
                            BadgeType.STREAK -> Icons.Default.LocalFireDepartment
                            BadgeType.COMPLETION -> Icons.Default.CheckCircle
                            BadgeType.CATEGORY -> Icons.Default.Category
                            BadgeType.SPECIAL -> Icons.Default.EmojiEvents
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // All badges section
        Text(
            text = "All Badges",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allBadges) { badge ->
                AchievementBadge(
                    title = badge.title,
                    description = badge.description,
                    isUnlocked = badge.isUnlocked,
                    iconVector = when (badge.type) {
                        BadgeType.STREAK -> Icons.Default.LocalFireDepartment
                        BadgeType.COMPLETION -> Icons.Default.CheckCircle
                        BadgeType.CATEGORY -> Icons.Default.Category
                        BadgeType.SPECIAL -> Icons.Default.EmojiEvents
                    }
                )
            }
        }
    }
}

/**
 * Tab for displaying rewards
 */
@Composable
fun RewardsTab(
    rewards: List<Reward>,
    currentXp: Int,
    onRewardClaim: (Reward) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Available Rewards",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (rewards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No rewards available yet",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rewards) { reward ->
                    RewardItem(
                        reward = reward,
                        currentXp = currentXp,
                        onRewardClaim = onRewardClaim
                    )
                }
            }
        }
    }
}

/**
 * Tab for displaying stats
 */
@Composable
fun StatsTab(
    currentLevel: Int,
    totalXp: Int,
    unlockedBadges: Int,
    totalBadges: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Level card
            StatCard(
                title = "Level",
                value = currentLevel.toString(),
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )

            // XP card
            StatCard(
                title = "Total XP",
                value = totalXp.toString(),
                icon = Icons.Default.Bolt,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badges card
            StatCard(
                title = "Badges",
                value = "$unlockedBadges/$totalBadges",
                icon = Icons.Default.EmojiEvents,
                modifier = Modifier.weight(1f)
            )

            // Completion rate card
            StatCard(
                title = "Completion",
                value = if (totalBadges > 0) "${(unlockedBadges * 100) / totalBadges}%" else "0%",
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Achievement progress
        Text(
            text = "Achievement Progress",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bars
        ProgressItem(
            title = "Badges Unlocked",
            progress = if (totalBadges > 0) unlockedBadges.toFloat() / totalBadges else 0f,
            progressText = "$unlockedBadges/$totalBadges"
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProgressItem(
            title = "Level Progress",
            progress = 0.75f, // This would be calculated based on XP
            progressText = "75%"
        )
    }
}

/**
 * Card for displaying a stat
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Progress item for displaying progress
 */
@Composable
fun ProgressItem(
    title: String,
    progress: Float,
    progressText: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = progressText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
