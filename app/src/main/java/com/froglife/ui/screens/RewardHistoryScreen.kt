package com.froglife.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.froglife.data.Frog
import com.froglife.data.Reward
import com.froglife.data.RewardRedemption
import com.froglife.viewmodel.FrogViewModel
import com.froglife.viewmodel.RewardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardHistoryScreen(
    navController: NavController,
    rewardViewModel: RewardViewModel,
    frogViewModel: FrogViewModel
) {
    val redemptions by rewardViewModel.allRedemptions.collectAsState()
    val frogs by frogViewModel.allFrogs.collectAsState()
    val rewards by rewardViewModel.allRewards.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedRedemption by remember { mutableStateOf<RewardRedemption?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reward History") },
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
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Redemption")
            }
        }
    ) { paddingValues ->
        if (redemptions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No reward redemptions yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(redemptions) { redemption ->
                    val frog = frogs.find { it.id == redemption.frogId }
                    val reward = rewards.find { it.id == redemption.rewardId }

                    RedemptionItem(
                        redemption = redemption,
                        frog = frog,
                        reward = reward,
                        onEdit = {
                            selectedRedemption = redemption
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedRedemption = redemption
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddRedemptionDialog(
            frogs = frogs,
            rewards = rewards,
            onDismiss = { showAddDialog = false },
            onAdd = { frogId, rewardId, date, pointsUsed ->
                rewardViewModel.insertRedemption(
                    RewardRedemption(
                        frogId = frogId,
                        rewardId = rewardId,
                        date = date,
                        pointsUsed = pointsUsed
                    )
                )
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && selectedRedemption != null) {
        EditRedemptionDialog(
            redemption = selectedRedemption!!,
            frogs = frogs,
            rewards = rewards,
            onDismiss = { showEditDialog = false },
            onSave = { frogId, rewardId, date, pointsUsed ->
                rewardViewModel.updateRedemption(
                    selectedRedemption!!.copy(
                        frogId = frogId,
                        rewardId = rewardId,
                        date = date,
                        pointsUsed = pointsUsed
                    )
                )
                showEditDialog = false
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm && selectedRedemption != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Redemption?") },
            text = { Text("Are you sure you want to delete this redemption record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        rewardViewModel.deleteRedemption(selectedRedemption!!)
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RedemptionItem(
    redemption: RewardRedemption,
    frog: Frog?,
    reward: Reward?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateStr = dateFormat.format(Date(redemption.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dateStr,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        frog?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    "${reward?.name ?: "Unknown"} (${redemption.pointsUsed} pts)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRedemptionDialog(
    frogs: List<Frog>,
    rewards: List<Reward>,
    onDismiss: () -> Unit,
    onAdd: (Long, Long, Long, Int) -> Unit
) {
    var selectedFrogId by remember { mutableStateOf(frogs.firstOrNull()?.id ?: 0L) }
    var selectedRewardId by remember { mutableStateOf(rewards.firstOrNull()?.id ?: 0L) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var pointsUsed by remember { mutableStateOf(rewards.firstOrNull()?.pointsCost?.toString() ?: "") }

    var expandedFrog by remember { mutableStateOf(false) }
    var expandedReward by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Redemption") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Frog dropdown
                Text("Frog:")
                ExposedDropdownMenuBox(
                    expanded = expandedFrog,
                    onExpandedChange = { expandedFrog = !expandedFrog }
                ) {
                    OutlinedTextField(
                        value = frogs.find { it.id == selectedFrogId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrog) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrog,
                        onDismissRequest = { expandedFrog = false }
                    ) {
                        frogs.forEach { frog ->
                            DropdownMenuItem(
                                text = { Text(frog.name) },
                                onClick = {
                                    selectedFrogId = frog.id
                                    expandedFrog = false
                                }
                            )
                        }
                    }
                }

                // Reward dropdown
                Text("Reward:")
                ExposedDropdownMenuBox(
                    expanded = expandedReward,
                    onExpandedChange = { expandedReward = !expandedReward }
                ) {
                    OutlinedTextField(
                        value = rewards.find { it.id == selectedRewardId }?.let { "${it.pointsCost} pts - ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedReward) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedReward,
                        onDismissRequest = { expandedReward = false }
                    ) {
                        rewards.forEach { reward ->
                            DropdownMenuItem(
                                text = { Text("${reward.pointsCost} pts - ${reward.name}") },
                                onClick = {
                                    selectedRewardId = reward.id
                                    pointsUsed = reward.pointsCost.toString()
                                    expandedReward = false
                                }
                            )
                        }
                    }
                }

                // Date button
                Text("Date:")
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormat.format(Date(selectedDate)))
                }

                // Points used (read-only, auto-filled from reward selection)
                OutlinedTextField(
                    value = pointsUsed,
                    onValueChange = {},
                    label = { Text("Points Used") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedFrogId > 0 && selectedRewardId > 0 && pointsUsed.isNotBlank()) {
                        onAdd(selectedFrogId, selectedRewardId, selectedDate, pointsUsed.toIntOrNull() ?: 0)
                    }
                },
                enabled = selectedFrogId > 0 && selectedRewardId > 0 && pointsUsed.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRedemptionDialog(
    redemption: RewardRedemption,
    frogs: List<Frog>,
    rewards: List<Reward>,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Long, Int) -> Unit
) {
    var selectedFrogId by remember { mutableStateOf(redemption.frogId) }
    var selectedRewardId by remember { mutableStateOf(redemption.rewardId) }
    var selectedDate by remember { mutableStateOf(redemption.date) }
    var pointsUsed by remember { mutableStateOf(redemption.pointsUsed.toString()) }

    var expandedFrog by remember { mutableStateOf(false) }
    var expandedReward by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Redemption") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Frog dropdown
                Text("Frog:")
                ExposedDropdownMenuBox(
                    expanded = expandedFrog,
                    onExpandedChange = { expandedFrog = !expandedFrog }
                ) {
                    OutlinedTextField(
                        value = frogs.find { it.id == selectedFrogId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrog) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrog,
                        onDismissRequest = { expandedFrog = false }
                    ) {
                        frogs.forEach { frog ->
                            DropdownMenuItem(
                                text = { Text(frog.name) },
                                onClick = {
                                    selectedFrogId = frog.id
                                    expandedFrog = false
                                }
                            )
                        }
                    }
                }

                // Reward dropdown
                Text("Reward:")
                ExposedDropdownMenuBox(
                    expanded = expandedReward,
                    onExpandedChange = { expandedReward = !expandedReward }
                ) {
                    OutlinedTextField(
                        value = rewards.find { it.id == selectedRewardId }?.let { "${it.pointsCost} pts - ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedReward) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedReward,
                        onDismissRequest = { expandedReward = false }
                    ) {
                        rewards.forEach { reward ->
                            DropdownMenuItem(
                                text = { Text("${reward.pointsCost} pts - ${reward.name}") },
                                onClick = {
                                    selectedRewardId = reward.id
                                    pointsUsed = reward.pointsCost.toString()
                                    expandedReward = false
                                }
                            )
                        }
                    }
                }

                // Date button
                Text("Date:")
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormat.format(Date(selectedDate)))
                }

                // Points used (read-only, auto-filled from reward selection)
                OutlinedTextField(
                    value = pointsUsed,
                    onValueChange = {},
                    label = { Text("Points Used") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedFrogId > 0 && selectedRewardId > 0 && pointsUsed.isNotBlank()) {
                        onSave(selectedFrogId, selectedRewardId, selectedDate, pointsUsed.toIntOrNull() ?: 0)
                    }
                },
                enabled = selectedFrogId > 0 && selectedRewardId > 0 && pointsUsed.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
