package com.froglife.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.froglife.sync.BucketFile
import com.froglife.sync.GCSSyncService
import com.froglife.viewmodel.ActivityViewModel
import com.froglife.viewmodel.FrogViewModel
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
fun ImportFromBucketScreen(
    navController: NavController,
    syncService: GCSSyncService,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    rewardViewModel: RewardViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var availableFiles by remember { mutableStateOf<List<BucketFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFile by remember { mutableStateOf<BucketFile?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<BucketFile?>(null) }
    var selectedFolder by remember { mutableStateOf("all") }

    LaunchedEffect(Unit) {
        isLoading = true
        availableFiles = syncService.listAllBackups()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from Bucket") },
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
        } else if (availableFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "No backup files found in bucket",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Make sure the master device has submitted data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    "Browse Bucket Files:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ Import will replace all local data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Folder filter chips - compact single line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedFolder == "all",
                        onClick = { selectedFolder = "all" },
                        label = { Text("All", fontSize = 11.sp) },
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    )
                    FilterChip(
                        selected = selectedFolder == "approved",
                        onClick = { selectedFolder = "approved" },
                        label = { Text("Approved", fontSize = 11.sp) },
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    )
                    FilterChip(
                        selected = selectedFolder == "manual_backups",
                        onClick = { selectedFolder = "manual_backups" },
                        label = { Text("Manual", fontSize = 11.sp) },
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    )
                    FilterChip(
                        selected = selectedFolder == "pending",
                        onClick = { selectedFolder = "pending" },
                        label = { Text("Pending", fontSize = 11.sp) },
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    )
                    FilterChip(
                        selected = selectedFolder == "rejected",
                        onClick = { selectedFolder = "rejected" },
                        label = { Text("Rejected", fontSize = 11.sp) },
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val filteredFiles = if (selectedFolder == "all") {
                    availableFiles
                } else {
                    availableFiles.filter { it.folder == selectedFolder }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFiles) { file ->
                        BucketFileCard(
                            file = file,
                            onImport = {
                                selectedFile = file
                                showConfirmDialog = true
                            },
                            onDelete = {
                                fileToDelete = file
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog && selectedFile != null) {
        val canImport = selectedFile!!.folder == "approved" || selectedFile!!.folder == "manual_backups"
        if (!canImport) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Cannot Import") },
                text = { Text("Can only import from approved or manual_backups folders. This file is in ${selectedFile!!.folder}.") },
                confirmButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("OK")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm Import") },
                text = {
                    Column {
                        Text("Are you sure you want to import this backup?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "File: ${selectedFile!!.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ This will replace ALL local data!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            scope.launch {
                                importFromBucket(
                                    context = context,
                                    syncService = syncService,
                                    fileName = selectedFile!!.fileName,
                                    frogViewModel = frogViewModel,
                                    activityViewModel = activityViewModel,
                                    rewardViewModel = rewardViewModel,
                                    onSuccess = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Import")
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

    if (showDeleteDialog && fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = {
                Column {
                    Text("Are you sure you want to delete this file?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "File: ${fileToDelete!!.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Folder: ${fileToDelete!!.folder}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ This cannot be undone!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            try {
                                syncService.deleteFileFromBucket(fileToDelete!!.fileName)
                                availableFiles = syncService.listAllBackups()
                                Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BucketFileCard(
    file: BucketFile,
    onImport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Show folder badge
                    val (badgeText, badgeColor) = when (file.folder) {
                        "approved" -> "APPROVED" to MaterialTheme.colorScheme.primary
                        "manual_backups" -> "MANUAL" to MaterialTheme.colorScheme.secondaryContainer
                        "pending" -> "PENDING" to Color(0xFFFF9800)
                        "rejected" -> "REJECTED" to MaterialTheme.colorScheme.error
                        else -> null to null
                    }

                    if (badgeText != null && badgeColor != null) {
                        Surface(
                            color = badgeColor,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                badgeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (file.folder == "manual_backups") MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                            )
                        }
                    }

                    // Show latest badge for master_data
                    if (file.displayName == "master_data" && file.folder == "approved") {
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "LATEST",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Updated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(file.timestamp))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Size: ${formatFileSize(file.size)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                    enabled = file.folder == "approved" || file.folder == "manual_backups"
                ) {
                    Text("Import")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private suspend fun importFromBucket(
    context: Context,
    syncService: GCSSyncService,
    fileName: String,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel,
    rewardViewModel: RewardViewModel,
    onSuccess: () -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            // Show loading toast
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Downloading from bucket...", Toast.LENGTH_SHORT).show()
            }

            // Download file
            val data = syncService.downloadFileFromBucket(fileName)
                ?: throw Exception("Failed to download file from bucket")

            // Clear local data
            frogViewModel.clearAllData()

            // Import settings
            data.settings?.let { frogViewModel.insertSettingsSync(it) }

            // Import frogs (map old IDs to new IDs)
            val frogIdMap = mutableMapOf<Long, Long>()
            data.frogs.forEach { frog ->
                val oldId = frog.id
                val newId = frogViewModel.insertFrogSync(frog.copy(id = 0))
                frogIdMap[oldId] = newId
            }

            // Import activities (map old IDs to new IDs)
            val activityIdMap = mutableMapOf<Long, Long>()
            data.activities.forEach { activity ->
                val oldId = activity.id
                val newId = activityViewModel.insertActivitySync(activity.copy(id = 0))
                activityIdMap[oldId] = newId
            }

            // Import rewards (map old IDs to new IDs) - do this BEFORE cross-refs
            val rewardIdMap = mutableMapOf<Long, Long>()
            data.rewards.forEach { reward ->
                val oldId = reward.id
                val newId = rewardViewModel.insertRewardSync(reward.copy(id = 0))
                rewardIdMap[oldId] = newId
            }

            // Import frog-activity cross-references (skip if IDs not found)
            data.frogActivityRefs.forEach { crossRef ->
                val newFrogId = frogIdMap[crossRef.frogId]
                val newActivityId = activityIdMap[crossRef.activityId]
                if (newFrogId != null && newActivityId != null) {
                    frogViewModel.insertCrossRefSync(crossRef.copy(frogId = newFrogId, activityId = newActivityId))
                }
            }

            // Import activity logs (skip if IDs not found)
            data.activityLogs.forEach { log ->
                val newFrogId = frogIdMap[log.frogId]
                val newActivityId = activityIdMap[log.activityId]
                if (newFrogId != null && newActivityId != null) {
                    frogViewModel.insertActivityLogSync(log.copy(id = 0, frogId = newFrogId, activityId = newActivityId))
                }
            }

            // Import special dates (skip if frog not found)
            data.specialDates.forEach { specialDate ->
                val newFrogId = frogIdMap[specialDate.frogId]
                if (newFrogId != null) {
                    frogViewModel.insertSpecialDateSync(specialDate.copy(id = 0, frogId = newFrogId))
                }
            }

            // Import day comments (skip if frog not found)
            data.dayComments.forEach { comment ->
                val newFrogId = frogIdMap[comment.frogId]
                if (newFrogId != null) {
                    frogViewModel.insertDayCommentSync(comment.copy(id = 0, frogId = newFrogId))
                }
            }

            // Import reward redemptions (skip if IDs not found)
            data.rewardRedemptions.forEach { redemption ->
                val newFrogId = frogIdMap[redemption.frogId]
                val newRewardId = rewardIdMap[redemption.rewardId]
                if (newFrogId != null && newRewardId != null) {
                    rewardViewModel.insertRedemptionSync(redemption.copy(id = 0, frogId = newFrogId, rewardId = newRewardId))
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Import successful!", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
