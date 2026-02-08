package com.example.politicalevents

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CommunityScreen(
    viewModel: EngagementViewModel
) {
    val discussions by viewModel.discussions.collectAsState()


    var showCreateSheet by remember { mutableStateOf(false) }
    var openDiscussion by remember { mutableStateOf<Discussion?>(null) }

    // 🔥 start listening once
    LaunchedEffect(Unit) {
        viewModel.observeDiscussions()
    }

    // 🔒 loader ONLY for profile (required for permissions)
    val loading by viewModel.profileLoading.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }


    val user = profile!!

    AnimatedContent(
        targetState = openDiscussion,
        transitionSpec = {
            fadeIn(tween(200)) with fadeOut(tween(200))
        },
        label = "community_transition"
    ) { selectedDiscussion ->

        if (selectedDiscussion != null) {
            CommentsScreen(
                discussion = selectedDiscussion,
                viewModel = viewModel,
                onBack = { openDiscussion = null }
            )
        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                // ---------------- DISCUSSION LIST ----------------
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    if (discussions.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No discussions yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Be the first to start a discussion",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(discussions, key = { it.id }) { discussion ->
                            DiscussionCard(
                                discussion = discussion,
                                viewModel = viewModel,
                                onOpen = { openDiscussion = discussion }
                            )
                        }
                    }
                }

                // ---------------- FAB (+) — ALWAYS VISIBLE ----------------
                FloatingActionButton(
                    onClick = {
                        if (user.totalPoints >= 20) {
                            showCreateSheet = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    shape = CircleShape,
                    containerColor =
                        if (user.totalPoints >= 20)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create discussion")
                }

                // ---------------- CREATE SHEET ----------------
                if (showCreateSheet) {
                    CreateDiscussionSheet(
                        onDismiss = { showCreateSheet = false },
                        onCreateClick = { title, content ->
                            viewModel.createDiscussion(title, content)
                            showCreateSheet = false
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
private fun DiscussionCard(
    discussion: Discussion,
    viewModel: EngagementViewModel,
    onOpen: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isOwner = discussion.authorUid == viewModel.userProfile.value?.uid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onOpen() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(discussion.authorUsername, fontWeight = FontWeight.Bold)
                    Text(
                        timeAgo(discussion.createdAt),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, null)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isOwner) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    viewModel.deleteDiscussion(discussion.id)
                                    showMenu = false
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = {
                                    viewModel.reportDiscussion(discussion.id)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (discussion.title.isNotBlank()) {
                Text(
                    discussion.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
            }

            Text(discussion.content)

            Spacer(Modifier.height(14.dp))

            AssistChip(
                onClick = onOpen,
                label = { Text("${discussion.commentCount} replies") },
                leadingIcon = {
                    Icon(Icons.Filled.ChatBubbleOutline, null)
                }
            )
        }
    }
}


// ============================================================================
// COMMENTS SCREEN
// ============================================================================

@Composable
fun CommentsScreen(
    discussion: Discussion,
    viewModel: EngagementViewModel,
    onBack: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<Comment>() }

    DisposableEffect(discussion.id) {
        val listener = FirebaseFirestore.getInstance()
            .collection("discussions")
            .document(discussion.id)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                comments.clear()
                comments.addAll(
                    snapshot.documents.map {
                        Comment(
                            id = it.id,
                            discussionId = discussion.id,
                            authorUsername = it.getString("authorUsername") ?: "",
                            text = it.getString("text") ?: "",
                            createdAt = it.getTimestamp("createdAt")?.toDate() ?: Date()
                        )
                    }
                )
            }

        onDispose { listener.remove() }
    }

    Column(Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text("Post", fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(discussion.authorUsername, fontWeight = FontWeight.Bold)
                Text(
                    timeAgo(discussion.createdAt),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(6.dp))

                if (discussion.title.isNotBlank()) {
                    Text(discussion.title, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                }

                Text(discussion.content)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            if (comments.isEmpty()) {
                item {
                    Text(
                        "No comments yet",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(comments) { comment ->
                    CommentItem(comment)
                }
            }
        }

        Divider()

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Post your reply") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(Modifier.width(8.dp))

            Button(
                enabled = replyText.isNotBlank(),
                onClick = {
                    viewModel.addComment(discussion.id, replyText)
                    replyText = ""
                }
            ) {
                Text("Post")
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.authorUsername, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text(
                    "· ${timeAgo(comment.createdAt)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(comment.text)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiscussionSheet(
    onDismiss: () -> Unit,
    onCreateClick: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Start a discussion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("What’s happening?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(16.dp))

            Button(
                enabled = title.isNotBlank() && content.isNotBlank(),
                onClick = { onCreateClick(title, content) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Post")
            }
        }
    }
}
