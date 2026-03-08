package com.froglife.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.froglife.data.Frog
import com.froglife.ui.Screen
import com.froglife.viewmodel.FrogViewModel
import com.froglife.viewmodel.RewardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFrogsScreen(
    navController: NavController,
    frogViewModel: FrogViewModel,
    rewardViewModel: RewardViewModel
) {
    val frogs by frogViewModel.allFrogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Frogs") },
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
                onClick = { navController.navigate(Screen.AddEditFrog.createRoute(null)) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Frog")
            }
        }
    ) { paddingValues ->
        if (frogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No frogs yet. Add your first frog!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(frogs) { frog ->
                    val rewardBalance by frogViewModel.getRewardBalanceFlow(frog.id).collectAsState(initial = 0)
                    FrogItem(
                        frog = frog,
                        rewardBalance = rewardBalance,
                        onClick = { navController.navigate(Screen.AddEditFrog.createRoute(frog.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun FrogItem(frog: Frog, rewardBalance: Int, onClick: () -> Unit) {
    val statusColor = when (frog.status.name) {
        "ROCK" -> androidx.compose.ui.graphics.Color.Gray
        "COPPER" -> androidx.compose.ui.graphics.Color(0xFFB87333)
        "BRONZE" -> androidx.compose.ui.graphics.Color(0xFFCD7F32)
        "SILVER" -> androidx.compose.ui.graphics.Color(0xFFC0C0C0)
        "GOLD" -> androidx.compose.ui.graphics.Color(0xFFFFD700)
        "DIAMOND" -> androidx.compose.ui.graphics.Color(0xFFB9F2FF)
        else -> androidx.compose.ui.graphics.Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(frog.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        frog.status.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(getStatusIcon(frog.status.name), fontSize = 18.sp)
                }
                Text(
                    "This month: ${frog.currentMonthPoints} | Total: ${frog.wealthPoints}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Reward Balance: $rewardBalance pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                if (frog.description.isNotEmpty()) {
                    Text(frog.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(getFrogIcon(frog.presetIconId), fontSize = 32.sp)
            }
        }
    }
}

fun getStatusIcon(status: String): String {
    return when (status) {
        "ROCK" -> "🪨"
        "COPPER" -> "🟤"
        "BRONZE" -> "🥉"
        "SILVER" -> "🥈"
        "GOLD" -> "🥇"
        "DIAMOND" -> "💎"
        else -> "🐸"
    }
}
