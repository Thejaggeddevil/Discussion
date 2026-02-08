package com.example.politicalevents

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun QuizScreen(
    viewModel: EngagementViewModel,
    onBackClick: () -> Unit
) {
    val quizzes by viewModel.quizzes.collectAsState()

    // 🚨 HARD GUARD
    if (quizzes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var index by remember { mutableStateOf(0) }

    // 🔥 IMPORTANT: derive quiz from index
    val quiz = quizzes[index]

    var selectedOptionId by remember(index) { mutableStateOf<String?>(null) }
    var submitted by remember(index) { mutableStateOf(false) }
    var isCorrect by remember(index) { mutableStateOf(false) }

    var timeElapsed by remember(index) { mutableStateOf(0) }

    /* ---------------- TIMER ---------------- */
    LaunchedEffect(quiz.id) {
        timeElapsed = 0
        while (!submitted && timeElapsed < quiz.timeLimit) {
            delay(1000)
            timeElapsed++
        }
        if (timeElapsed >= quiz.timeLimit && !submitted) {
            isCorrect = evaluateAnswer(quiz, selectedOptionId)
            submitted = true
            viewModel.submitQuizResult(
                quiz.id,
                selectedOptionId,
                isCorrect,
                timeElapsed
            )
        }
    }

    val timeRemaining = quiz.timeLimit - timeElapsed
    val timeProgress = timeElapsed.toFloat() / quiz.timeLimit
    val isTimeWarning = timeRemaining <= 5

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

            /* ---------------- HEADER ---------------- */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                    Text(
                        "Question ${index + 1} of ${quizzes.size}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.width(48.dp))
                }
            }

            /* ---------------- PROGRESS ---------------- */
            item {
                LinearProgressIndicator(
                    progress = (index + 1f) / quizzes.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                        .height(4.dp)
                )
            }

            /* ---------------- TIMER UI ---------------- */
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Time Remaining")
                            Text(
                                "${timeRemaining}s",
                                fontWeight = FontWeight.Bold,
                                color = if (isTimeWarning)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { timeProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isTimeWarning)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            /* ---------------- QUESTION ---------------- */
            item {
                Text(
                    quiz.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(padding)
                )
            }

            /* ---------------- OPTIONS ---------------- */
            items(quiz.options.size) { i ->
                val option = quiz.options[i]
                val selected = selectedOptionId == option.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding, vertical = 6.dp)
                        .clickable(enabled = !submitted) {
                            selectedOptionId = option.id
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option.text,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onBackground
                        )
                        if (submitted && selected) {
                            Text(
                                if (isCorrect) "✓" else "✗",
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color.Green else Color.Red
                            )
                        }
                    }
                }
            }

            /* ---------------- ACTION BUTTON ---------------- */
            item {
                Button(
                    onClick = {
                        if (!submitted) {
                            isCorrect = evaluateAnswer(quiz, selectedOptionId)
                            submitted = true
                            viewModel.submitQuizResult(
                                quiz.id,
                                selectedOptionId,
                                isCorrect,
                                timeElapsed
                            )
                        } else {
                            if (index < quizzes.lastIndex) {
                                index++
                            } else {
                                onBackClick()
                            }
                        }
                    },
                    enabled = selectedOptionId != null || submitted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (!submitted) "Check Answer"
                        else if (index < quizzes.lastIndex) "Next Question"
                        else "Finish"
                    )
                }
            }
        }
    }
}

/* ---------------- PURE LOGIC ---------------- */
private fun evaluateAnswer(
    quiz: Quiz,
    selectedOptionId: String?
): Boolean {
    return quiz.options
        .firstOrNull { it.id == selectedOptionId }
        ?.isCorrect == true
}
