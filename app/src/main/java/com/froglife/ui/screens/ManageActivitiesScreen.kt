package com.froglife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.froglife.data.Activity
import com.froglife.data.ActivityType
import com.froglife.ui.Screen
import com.froglife.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageActivitiesScreen(navController: NavController, viewModel: ActivityViewModel) {
    val activities by viewModel.allActivities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Activities") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditActivity.createRoute(null)) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { paddingValues ->
        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No activities yet. Add your first activity!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    ActivityItem(
                        activity = activity,
                        onClick = { navController.navigate(Screen.AddEditActivity.createRoute(activity.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit) {
    // Safely handle color conversion with fallback
    val activityColor = try {
        Color(activity.color.toULong())
    } catch (e: Exception) {
        Color(0xFF9C27B0) // Default purple color if invalid
    }

    val backgroundColor = try {
        activityColor.copy(alpha = 0.2f)
    } catch (e: Exception) {
        Color(0xFF9C27B0).copy(alpha = 0.2f) // Fallback background
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (activity.description.isNotEmpty()) {
                    Text(activity.description, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Type: ${if (activity.type == ActivityType.BOOLEAN) "Check/Cross" else "Number"} | Wealth: ${activity.wealthAmount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = activityColor,
                    shape = CircleShape
                ) {}
            }
        }
    }
}
