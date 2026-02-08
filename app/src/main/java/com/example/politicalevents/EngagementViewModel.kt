package com.example.politicalevents

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class EngagementViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /* ================= USER PROFILE ================= */

    private val _profileLoading = MutableStateFlow(true)
    val profileLoading = _profileLoading.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .addSnapshotListener { snap, _ ->

                // 👇 THIS IS THE VARIABLE YOU ASKED ABOUT
                val currentProfile = _userProfile.value

                if (snap == null || !snap.exists()) {
                    _profileLoading.value = false
                    return@addSnapshotListener
                }

                val points = snap.getLong("totalPoints")?.toInt()
                    ?: currentProfile?.totalPoints
                    ?: 0

                _userProfile.value = UserProfile(
                    uid = uid,

                    // ✅ NEVER FALL BACK TO "User" IF WE ALREADY HAVE A NAME
                    username = snap.getString("username")
                        ?: currentProfile?.username
                        ?: "User",

                    email = snap.getString("email")
                        ?: currentProfile?.email
                        ?: "",

                    bio = snap.getString("bio")
                        ?: currentProfile?.bio
                        ?: "",

                    totalPoints = points,
                    level = calculateLevel(points),

                    streak = snap.getLong("streak")?.toInt()
                        ?: currentProfile?.streak
                        ?: 0,

                    communityPermission = getCommunityPermission(points)
                )

                _profileLoading.value = false
            }
    }


    private fun calculateLevel(points: Int): Int {
        return (points / 10) + 1
    }

    private fun getCommunityPermission(points: Int): CommunityPermission {
        return when {
            points >= 20 -> CommunityPermission.DISCUSS
            points >= 10 -> CommunityPermission.COMMENT
            else -> CommunityPermission.LOCKED
        }
    }

    /* ================= PROFILE UPDATE (FIXED) ================= */

    fun updateUsername(name: String) {
        val uid = auth.currentUser?.uid ?: return

        // ✅ Update local state immediately
        _userProfile.value = _userProfile.value?.copy(username = name)

        // ✅ Persist to Firestore
        db.collection("users")
            .document(uid)
            .update("username", name)
    }


    fun updateBio(bio: String) {
        val uid = auth.currentUser?.uid ?: return

        _userProfile.value = _userProfile.value?.copy(bio = bio)

        db.collection("users")
            .document(uid)
            .update("bio", bio)
    }


    fun updateProfile(name: String, bio: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(
            mapOf("username" to name, "bio" to bio)
        )
    }

    /* ================= POLLS ================= */

    private val _polls = MutableStateFlow(
        listOf(
            Poll(
                id = "poll1",
                question = "Should voting be compulsory?",
                options = listOf(
                    PollOption("a", "Yes", 10),
                    PollOption("b", "No", 5)
                ),
                totalVotes = 15
            )
        )
    )
    val polls: StateFlow<List<Poll>> = _polls.asStateFlow()

    fun votePoll(pollId: String, optionId: String) {
        addPoints(3)
    }

    /* ================= QUIZZES (FIXED: 3 QUESTIONS) ================= */

    private val _quizzes = MutableStateFlow(
        listOf(
            Quiz(
                id = "q1",
                title = "Indian Constitution",
                description = "Basics",
                question = "Who is the head of the Indian State?",
                options = listOf(
                    QuizOption("a", "President", true, ""),
                    QuizOption("b", "Prime Minister", false, ""),
                    QuizOption("c", "Chief Justice", false, "")
                ),
                pointsReward = 5,
                timeLimit = 30,
                speedBonusThreshold = 10
            ),
            Quiz(
                id = "q2",
                title = "Parliament",
                description = "Legislature",
                question = "How many houses does Indian Parliament have?",
                options = listOf(
                    QuizOption("a", "One", false, ""),
                    QuizOption("b", "Two", true, ""),
                    QuizOption("c", "Three", false, "")
                ),
                pointsReward = 5,
                timeLimit = 30,
                speedBonusThreshold = 10
            ),
            Quiz(
                id = "q3",
                title = "Rights",
                description = "Fundamental Rights",
                question = "Which article guarantees Right to Equality?",
                options = listOf(
                    QuizOption("a", "Article 14", true, ""),
                    QuizOption("b", "Article 21", false, ""),
                    QuizOption("c", "Article 32", false, "")
                ),
                pointsReward = 5,
                timeLimit = 30,
                speedBonusThreshold = 10
            )
        )
    )
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    fun submitQuizResult(
        quizId: String,
        selectedOptionId: String?,
        isCorrect: Boolean,
        timeSpent: Int
    ) {
        if (!isCorrect) return   // ❌ wrong answer → no points, no streak

        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            val snap = transaction.get(userRef)

            val currentPoints = snap.getLong("totalPoints") ?: 0
            val currentStreak = snap.getLong("streak") ?: 0
            val lastQuizDate = snap.getTimestamp("lastQuizDate")?.toDate()

            val now = Date()

            val shouldIncreaseStreak =
                lastQuizDate == null || !isSameDay(lastQuizDate, now)

            val newStreak = if (shouldIncreaseStreak) {
                currentStreak + 1
            } else {
                currentStreak // same day → no extra streak
            }

            transaction.update(
                userRef,
                mapOf(
                    "totalPoints" to currentPoints + 5,
                    "streak" to newStreak,
                    "lastQuizDate" to now
                )
            )
        }
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = java.util.Calendar.getInstance().apply { time = d1 }
        val c2 = java.util.Calendar.getInstance().apply { time = d2 }

        return c1.get(java.util.Calendar.YEAR) == c2.get(java.util.Calendar.YEAR) &&
                c1.get(java.util.Calendar.DAY_OF_YEAR) == c2.get(java.util.Calendar.DAY_OF_YEAR)
    }


    /* ================= DISCUSSIONS ================= */

    private val _discussions = MutableStateFlow<List<Discussion>>(emptyList())
    val discussions: StateFlow<List<Discussion>> = _discussions.asStateFlow()

    fun observeDiscussions() {
        db.collection("discussions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                _discussions.value = snapshot.documents.map {
                    Discussion(
                        id = it.id,
                        title = it.getString("title") ?: "",
                        content = it.getString("content") ?: "",
                        authorUid = it.getString("authorUid") ?: "",
                        authorUsername = it.getString("authorUsername") ?: "Civic Learner",
                        commentCount = it.getLong("commentCount")?.toInt() ?: 0,
                        views = it.getLong("views")?.toInt() ?: 0,
                        createdAt = it.getTimestamp("createdAt")?.toDate() ?: Date()
                    )
                }
            }
    }

    fun createDiscussion(title: String, content: String) {
        val user = auth.currentUser ?: return

        db.collection("discussions").add(
            mapOf(
                "title" to title,
                "content" to content,
                "authorUid" to user.uid,
                "authorUsername" to (_userProfile.value?.username ?: "Civic Learner"),
                "commentCount" to 0,
                "views" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
    }

    fun deleteDiscussion(discussionId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("discussions").document(discussionId)
        ref.get().addOnSuccessListener {
            if (it.getString("authorUid") == uid) ref.delete()
        }
    }

    fun reportDiscussion(discussionId: String, reason: String = "Inappropriate content") {
        val uid = auth.currentUser?.uid ?: return
        db.collection("reports").add(
            mapOf(
                "discussionId" to discussionId,
                "reportedBy" to uid,
                "reason" to reason,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
    }

    fun addComment(discussionId: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("discussions").document(discussionId)

        ref.collection("comments").add(
            mapOf(
                "text" to text,
                "authorUid" to uid,
                "authorUsername" to (_userProfile.value?.username ?: "Civic Learner"),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )

        ref.update("commentCount", FieldValue.increment(1))
    }

    /* ================= POINTS (FIXED CHAIN) ================= */

    private fun addPoints(points: Int) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(uid)

        db.runTransaction {
            val snap = it.get(ref)
            val current = snap.getLong("totalPoints") ?: 0
            it.update(ref, "totalPoints", current + points)
        }
    }

    /* ================= CIVIC PULSE ================= */

    fun getDynamicCivicPulse(): CivicPulse {
        val pulses = mutableListOf<CivicPulse>()

        quizzes.value.firstOrNull()?.let {
            pulses.add(
                CivicPulse(
                    type = CivicPulseType.QUIZ,
                    title = "Quick Question",
                    subtitle = it.question,
                    actionLabel = "Answer"
                )
            )
        }

        polls.value.firstOrNull()?.let {
            pulses.add(
                CivicPulse(
                    type = CivicPulseType.POLL,
                    title = "Cast Your Vote",
                    subtitle = it.question,
                    actionLabel = "Vote"
                )
            )
        }

        if (_userProfile.value?.communityPermission == CommunityPermission.DISCUSS) {
            pulses.add(
                CivicPulse(
                    type = CivicPulseType.DISCUSSION,
                    title = "Join Discussion",
                    subtitle = "Share your opinion",
                    actionLabel = "Discuss"
                )
            )
        }

        return pulses.random()
    }

    fun getUnlockedFeatures(): List<String> {
        val points = _userProfile.value?.totalPoints ?: 0
        return buildList {
            if (points >= 5) add("Comments")
            if (points >= 20) add("Discussions")
        }
    }

    fun getNextUnlockRequirement(): String? {
        val points = _userProfile.value?.totalPoints ?: return null
        return when {
            points < 5 -> "Earn 5 points to unlock comments"
            points < 20 -> "Earn 20 points to unlock discussions"
            else -> null
        }
    }
}
