package com.example.politicalevents

import java.util.Date

// ---------------- ENUMS ----------------

enum class CommunityPermission {

    COMMENT,
    DISCUSS,
    LOCKED
}

// ---------------- USER ----------------

data class UserProfile(
    val uid: String = "",
    val username: String = "Guest",
    val bio: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val quizCompleted: Int = 0,
    val communityPermission: CommunityPermission = CommunityPermission.LOCKED,
    val email: String=""
)

// ---------------- POLLS ----------------

data class Poll(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int
)

data class PollOption(
    val id: String,
    val text: String,
    val votes: Int
)

// ---------------- QUIZ ----------------

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val question: String,
    val options: List<QuizOption>,
    val pointsReward: Int,
    val timeLimit: Int,              // ⏱ seconds
    val speedBonusThreshold: Int      // ⚡ seconds
)

data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val explanation: String
)
data class Comment(
    val id: String,
    val discussionId: String,
    val authorUsername: String,
    val text: String,
    val createdAt: Date
)


// ---------------- DISCUSSION ----------------

data class Discussion(
    val id: String,
    val title: String,
    val content: String,
    val authorUsername: String,
    val authorUid: String,

    val commentCount: Int,
    val views: Int,
    val createdAt: Date
)



// -------------------- HELPERS --------------------

fun calculateLevelFromPoints(points: Int): Int {
    return (points / 50) + 1
}

fun getCommunityPermission(points: Int): CommunityPermission {
    return when {
        points >= 20 -> CommunityPermission.DISCUSS
        points >= 5 -> CommunityPermission.COMMENT
        else -> CommunityPermission.LOCKED
    }
}



enum class CivicPulseType {
    QUIZ,
    POLL,
    DISCUSSION
}

data class CivicPulse(
    val type: CivicPulseType,
    val title: String,
    val subtitle: String,
    val actionLabel: String
)

