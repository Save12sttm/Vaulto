package com.example.vaulto.ui.screens.health

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vaulto.data.local.entities.VaultItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordHealthScreen(
    viewModel: PasswordHealthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.analyzePasswords()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Health") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SecurityScoreCard(score = state.securityScore)
                }

                item {
                    StatsOverviewCard(state = state)
                }

                if (state.weakPasswords.isNotEmpty()) {
                    item {
                        Text(
                            text = "Weak Passwords (${state.weakPasswords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(state.weakPasswords) { item ->
                        PasswordIssueCard(
                            item = item,
                            issueType = IssueType.WEAK,
                            onClick = { onItemClick(item.id) }
                        )
                    }
                }

                if (state.reusedPasswords.isNotEmpty()) {
                    item {
                        Text(
                            text = "Reused Passwords (${state.reusedPasswords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9800)
                        )
                    }
                    items(state.reusedPasswords) { item ->
                        PasswordIssueCard(
                            item = item,
                            issueType = IssueType.REUSED,
                            onClick = { onItemClick(item.id) }
                        )
                    }
                }

                if (state.oldPasswords.isNotEmpty()) {
                    item {
                        Text(
                            text = "Old Passwords (${state.oldPasswords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFC107)
                        )
                    }
                    items(state.oldPasswords) { item ->
                        PasswordIssueCard(
                            item = item,
                            issueType = IssueType.OLD,
                            onClick = { onItemClick(item.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun SecurityScoreCard(score: Int) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        label = "score animation"
    )

    val color = when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 60 -> Color(0xFF8BC34A)
        score >= 40 -> Color(0xFFFFC107)
        score >= 20 -> Color(0xFFFF9800)
        else -> Color(0xFFE53935)
    }

    val rating = when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        score >= 20 -> "Weak"
        else -> "Very Weak"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Security Score",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedScore / 100f },
                    modifier = Modifier.size(120.dp),
                    color = color,
                    strokeWidth = 12.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        color = color
                    )
                    Text(
                        text = rating,
                        style = MaterialTheme.typography.labelMedium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun StatsOverviewCard(state: PasswordHealthState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium
            )
            
            StatRow(
                label = "Total Passwords",
                value = "${state.totalPasswords}",
                icon = Icons.Default.Lock,
                color = MaterialTheme.colorScheme.primary
            )
            
            StatRow(
                label = "Weak Passwords",
                value = "${state.weakPasswords.size}",
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.error
            )
            
            StatRow(
                label = "Reused Passwords",
                value = "${state.reusedPasswords.size}",
                icon = Icons.Default.ContentCopy,
                color = Color(0xFFFF9800)
            )
            
            StatRow(
                label = "Old Passwords (>90 days)",
                value = "${state.oldPasswords.size}",
                icon = Icons.Default.Schedule,
                color = Color(0xFFFFC107)
            )
        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
fun PasswordIssueCard(
    item: VaultItemEntity,
    issueType: IssueType,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (issueType) {
                        IssueType.WEAK -> Icons.Default.Warning
                        IssueType.REUSED -> Icons.Default.ContentCopy
                        IssueType.OLD -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = when (issueType) {
                        IssueType.WEAK -> MaterialTheme.colorScheme.error
                        IssueType.REUSED -> Color(0xFFFF9800)
                        IssueType.OLD -> Color(0xFFFFC107)
                    }
                )
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.username.ifEmpty { "No username" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class IssueType {
    WEAK, REUSED, OLD
}