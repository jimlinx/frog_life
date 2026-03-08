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
import com.froglife.data.Reward
import com.froglife.ui.Screen
import com.froglife.viewmodel.RewardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRewardsScreen(navController: NavController, viewModel: RewardViewModel) {
    val rewards by viewModel.allRewards.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Rewards") },
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
                onClick = { navController.navigate(Screen.AddEditReward.createRoute(null)) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reward")
            }
        }
    ) { paddingValues ->
        if (rewards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No rewards yet. Add your first reward!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rewards) { reward ->
                    RewardItem(
                        reward = reward,
                        onClick = { navController.navigate(Screen.AddEditReward.createRoute(reward.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun RewardItem(reward: Reward, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(reward.color.toULong()).copy(alpha = 0.2f)
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
                Text(reward.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (reward.description.isNotEmpty()) {
                    Text(reward.description, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Cost: ${reward.pointsCost} points",
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
                    color = Color(reward.color.toULong()),
                    shape = CircleShape
                ) {}
            }
        }
    }
}
