package com.example.politicalevents.firebase

import com.example.politicalevents.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirestoreUserRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val USERS = "users"

    // ------------------------------------------------
    // USER
    // ------------------------------------------------

    suspend fun createUserIfNotExists(uid: String) {
        val ref = db.collection(USERS).document(uid)
        val snap = ref.get().await()

        if (!snap.exists()) {
            ref.set(
                mapOf(
                    "username" to "Civic Learner",
                    "bio" to "",
                    "totalPoints" to 0,
                    "streak" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastActivityAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun getUser(uid: String): UserProfile {
        val snap = db.collection(USERS).document(uid).get().await()
        val points = snap.getLong("totalPoints")?.toInt() ?: 0

        return UserProfile(
            uid = uid,
            username = snap.getString("username") ?: "",
            bio = snap.getString("bio") ?: "",
            totalPoints = points,
            level = calculateLevelFromPoints(points),
            streak = snap.getLong("streak")?.toInt() ?: 0,
            quizCompleted = snap.getLong("quizCompleted")?.toInt() ?: 0,
            communityPermission = getCommunityPermission(points),
            email = snap.getString("email") ?: ""
        )


    }

    suspend fun updateUsername(uid: String, username: String) {
        db.collection(USERS).document(uid)
            .update("username", username)
            .await()
    }

    suspend fun updateBio(uid: String, bio: String) {
        db.collection(USERS).document(uid)
            .update("bio", bio)
            .await()
    }

    // ------------------------------------------------
    // QUIZ  (+5 points)
    // ------------------------------------------------

    suspend fun applyQuizCompletion(
        uid: String,
        quizId: String,
        optionId: String,
        isCorrect: Boolean
    ) {
        val pointsEarned = 5

        db.collection(USERS).document(uid)
            .update(
                mapOf(
                    "totalPoints" to FieldValue.increment(pointsEarned.toLong()),
                    "streak" to FieldValue.increment(1),
                    "lastActivityAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // ------------------------------------------------
    // POLL  (+3 points)
    // ------------------------------------------------

    suspend fun applyPollVote(
        uid: String,
        pollId: String,
        optionId: String
    ) {
        db.collection(USERS).document(uid)
            .update(
                mapOf(
                    "totalPoints" to FieldValue.increment(3),
                    "lastActivityAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // ------------------------------------------------
    // STATIC DATA (DEMO ONLY)
    // ------------------------------------------------

    fun getPolls(): List<Poll> {
        return listOf(
            Poll(
                id = "p1",
                question = "Should voting be compulsory?",
                options = listOf(
                    PollOption("o1", "Yes", 60),
                    PollOption("o2", "No", 40)
                ),
                totalVotes = 100
            )
        )

    }
    private fun getCommunityPermission(points: Int): CommunityPermission {
        return when {
            points >= 20 -> CommunityPermission.DISCUSS
            points >= 10 -> CommunityPermission.COMMENT
            else -> CommunityPermission.LOCKED
        }
    }



//    fun getQuizzes(): List<Quiz> {
//        return listOf(
//            Quiz(
//                id = "q1",
//                question = "What is the minimum voting age in India?",
//                description = "Basic civics knowledge question",
//                options = listOf(
//                    QuizOption(
//                        id = "a",
//                        text = "16",
//                        isCorrect = false,
//                        explanation = "Voting age in India is not 16"
//                    ),
//                    QuizOption(
//                        id = "b",
//                        text = "18",
//                        isCorrect = true,
//                        explanation = "18 is the minimum voting age in India"
//                    ),
//                    QuizOption(
//                        id = "c",
//                        text = "21",
//                        isCorrect = false,
//                        explanation = "21 is not the voting age",
//
//                    )
//                ),
//                correctOptionId = "b",
//                pointsReward = 5
//            )
//        )
//    }


}
