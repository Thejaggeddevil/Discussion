package com.example.politicalevents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * UPDATED Navigation Item
 * Includes new Community tab
 * Order: Home | Quiz | Polls | Community | Profile
 */
sealed class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : NavigationItem("home", "Home", Icons.Filled.Home)
    object Quiz : NavigationItem("quiz", "Quiz", Icons.Filled.Psychology)
    object Polls : NavigationItem("polls", "Poll", Icons.Filled.BarChart)
    object Community : NavigationItem("community", "Discuss", Icons.Filled.People)
    object Profile : NavigationItem("profile", "Profile", Icons.Filled.Person)

    companion object {
        fun all() = listOf(Home, Quiz, Polls, Community, Profile)
    }
}

/**
 * UPDATED Bottom Navigation Bar
 * Now includes Community tab
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        NavigationItem.all().forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                alwaysShowLabel = true
            )
        }
    }
}
