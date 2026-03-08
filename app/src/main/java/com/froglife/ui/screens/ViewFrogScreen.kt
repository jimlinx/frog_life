package com.froglife.ui.screens

import android.widget.Toast
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.froglife.data.Frog
import com.froglife.data.SpecialDate
import com.froglife.utils.BiometricHelper
import com.froglife.viewmodel.FrogViewModel
import com.froglife.viewmodel.RewardViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFrogScreen(
    navController: NavController,
    frogId: Long,
    viewModel: FrogViewModel,
    activity: FragmentActivity,
    rewardViewModel: RewardViewModel
) {
    val frogs by viewModel.allFrogs.collectAsState()
    var selectedFrog by remember { mutableStateOf<Frog?>(null) }

    LaunchedEffect(frogId, frogs) {
        if (frogId > 0) {
            selectedFrog = frogs.find { it.id == frogId }
        }
    }

    if (selectedFrog == null) {
        // Show frog selection list
        FrogSelectionScreen(
            navController = navController,
            frogs = frogs,
            viewModel = viewModel,
            rewardViewModel = rewardViewModel,
            onFrogSelected = { frog -> selectedFrog = frog }
        )
    } else {
        // Show frog details
        FrogDetailsScreen(
            navController = navController,
            frog = selectedFrog!!,
            viewModel = viewModel,
            rewardViewModel = rewardViewModel,
            activity = activity,
            onBack = { selectedFrog = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrogSelectionScreen(
    navController: NavController,
    frogs: List<Frog>,
    viewModel: FrogViewModel,
    rewardViewModel: RewardViewModel,
    onFrogSelected: (Frog) -> Unit
) {
    // Sort by current month points for ranking
    val sortedFrogs = frogs.sortedByDescending { it.currentMonthPoints }

    // Find leaders for different categories
    val monthLeader = sortedFrogs.maxByOrNull { it.currentMonthPoints }
    val totalLeader = sortedFrogs.maxByOrNull { it.wealthPoints }
    val winsLeader = sortedFrogs.maxByOrNull { it.monthlyWins }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Frog") },
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
        }
    ) { paddingValues ->
        if (frogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No frogs available. Please add a frog first!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedFrogs) { frog ->
                    val rewardBalance by viewModel.getRewardBalanceFlow(frog.id).collectAsState(initial = 0)
                    FrogSelectionCard(
                        frog = frog,
                        rank = sortedFrogs.indexOf(frog) + 1,
                        rewardBalance = rewardBalance,
                        hasCrown = frog.id == monthLeader?.id && (monthLeader?.currentMonthPoints ?: 0) > 0,
                        hasHalo = frog.id == totalLeader?.id,
                        hasPetFrog = frog.id == winsLeader?.id && (winsLeader?.monthlyWins ?: 0) > 0,
                        onClick = { onFrogSelected(frog) }
                    )
                }
            }
        }
    }
}

