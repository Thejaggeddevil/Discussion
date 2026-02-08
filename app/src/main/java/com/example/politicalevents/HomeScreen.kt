package com.example.politicalevents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: EngagementViewModel,
    onNavigateToQuiz: () -> Unit,
    onNavigateToPolls: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val loading by viewModel.profileLoading.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    if (loading || profile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val user = profile!!

    // 🔹 civic pulse (safe – no recomposition spam)
    val civicPulse = remember {
        viewModel.getDynamicCivicPulse()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {

        // ================= HEADER =================
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Welcome back, ${user.username}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
//            Text(
//                text = "Level ${user.level} • ${user.totalPoints} points",
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
        }

        // ================= DAILY CIVIC PULSE =================
        DailyCivicPulseCard(
            title = civicPulse.title,
            subtitle = civicPulse.subtitle,
            actionLabel = civicPulse.actionLabel,
            isEnabled = true,
            onActionClick = {
                when (civicPulse.type) {
                    CivicPulseType.QUIZ -> onNavigateToQuiz()
                    CivicPulseType.POLL -> onNavigateToPolls()
                    CivicPulseType.DISCUSSION -> onNavigateToCommunity()
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ================= STREAK =================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Current Streak: ${user.streak} days",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ================= QUIZ / POLLS =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Take Quiz")
            }

            OutlinedButton(
                onClick = onNavigateToPolls,
                modifier = Modifier.weight(1f)
            ) {
                Text("Vote Polls")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ================= USER STATS =================
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp)
//        ) {
//            Row(
//                modifier = Modifier.padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                StatItem("Points", user.totalPoints)
//                StatItem("Level", user.level)
//                StatItem("Streak", user.streak)
//            }
//        }



        // ================= COMMUNITY ACCESS =================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Community Access", fontWeight = FontWeight.Bold)

                Text(
                    when (user.communityPermission) {
                        CommunityPermission.DISCUSS -> "Discussions unlocked"
                        CommunityPermission.COMMENT -> "Comments only"
                        CommunityPermission.LOCKED -> "Locked"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (user.communityPermission != CommunityPermission.LOCKED) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onNavigateToCommunity) {
                        Text("Go to Community")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
