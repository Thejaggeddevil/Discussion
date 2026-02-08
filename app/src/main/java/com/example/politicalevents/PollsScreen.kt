package com.example.politicalevents


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.politicalevents.EngagementViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PollsScreen(
    viewModel: EngagementViewModel
) {
    val polls by viewModel.polls.collectAsState()

    if (polls.isEmpty()) return

    var pollIndex by remember { mutableStateOf(0) }
    var votedOptionId by remember { mutableStateOf<String?>(null) }

    val poll = polls[pollIndex]
    val totalVotes = poll.totalVotes

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val padding = if (maxWidth > 600.dp) 32.dp else 16.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {

            /** HEADER */
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.BarChart, null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Community Polls",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            /** PROGRESS */
            item {
                LinearProgressIndicator(
                    progress = (pollIndex + 1f) / polls.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }

            /** QUESTION */
            item {
                Text(
                    poll.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(padding)
                )
            }

            /** OPTIONS */
            poll.options.forEach { option ->
                val percent =
                    if (totalVotes > 0)
                        (option.votes.toFloat() / totalVotes) * 100f
                    else 0f

                val selected = votedOptionId == option.id

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding, vertical = 6.dp)
                            .clickable(enabled = votedOptionId == null) {
                                votedOptionId = option.id
                                viewModel.votePoll(poll.id, option.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    option.text,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onBackground
                                )

                                if (votedOptionId != null) {
                                    Text(
                                        "%.1f%%".format(percent),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (votedOptionId != null) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = percent / 100f
                                )
                            }
                        }
                    }
                }
            }

            /** NEXT */
            if (votedOptionId != null && pollIndex < polls.lastIndex) {
                item {
                    Button(
                        onClick = {
                            pollIndex++
                            votedOptionId = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .height(48.dp)
                    ) {
                        Text("Next Poll")
                    }
                }
            }
        }
    }
}