@Composable
fun FrogSelectionCard(
    frog: Frog,
    rank: Int,
    rewardBalance: Int,
    hasCrown: Boolean,
    hasHalo: Boolean,
    hasPetFrog: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (frog.status.name) {
        "ROCK" -> Color.Gray
        "COPPER" -> Color(0xFFB87333)
        "BRONZE" -> Color(0xFFCD7F32)
        "SILVER" -> Color(0xFFC0C0C0)
        "GOLD" -> Color(0xFFFFD700)
        "DIAMOND" -> Color(0xFFB9F2FF)
        else -> Color.Gray
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Box(contentAlignment = Alignment.TopStart) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, statusColor, MaterialTheme.shapes.medium)
                        .background(Color.White, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getFrogIcon(frog.presetIconId), fontSize = 50.sp)
                }
                // Icons overlay
                Row(
                    modifier = Modifier.offset(x = (-8).dp, y = (-12).dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (hasHalo) {
                        Text("😇", fontSize = 22.sp)
                    }
                    if (hasCrown) {
                        Text("👑", fontSize = 22.sp)
                    }
                    if (hasPetFrog) {
                        Text("🐸", fontSize = 16.sp)
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    frog.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        frog.status.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = statusColor
                    )
                    Text(getStatusIcon(frog.status.name), fontSize = 20.sp)
                }
                Text(
                    "This month: ${frog.currentMonthPoints} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Total: ${frog.wealthPoints} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Reward Balance: $rewardBalance pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (frog.monthlyWins > 0) {
                    Text(
                        "Monthly wins: ${frog.monthlyWins}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrogDetailsScreen(
    navController: NavController,
    frog: Frog,
    viewModel: FrogViewModel,
    rewardViewModel: RewardViewModel,
    activity: FragmentActivity,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentFrog by remember { mutableStateOf(frog) }
    var adjustPoints by remember { mutableStateOf("") }
    var isBonus by remember { mutableStateOf(true) } // true = bonus, false = punishment

    val frogs by viewModel.allFrogs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val rewardBalance by viewModel.getRewardBalanceFlow(currentFrog.id).collectAsState(initial = 0)

    LaunchedEffect(frogs) {
        currentFrog = frogs.find { it.id == frog.id } ?: frog
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("View Frog") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val statusColor = when (currentFrog.status.name) {
                "ROCK" -> Color.Gray
                "COPPER" -> Color(0xFFB87333)
                "BRONZE" -> Color(0xFFCD7F32)
                "SILVER" -> Color(0xFFC0C0C0)
                "GOLD" -> Color(0xFFFFD700)
                "DIAMOND" -> Color(0xFFB9F2FF)
                else -> Color.Gray
            }

            // Profile Picture with Status Ring
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(6.dp, statusColor, MaterialTheme.shapes.large)
                    .background(Color.White, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(getFrogIcon(currentFrog.presetIconId), fontSize = 80.sp)
            }

            // Name
            Text(
                currentFrog.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            // Description
            if (currentFrog.description.isNotEmpty()) {
                Text(
                    currentFrog.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Points Summary - Compact Grid Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current Month Points
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Month", style = MaterialTheme.typography.labelMedium)
                        Text(
                            currentFrog.currentMonthPoints.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Total Wealth Points
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total", style = MaterialTheme.typography.labelMedium)
                        Text(
                            currentFrog.wealthPoints.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reward Balance
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            rewardBalance.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Monthly Wins
                if (currentFrog.monthlyWins > 0) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Wins", style = MaterialTheme.typography.labelMedium)
                            Text(
                                currentFrog.monthlyWins.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentFrog.status.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getStatusIcon(currentFrog.status.name), fontSize = 32.sp)
                        }
                    }

                    // Points to next level
                    settings?.let { appSettings ->
                        val nextLevelPoints = when (currentFrog.status.name) {
                            "ROCK" -> appSettings.copperThreshold
                            "COPPER" -> appSettings.bronzeThreshold
                            "BRONZE" -> appSettings.silverThreshold
                            "SILVER" -> appSettings.goldThreshold
                            "GOLD" -> appSettings.diamondThreshold
                            "DIAMOND" -> null // Already at max
                            else -> null
                        }

                        nextLevelPoints?.let { threshold ->
                            val pointsNeeded = threshold - currentFrog.wealthPoints
                            if (pointsNeeded > 0) {
                                Text(
                                    "Next level in $pointsNeeded points",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } ?: run {
                            if (currentFrog.status.name == "DIAMOND") {
                                Text(
                                    "Maximum level achieved! 💎",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Special Dates Section
            SpecialDatesSection(
                frogId = currentFrog.id,
                viewModel = viewModel
            )

            HorizontalDivider()

            // Admin Override Section
            Text(
                "Admin Override",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isBonus = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBonus) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isBonus) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Bonus")
                }
                Button(
                    onClick = { isBonus = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isBonus) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isBonus) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Punishment")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = adjustPoints,
                    onValueChange = { adjustPoints = it },
                    label = { Text("Points") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val points = adjustPoints.toIntOrNull() ?: 0
                        if (points > 0) {
                            authenticateAndAdjust(
                                activity,
                                context,
                                viewModel,
                                currentFrog.id,
                                if (isBonus) points else -points
                            ) {
                                adjustPoints = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBonus) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    ),
                    enabled = adjustPoints.toIntOrNull() ?: 0 > 0
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
fun SpecialDatesSection(
    frogId: Long,
    viewModel: FrogViewModel
) {
    val specialDates by viewModel.getSpecialDatesForFrog(frogId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val upcomingDates = specialDates.filter { it.date >= today }.sortedBy { it.date }
    val pastDates = specialDates.filter { it.date < today }.sortedByDescending { it.date }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Special Dates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { showAddDialog = true }) {
                Text("+ Add")
            }
        }

        if (upcomingDates.isEmpty() && pastDates.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No special dates added yet", color = Color.Gray)
                }
            }
        } else {
            // Upcoming Dates
            if (upcomingDates.isNotEmpty()) {
                upcomingDates.forEach { specialDate ->
                    SpecialDateCard(
                        specialDate = specialDate,
                        isPast = false,
                        onDelete = { viewModel.deleteSpecialDate(specialDate) }
                    )
                }
            }

            // Past Dates
            if (pastDates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Past",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                pastDates.forEach { specialDate ->
                    SpecialDateCard(
                        specialDate = specialDate,
                        isPast = true,
                        onDelete = { viewModel.deleteSpecialDate(specialDate) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSpecialDateDialog(
            onDismiss = { showAddDialog = false },
            onSave = { description, date ->
                viewModel.insertSpecialDate(
                    SpecialDate(
                        frogId = frogId,
                        description = description,
                        date = date
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SpecialDateCard(
    specialDate: SpecialDate,
    isPast: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val daysDiff = TimeUnit.MILLISECONDS.toDays(specialDate.date - today).toInt()
    val countdownText = when {
        daysDiff < 0 -> "${-daysDiff} days ago"
        daysDiff == 0 -> "Today"
        daysDiff == 1 -> "Tomorrow"
        else -> "In $daysDiff days"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.primaryContainer
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
                Text(
                    specialDate.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(specialDate.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPast) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (!isPast) {
                    Text(
                        countdownText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            daysDiff == 0 -> Color(0xFFFF9800)
                            daysDiff <= 7 -> Color(0xFFFF5722)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Text("🗑️", fontSize = 20.sp)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Special Date") },
            text = { Text("Are you sure you want to delete '${specialDate.description}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
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
fun AddSpecialDateDialog(
    onDismiss: () -> Unit,
    onSave: (description: String, date: Long) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    val calendar = Calendar.getInstance().apply {
        timeInMillis = selectedDateMillis
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDateMillis = newCalendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Special Date") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Birthday, Anniversary, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Date:", fontWeight = FontWeight.Bold)

                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(description, selectedDateMillis) },
                enabled = description.isNotBlank()
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
}

private fun authenticateAndAdjust(
    activity: FragmentActivity,
    context: android.content.Context,
    viewModel: FrogViewModel,
    frogId: Long,
    adjustment: Int,
    onSuccess: () -> Unit
) {
    if (BiometricHelper.canAuthenticate(activity)) {
        BiometricHelper.authenticate(
            activity = activity,
            title = "Admin Authentication",
            subtitle = "Authenticate to modify wealth points",
            negativeButtonText = "Cancel",
            onSuccess = {
                viewModel.adjustWealthPoints(frogId, adjustment)
                Toast.makeText(
                    context,
                    "Wealth points adjusted by $adjustment",
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess()
            },
            onError = { error ->
                Toast.makeText(context, "Authentication failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        Toast.makeText(context, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
    }
}
