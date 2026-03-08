package com.froglife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.froglife.sync.GCSSyncService
import com.froglife.sync.PendingChange
import com.froglife.viewmodel.FrogViewModel
import com.froglife.viewmodel.ActivityViewModel
import com.froglife.viewmodel.RewardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsScreen(
    navController: NavController,
    syncService: GCSSyncService,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    rewardViewModel: RewardViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var pendingChanges by remember { mutableStateOf<List<PendingChange>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        pendingChanges = syncService.listPendingChanges()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Approvals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (pendingChanges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No pending changes", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingChanges) { change ->
                    PendingChangeCard(
                        change = change,
                        frogViewModel = frogViewModel,
                        activityViewModel = activityViewModel,
                        rewardViewModel = rewardViewModel,
                        onApprove = {
                            scope.launch {
                                try {
                                    // Get current master data
                                    val currentData = collectCurrentMasterData(
                                        frogViewModel,
                                        activityViewModel,
                                        rewardViewModel
                                    )

                                    // Approve change with current master data, get pending changes back
                                    val pendingDataOnly = syncService.approveChange(change, currentData)

                                    // Import only the pending changes to master device
                                    importPendingChanges(
                                        context = context,
                                        frogViewModel = frogViewModel,
                                        activityViewModel = activityViewModel,
                                        rewardViewModel = rewardViewModel,
                                        pendingData = pendingDataOnly
                                    )

                                    // Refresh pending list
                                    pendingChanges = syncService.listPendingChanges()

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Changes approved and applied!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Approval failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        onReject = {
                            scope.launch {
                                try {
                                    syncService.rejectChange(change)
                                    pendingChanges = syncService.listPendingChanges()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Changes rejected", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Rejection failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PendingChangeCard(
    change: PendingChange,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    rewardViewModel: RewardViewModel,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var comparisonDetails by remember { mutableStateOf<ChangeComparison?>(null) }

    // Calculate comparison details
    LaunchedEffect(change) {
        withContext(Dispatchers.IO) {
            // Use direct repository access to bypass StateFlow cache issues
            val currentFrogs = frogViewModel.getAllFrogs().first()
            val currentActivities = activityViewModel.getAllActivities().first()
            val currentRewards = rewardViewModel.getAllRewards().first()
            val currentLogs = frogViewModel.getAllActivityLogs().first()
            val currentRedemptions = rewardViewModel.getAllRedemptions().first()
            val currentDayComments = frogViewModel.getAllDayComments().first()
            val currentSpecialDates = frogViewModel.getAllSpecialDates().first()

            android.util.Log.d("PendingApprovals", "Current frogs count: ${currentFrogs.size}, Pending frogs count: ${change.data.frogs.size}")
            android.util.Log.d("PendingApprovals", "Current frogs IDs: ${currentFrogs.map { "${it.id}:${it.name}" }}")
            android.util.Log.d("PendingApprovals", "Pending frogs IDs: ${change.data.frogs.map { "${it.id}:${it.name}" }}")
            android.util.Log.d("PendingApprovals", "Current activities count: ${currentActivities.size}, Pending activities count: ${change.data.activities.size}")
            android.util.Log.d("PendingApprovals", "Current logs count: ${currentLogs.size}, Pending logs count: ${change.data.activityLogs.size}")

            // Match frogs by name instead of ID (IDs are device-specific)
            val newFrogs = change.data.frogs.filter { pendingFrog ->
                currentFrogs.none { it.name == pendingFrog.name }
            }
            android.util.Log.d("PendingApprovals", "New frogs: ${newFrogs.size} - ${newFrogs.map { "${it.id}:${it.name}" }}")

            val updatedFrogs = change.data.frogs.mapNotNull { pendingFrog ->
                val existing = currentFrogs.find { it.name == pendingFrog.name }
                if (existing != null && (
                    existing.status != pendingFrog.status ||
                    existing.wealthPoints != pendingFrog.wealthPoints ||
                    existing.currentMonthPoints != pendingFrog.currentMonthPoints ||
                    existing.description != pendingFrog.description ||
                    existing.presetIconId != pendingFrog.presetIconId ||
                    existing.monthlyWins != pendingFrog.monthlyWins
                )) {
                    FrogUpdate(
                        name = pendingFrog.name,
                        oldStatus = existing.status,
                        newStatus = pendingFrog.status,
                        oldPoints = existing.wealthPoints,
                        newPoints = pendingFrog.wealthPoints,
                        oldMonthPoints = existing.currentMonthPoints,
                        newMonthPoints = pendingFrog.currentMonthPoints,
                        oldDescription = existing.description,
                        newDescription = pendingFrog.description,
                        oldPresetIconId = existing.presetIconId,
                        newPresetIconId = pendingFrog.presetIconId,
                        oldMonthlyWins = existing.monthlyWins,
                        newMonthlyWins = pendingFrog.monthlyWins
                    )
                } else null
            }

            // Match activities by name instead of ID
            val newActivities = change.data.activities.filter { pendingActivity ->
                currentActivities.none { it.name == pendingActivity.name }
            }

            val updatedActivities = change.data.activities.mapNotNull { pendingActivity ->
                val existing = currentActivities.find { it.name == pendingActivity.name }
                if (existing != null && (
                    existing.description != pendingActivity.description ||
                    existing.type != pendingActivity.type ||
                    existing.wealthAmount != pendingActivity.wealthAmount
                )) {
                    ActivityUpdate(
                        name = pendingActivity.name,
                        oldName = existing.name,
                        newName = pendingActivity.name,
                        oldDescription = existing.description,
                        newDescription = pendingActivity.description,
                        oldType = existing.type,
                        newType = pendingActivity.type,
                        oldPoints = existing.wealthAmount,
                        newPoints = pendingActivity.wealthAmount
                    )
                } else null
            }

            // Match rewards by name instead of ID
            val newRewards = change.data.rewards.filter { pendingReward ->
                currentRewards.none { it.name == pendingReward.name }
            }

            val updatedRewards = change.data.rewards.mapNotNull { pendingReward ->
                val existing = currentRewards.find { it.name == pendingReward.name }
                if (existing != null && (
                    existing.description != pendingReward.description ||
                    existing.pointsCost != pendingReward.pointsCost
                )) {
                    RewardUpdate(
                        name = pendingReward.name,
                        oldName = existing.name,
                        newName = pendingReward.name,
                        oldDescription = existing.description,
                        newDescription = pendingReward.description,
                        oldPoints = existing.pointsCost,
                        newPoints = pendingReward.pointsCost
                    )
                } else null
            }

            // Match logs by frog name + activity name + date (instead of IDs)
            val newLogs = change.data.activityLogs.filter { pendingLog ->
                val pendingFrogName = change.data.frogs.find { it.id == pendingLog.frogId }?.name
                val pendingActivityName = change.data.activities.find { it.id == pendingLog.activityId }?.name

                currentLogs.none { currentLog ->
                    val currentFrogName = currentFrogs.find { it.id == currentLog.frogId }?.name
                    val currentActivityName = currentActivities.find { it.id == currentLog.activityId }?.name

                    currentFrogName == pendingFrogName &&
                    currentActivityName == pendingActivityName &&
                    currentLog.date == pendingLog.date
                }
            }.map { log ->
                val frog = change.data.frogs.find { it.id == log.frogId }
                val activity = change.data.activities.find { it.id == log.activityId }
                ActivityLogDetail(
                    frogName = frog?.name ?: "Unknown",
                    activityName = activity?.name ?: "Unknown",
                    date = log.date,
                    points = log.pointsEarned
                )
            }

            // Match redemptions by frog name + reward name + date (instead of IDs)
            val newRedemptions = change.data.rewardRedemptions.filter { pendingRedemption ->
                val pendingFrogName = change.data.frogs.find { it.id == pendingRedemption.frogId }?.name
                val pendingRewardName = change.data.rewards.find { it.id == pendingRedemption.rewardId }?.name

                currentRedemptions.none { currentRedemption ->
                    val currentFrogName = currentFrogs.find { it.id == currentRedemption.frogId }?.name
                    val currentRewardName = currentRewards.find { it.id == currentRedemption.rewardId }?.name

                    currentFrogName == pendingFrogName &&
                    currentRewardName == pendingRewardName &&
                    currentRedemption.date == pendingRedemption.date
                }
            }.map { redemption ->
                val frog = change.data.frogs.find { it.id == redemption.frogId }
                val reward = change.data.rewards.find { it.id == redemption.rewardId }
                RedemptionDetail(
                    frogName = frog?.name ?: "Unknown",
                    rewardName = reward?.name ?: "Unknown",
                    date = redemption.date,
                    points = redemption.pointsUsed
                )
            }

            // Match day comments by frog name + date (instead of frog ID)
            val newDayComments = change.data.dayComments.filter { pendingComment ->
                val pendingFrogName = change.data.frogs.find { it.id == pendingComment.frogId }?.name

                currentDayComments.none { currentComment ->
                    val currentFrogName = currentFrogs.find { it.id == currentComment.frogId }?.name

                    currentFrogName == pendingFrogName &&
                    currentComment.date == pendingComment.date
                }
            }.map { comment ->
                val frog = change.data.frogs.find { it.id == comment.frogId }
                DayCommentDetail(
                    frogName = frog?.name ?: "Unknown",
                    date = comment.date,
                    comment = comment.comment
                )
            }

            val updatedDayComments = change.data.dayComments.mapNotNull { pendingComment ->
                val pendingFrogName = change.data.frogs.find { it.id == pendingComment.frogId }?.name

                val existing = currentDayComments.find { currentComment ->
                    val currentFrogName = currentFrogs.find { it.id == currentComment.frogId }?.name
                    currentFrogName == pendingFrogName && currentComment.date == pendingComment.date
                }

                if (existing != null && existing.comment != pendingComment.comment) {
                    DayCommentUpdate(
                        frogName = pendingFrogName ?: "Unknown",
                        date = pendingComment.date,
                        oldComment = existing.comment,
                        newComment = pendingComment.comment
                    )
                } else null
            }

            // Match special dates by frog name + date (instead of frog ID)
            val newSpecialDates = change.data.specialDates.filter { pendingDate ->
                val pendingFrogName = change.data.frogs.find { it.id == pendingDate.frogId }?.name

                currentSpecialDates.none { currentDate ->
                    val currentFrogName = currentFrogs.find { it.id == currentDate.frogId }?.name

                    currentFrogName == pendingFrogName &&
                    currentDate.date == pendingDate.date
                }
            }.map { specialDate ->
                val frog = change.data.frogs.find { it.id == specialDate.frogId }
                SpecialDateDetail(
                    frogName = frog?.name ?: "Unknown",
                    date = specialDate.date,
                    description = specialDate.description
                )
            }

            comparisonDetails = ChangeComparison(
                newFrogs = newFrogs,
                updatedFrogs = updatedFrogs,
                newActivities = newActivities,
                updatedActivities = updatedActivities,
                newRewards = newRewards,
                updatedRewards = updatedRewards,
                newLogs = newLogs,
                newRedemptions = newRedemptions,
                newDayComments = newDayComments,
                updatedDayComments = updatedDayComments,
                newSpecialDates = newSpecialDates
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Device: ${change.deviceId.take(8)}...",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Time: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(change.timestamp))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            comparisonDetails?.let { details ->
                Text("Changes:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (details.newFrogs.isNotEmpty()) {
                    Text("➕ New Frogs:", fontWeight = FontWeight.Medium, color = Color(0xFF4CAF50))
                    details.newFrogs.forEach { frog ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${frog.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("  Status: ${frog.status}", fontSize = 12.sp)
                                Text("  Wealth Points: ${frog.wealthPoints}", fontSize = 12.sp)
                                Text("  Month Points: ${frog.currentMonthPoints}", fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.updatedFrogs.isNotEmpty()) {
                    Text("📝 Updated Frogs:", fontWeight = FontWeight.Medium, color = Color(0xFF2196F3))
                    details.updatedFrogs.forEach { update ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${update.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (update.oldPresetIconId != update.newPresetIconId) {
                                    Text("  Emoji: Icon ${update.oldPresetIconId} → Icon ${update.newPresetIconId}", fontSize = 12.sp)
                                }
                                if (update.oldDescription != update.newDescription) {
                                    Text("  Description:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    if (update.oldDescription.isNotEmpty()) {
                                        Text("    Old: \"${update.oldDescription}\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (update.newDescription.isNotEmpty()) {
                                        Text("    New: \"${update.newDescription}\"", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                    }
                                }
                                if (update.oldStatus != update.newStatus) {
                                    Text("  Status: ${update.oldStatus} → ${update.newStatus}", fontSize = 12.sp)
                                }
                                if (update.oldPoints != update.newPoints) {
                                    Text("  Wealth: ${update.oldPoints} → ${update.newPoints}", fontSize = 12.sp)
                                }
                                if (update.oldMonthPoints != update.newMonthPoints) {
                                    Text("  Month: ${update.oldMonthPoints} → ${update.newMonthPoints}", fontSize = 12.sp)
                                }
                                if (update.oldMonthlyWins != update.newMonthlyWins) {
                                    Text("  Monthly Wins: ${update.oldMonthlyWins} → ${update.newMonthlyWins}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.newActivities.isNotEmpty()) {
                    Text("🏃 New Activities:", fontWeight = FontWeight.Medium, color = Color(0xFF00BCD4))
                    details.newActivities.forEach { activity ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${activity.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (activity.description.isNotEmpty()) {
                                    Text("  ${activity.description}", fontSize = 11.sp)
                                }
                                Text("  Type: ${activity.type}", fontSize = 11.sp)
                                Text("  Points: ${if (activity.wealthAmount >= 0) "+${activity.wealthAmount}" else activity.wealthAmount}", fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.updatedActivities.isNotEmpty()) {
                    Text("🏃 Updated Activities:", fontWeight = FontWeight.Medium, color = Color(0xFF00BCD4))
                    details.updatedActivities.forEach { update ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${update.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (update.oldName != update.newName) {
                                    Text("  Name: ${update.oldName} → ${update.newName}", fontSize = 11.sp)
                                }
                                if (update.oldDescription != update.newDescription) {
                                    Text("  Description: \"${update.oldDescription}\" → \"${update.newDescription}\"", fontSize = 11.sp)
                                }
                                if (update.oldType != update.newType) {
                                    Text("  Type: ${update.oldType} → ${update.newType}", fontSize = 11.sp)
                                }
                                if (update.oldPoints != update.newPoints) {
                                    Text("  Points: ${if (update.oldPoints >= 0) "+${update.oldPoints}" else update.oldPoints} → ${if (update.newPoints >= 0) "+${update.newPoints}" else update.newPoints}", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.newRewards.isNotEmpty()) {
                    Text("🎁 New Rewards:", fontWeight = FontWeight.Medium, color = Color(0xFFFFEB3B))
                    details.newRewards.forEach { reward ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${reward.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (reward.description.isNotEmpty()) {
                                    Text("  ${reward.description}", fontSize = 11.sp)
                                }
                                Text("  Points: ${reward.pointsCost}", fontSize = 11.sp, color = Color(0xFFFF6F00))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.updatedRewards.isNotEmpty()) {
                    Text("🎁 Updated Rewards:", fontWeight = FontWeight.Medium, color = Color(0xFFFFEB3B))
                    details.updatedRewards.forEach { update ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${update.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (update.oldName != update.newName) {
                                    Text("  Name: ${update.oldName} → ${update.newName}", fontSize = 11.sp)
                                }
                                if (update.oldDescription != update.newDescription) {
                                    Text("  Description: \"${update.oldDescription}\" → \"${update.newDescription}\"", fontSize = 11.sp)
                                }
                                if (update.oldPoints != update.newPoints) {
                                    Text("  Points: ${update.oldPoints} → ${update.newPoints}", fontSize = 11.sp, color = Color(0xFFFF6F00))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.newLogs.isNotEmpty()) {
                    Text("📊 New Activity Logs:", fontWeight = FontWeight.Medium, color = Color(0xFFFF9800))
                    details.newLogs.forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${log.frogName} - ${log.activityName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(log.date))}", fontSize = 11.sp)
                                Text("  Points: ${log.points}", fontSize = 11.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.newRedemptions.isNotEmpty()) {
                    Text("💰 New Redemptions:", fontWeight = FontWeight.Medium, color = Color(0xFF9C27B0))
                    details.newRedemptions.forEach { redemption ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${redemption.frogName} - ${redemption.rewardName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(redemption.date))}", fontSize = 11.sp)
                                Text("  Points: ${redemption.points}", fontSize = 11.sp, color = Color(0xFFE91E63))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.newDayComments.isNotEmpty()) {
                    Text("📝 New Notes:", fontWeight = FontWeight.Medium, color = Color(0xFF673AB7))
                    details.newDayComments.forEach { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${comment.frogName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(comment.date))}", fontSize = 11.sp)
                                Text("  \"${comment.comment}\"", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.updatedDayComments.isNotEmpty()) {
                    Text("📝 Updated Notes:", fontWeight = FontWeight.Medium, color = Color(0xFF673AB7))
                    details.updatedDayComments.forEach { update ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${update.frogName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(update.date))}", fontSize = 11.sp)
                                Text("  Old: \"${update.oldComment}\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("  New: \"${update.newComment}\"", fontSize = 11.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (details.newSpecialDates.isNotEmpty()) {
                    Text("🌟 New Special Dates:", fontWeight = FontWeight.Medium, color = Color(0xFFE91E63))
                    details.newSpecialDates.forEach { specialDate ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("  ${specialDate.frogName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(specialDate.date))}", fontSize = 11.sp)
                                Text("  \"${specialDate.description}\"", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                            }
                        }
                    }
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        confirmAction = onApprove
                        showConfirmDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Approve")
                }
                Button(
                    onClick = {
                        confirmAction = onReject
                        showConfirmDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject")
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to ${if (confirmAction == onApprove) "approve" else "reject"} this change?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmAction?.invoke()
                    showConfirmDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper function to collect current master data
private suspend fun collectCurrentMasterData(
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    rewardViewModel: RewardViewModel
): com.froglife.utils.ExportData {
    return withContext(Dispatchers.IO) {
        val frogs = frogViewModel.allFrogs.first()
        val activities = activityViewModel.allActivities.first()
        val settings = frogViewModel.settings.first()
        val crossRefs = frogViewModel.getAllCrossRefs().first()
        val activityLogs = frogViewModel.getAllActivityLogs().first()
        val specialDates = frogViewModel.getAllSpecialDates().first()
        val dayComments = frogViewModel.getAllDayComments().first()
        val rewards = rewardViewModel.allRewards.first()
        val redemptions = rewardViewModel.allRedemptions.first()

        com.froglife.utils.ExportData(
            frogs = frogs,
            activities = activities,
            frogActivityRefs = crossRefs,
            activityLogs = activityLogs,
            specialDates = specialDates,
            dayComments = dayComments,
            rewards = rewards,
            rewardRedemptions = redemptions,
            settings = settings
        )
    }
}

// Helper function to import only pending changes (not full data)
private suspend fun importPendingChanges(
    context: android.content.Context,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    rewardViewModel: RewardViewModel,
    pendingData: com.froglife.utils.ExportData
) {
    withContext(Dispatchers.IO) {
        try {
            // Import only the NEW data from slave device
            // Map slave IDs to master IDs to avoid duplicates

            // Import settings (if any)
            pendingData.settings?.let { frogViewModel.insertSettingsSync(it) }

            // Get current master data
            val masterFrogs = frogViewModel.getAllFrogs().first()
            val masterActivities = activityViewModel.getAllActivities().first()
            val masterRewards = rewardViewModel.getAllRewards().first()

            // Create ID mapping: slave ID -> master ID
            val frogIdMap = mutableMapOf<Long, Long>()
            val activityIdMap = mutableMapOf<Long, Long>()
            val rewardIdMap = mutableMapOf<Long, Long>()

            // Import frogs (match by name)
            pendingData.frogs.forEach { pendingFrog ->
                val existing = masterFrogs.find { it.name == pendingFrog.name }
                if (existing != null) {
                    // Update existing frog
                    frogIdMap[pendingFrog.id] = existing.id
                    frogViewModel.updateFrog(pendingFrog.copy(id = existing.id))
                } else {
                    // Insert new frog with auto-generated ID
                    val newId = frogViewModel.insertFrogSync(pendingFrog.copy(id = 0))
                    frogIdMap[pendingFrog.id] = newId
                }
            }

            // Import activities (match by name)
            pendingData.activities.forEach { pendingActivity ->
                val existing = masterActivities.find { it.name == pendingActivity.name }
                if (existing != null) {
                    // Update existing activity
                    activityIdMap[pendingActivity.id] = existing.id
                    activityViewModel.updateActivity(pendingActivity.copy(id = existing.id))
                } else {
                    // Insert new activity with auto-generated ID
                    val newId = activityViewModel.insertActivitySync(pendingActivity.copy(id = 0))
                    activityIdMap[pendingActivity.id] = newId
                }
            }

            // Import rewards (match by name)
            pendingData.rewards.forEach { pendingReward ->
                val existing = masterRewards.find { it.name == pendingReward.name }
                if (existing != null) {
                    // Update existing reward
                    rewardIdMap[pendingReward.id] = existing.id
                    rewardViewModel.updateReward(pendingReward.copy(id = existing.id))
                } else {
                    // Insert new reward with auto-generated ID
                    val newId = rewardViewModel.insertRewardSync(pendingReward.copy(id = 0))
                    rewardIdMap[pendingReward.id] = newId
                }
            }

            // Import cross-references with mapped IDs
            pendingData.frogActivityRefs.forEach { crossRef ->
                val masterFrogId = frogIdMap[crossRef.frogId]
                val masterActivityId = activityIdMap[crossRef.activityId]
                if (masterFrogId != null && masterActivityId != null) {
                    try {
                        frogViewModel.insertCrossRefSync(
                            com.froglife.data.FrogActivityCrossRef(
                                frogId = masterFrogId,
                                activityId = masterActivityId
                            )
                        )
                    } catch (e: Exception) {
                        // Ignore duplicate cross-refs
                    }
                }
            }

            // Import activity logs with mapped IDs
            val existingLogs = frogViewModel.getAllActivityLogs().first()
            val masterLogsAsStrings = existingLogs.map { log ->
                val frog = masterFrogs.find { it.id == log.frogId }
                val activity = masterActivities.find { it.id == log.activityId }
                "${frog?.name}|${activity?.name}|${log.date}"
            }.toSet()

            pendingData.activityLogs.forEach { log ->
                val masterFrogId = frogIdMap[log.frogId]
                val masterActivityId = activityIdMap[log.activityId]

                if (masterFrogId != null && masterActivityId != null) {
                    // Check if log already exists using name-based comparison
                    val pendingFrog = pendingData.frogs.find { it.id == log.frogId }
                    val pendingActivity = pendingData.activities.find { it.id == log.activityId }
                    val logKey = "${pendingFrog?.name}|${pendingActivity?.name}|${log.date}"

                    if (!masterLogsAsStrings.contains(logKey)) {
                        try {
                            frogViewModel.insertActivityLogSync(
                                log.copy(
                                    id = 0,
                                    frogId = masterFrogId,
                                    activityId = masterActivityId
                                )
                            )
                        } catch (e: Exception) {
                            // Ignore if fails
                        }
                    }
                }
            }

            // Import special dates with mapped IDs
            val existingDates = frogViewModel.getAllSpecialDates().first()
            val masterDatesAsStrings = existingDates.map { date ->
                val frog = masterFrogs.find { it.id == date.frogId }
                "${frog?.name}|${date.date}"
            }.toSet()

            pendingData.specialDates.forEach { specialDate ->
                val masterFrogId = frogIdMap[specialDate.frogId]

                if (masterFrogId != null) {
                    val pendingFrog = pendingData.frogs.find { it.id == specialDate.frogId }
                    val dateKey = "${pendingFrog?.name}|${specialDate.date}"

                    if (!masterDatesAsStrings.contains(dateKey)) {
                        try {
                            frogViewModel.insertSpecialDateSync(
                                specialDate.copy(id = 0, frogId = masterFrogId)
                            )
                        } catch (e: Exception) {
                            // Ignore duplicates
                        }
                    }
                }
            }

            // Import day comments with mapped IDs
            val existingComments = frogViewModel.getAllDayComments().first()
            val masterCommentsAsStrings = existingComments.map { comment ->
                val frog = masterFrogs.find { it.id == comment.frogId }
                "${frog?.name}|${comment.date}"
            }.toSet()

            pendingData.dayComments.forEach { comment ->
                val masterFrogId = frogIdMap[comment.frogId]

                if (masterFrogId != null) {
                    val pendingFrog = pendingData.frogs.find { it.id == comment.frogId }
                    val commentKey = "${pendingFrog?.name}|${comment.date}"

                    if (masterCommentsAsStrings.contains(commentKey)) {
                        // Update existing comment
                        val existing = existingComments.find { existingComment ->
                            val existingFrog = masterFrogs.find { it.id == existingComment.frogId }
                            existingFrog?.name == pendingFrog?.name && existingComment.date == comment.date
                        }
                        if (existing != null) {
                            frogViewModel.updateDayComment(
                                comment.copy(id = existing.id, frogId = masterFrogId)
                            )
                        }
                    } else {
                        // Insert new comment
                        try {
                            frogViewModel.insertDayCommentSync(
                                comment.copy(id = 0, frogId = masterFrogId)
                            )
                        } catch (e: Exception) {
                            // Ignore duplicates
                        }
                    }
                }
            }

            // Import reward redemptions with mapped IDs
            val existingRedemptions = rewardViewModel.getAllRedemptions().first()
            val masterRedemptionsAsStrings = existingRedemptions.map { redemption ->
                val frog = masterFrogs.find { it.id == redemption.frogId }
                val reward = masterRewards.find { it.id == redemption.rewardId }
                "${frog?.name}|${reward?.name}|${redemption.date}"
            }.toSet()

            pendingData.rewardRedemptions.forEach { redemption ->
                val masterFrogId = frogIdMap[redemption.frogId]
                val masterRewardId = rewardIdMap[redemption.rewardId]

                if (masterFrogId != null && masterRewardId != null) {
                    val pendingFrog = pendingData.frogs.find { it.id == redemption.frogId }
                    val pendingReward = pendingData.rewards.find { it.id == redemption.rewardId }
                    val redemptionKey = "${pendingFrog?.name}|${pendingReward?.name}|${redemption.date}"

                    if (!masterRedemptionsAsStrings.contains(redemptionKey)) {
                        try {
                            rewardViewModel.insertRedemptionSync(
                                redemption.copy(
                                    id = 0,
                                    frogId = masterFrogId,
                                    rewardId = masterRewardId
                                )
                            )
                        } catch (e: Exception) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            throw e
        }
    }
}

data class ChangeComparison(
    val newFrogs: List<com.froglife.data.Frog>,
    val updatedFrogs: List<FrogUpdate>,
    val newActivities: List<com.froglife.data.Activity>,
    val updatedActivities: List<ActivityUpdate>,
    val newRewards: List<com.froglife.data.Reward>,
    val updatedRewards: List<RewardUpdate>,
    val newLogs: List<ActivityLogDetail>,
    val newRedemptions: List<RedemptionDetail>,
    val newDayComments: List<DayCommentDetail>,
    val updatedDayComments: List<DayCommentUpdate>,
    val newSpecialDates: List<SpecialDateDetail>
)

data class FrogUpdate(
    val name: String,
    val oldStatus: com.froglife.data.FrogStatus,
    val newStatus: com.froglife.data.FrogStatus,
    val oldPoints: Int,
    val newPoints: Int,
    val oldMonthPoints: Int,
    val newMonthPoints: Int,
    val oldDescription: String,
    val newDescription: String,
    val oldPresetIconId: Int,
    val newPresetIconId: Int,
    val oldMonthlyWins: Int,
    val newMonthlyWins: Int
)

data class ActivityUpdate(
    val name: String,
    val oldName: String,
    val newName: String,
    val oldDescription: String,
    val newDescription: String,
    val oldType: com.froglife.data.ActivityType,
    val newType: com.froglife.data.ActivityType,
    val oldPoints: Int,
    val newPoints: Int
)

data class RewardUpdate(
    val name: String,
    val oldName: String,
    val newName: String,
    val oldDescription: String,
    val newDescription: String,
    val oldPoints: Int,
    val newPoints: Int
)

data class ActivityLogDetail(
    val frogName: String,
    val activityName: String,
    val date: Long,
    val points: Int
)

data class RedemptionDetail(
    val frogName: String,
    val rewardName: String,
    val date: Long,
    val points: Int
)

data class DayCommentDetail(
    val frogName: String,
    val date: Long,
    val comment: String
)

data class DayCommentUpdate(
    val frogName: String,
    val date: Long,
    val oldComment: String,
    val newComment: String
)

data class SpecialDateDetail(
    val frogName: String,
    val date: Long,
    val description: String
)
