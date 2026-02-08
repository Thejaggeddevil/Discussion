package com.example.politicalevents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/* -------------------------------
   STAGE 1 MODELS (LOCAL ONLY)
-------------------------------- */



data class SimpleUser(
    val points: Int = 0,
    val permission: CommunityPermission = CommunityPermission.LOCKED
)

/* -------------------------------
   STAGE 1 SCREEN
-------------------------------- */

@Composable
fun StageOneDemoScreen() {

    var user by remember {
        mutableStateOf(SimpleUser())
    }

    var quizAnswered by remember { mutableStateOf(false) }
    var pollAnswered by remember { mutableStateOf(false) }

    fun updateUserPoints(add: Int) {
        val newPoints = user.points + add
        val permission = when {
            newPoints >= 20 -> CommunityPermission.DISCUSS
            newPoints >= 5 -> CommunityPermission.COMMENT
            else -> CommunityPermission.LOCKED
        }
        user = user.copy(points = newPoints, permission = permission)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        /* -------------------------------
           HEADER
        -------------------------------- */
        Text(
            text = "Political Engagement – Stage 1 Demo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        /* -------------------------------
           USER STATUS
        -------------------------------- */
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Points: ${user.points}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Community: ${user.permission}")
            }
        }

        /* -------------------------------
           QUIZ
        -------------------------------- */
        Card {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Quiz: Indian Constitution was adopted in?",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                QuizOption(
                    text = "1947",
                    enabled = !quizAnswered
                ) {
                    quizAnswered = true
                    updateUserPoints(5)
                }

                QuizOption(
                    text = "1950 ✅",
                    enabled = !quizAnswered
                ) {
                    quizAnswered = true
                    updateUserPoints(5)
                }

                if (quizAnswered) {
                    Spacer(Modifier.height(8.dp))
                    Text("✔ Quiz completed (+5 points)")
                }
            }
        }

        /* -------------------------------
           POLL
        -------------------------------- */
        Card {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Poll: Should voting be compulsory?",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                PollOption(
                    text = "Yes",
                    enabled = !pollAnswered
                ) {
                    pollAnswered = true
                    updateUserPoints(3)
                }

                PollOption(
                    text = "No",
                    enabled = !pollAnswered
                ) {
                    pollAnswered = true
                    updateUserPoints(3)
                }

                if (pollAnswered) {
                    Spacer(Modifier.height(8.dp))
                    Text("✔ Poll voted (+3 points)")
                }
            }
        }

        /* -------------------------------
           COMMUNITY STATUS MESSAGE
        -------------------------------- */
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (user.permission) {
                    CommunityPermission.DISCUSS -> MaterialTheme.colorScheme.primary
                    CommunityPermission.COMMENT -> MaterialTheme.colorScheme.secondary
                    CommunityPermission.LOCKED -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = when (user.permission) {
                    CommunityPermission.DISCUSS -> "Community Fully Unlocked 🎉"
                    CommunityPermission.COMMENT -> "Comments Unlocked 💬"
                    CommunityPermission.LOCKED -> "Community Locked 🔒 (Earn 5+ points)"
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* -------------------------------
   REUSABLE OPTIONS
-------------------------------- */

@Composable
private fun QuizOption(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = text
        )
    }
}

@Composable
private fun PollOption(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = text
        )
    }
}
