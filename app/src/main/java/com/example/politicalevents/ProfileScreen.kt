package com.example.politicalevents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: EngagementViewModel,
    onLogout: () -> Unit
) {

    val authUser = FirebaseAuth.getInstance().currentUser

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
    var showEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null)
            Spacer(Modifier.width(8.dp))
            Text("Profile", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showEdit = true }) {
                Icon(Icons.Default.Edit, null)
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(user.username, fontWeight = FontWeight.Bold)
                Text(authUser?.email ?: "", style = MaterialTheme.typography.bodySmall)
                if (user.bio.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(user.bio)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Points", user.totalPoints)
            StatCard("Level", user.level)
            StatCard("Streak", user.streak)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Logout")
        }
    }

    if (showEdit) {
        EditProfileDialog(
            initialName = user.username,
            initialBio = user.bio,
            onSave = { name, bio ->
                viewModel.updateUsername(name)
                viewModel.updateBio(bio)
                showEdit = false
            },
            onDismiss = { showEdit = false }
        )
    }
}

@Composable
private fun RowScope.StatCard(label: String, value: Int) {
    Card(modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label)
            Text(value.toString(), fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun EditProfileDialog(
    initialName: String,
    initialBio: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var bio by remember { mutableStateOf(initialBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Username") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, bio) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}