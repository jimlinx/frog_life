package com.froglife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.froglife.data.Activity
import com.froglife.data.ActivityType
import com.froglife.viewmodel.ActivityViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActivityScreen(
    navController: NavController,
    activityId: Long?,
    viewModel: ActivityViewModel
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.Blue.value.toLong()) }
    var activityType by remember { mutableStateOf(ActivityType.BOOLEAN) }
    var defaultValue by remember { mutableStateOf("false") }
    var wealthAmount by remember { mutableStateOf("0") }

    var existingActivity by remember { mutableStateOf<Activity?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(activityId) {
        if (activityId != null && activityId > 0) {
            scope.launch {
                existingActivity = viewModel.allActivities.first().find { it.id == activityId }
                existingActivity?.let {
                    name = it.name
                    description = it.description
                    selectedColor = it.color
                    activityType = it.type
                    defaultValue = it.defaultValue
                    wealthAmount = it.wealthAmount.toString()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (activityId == null) "Add Activity" else "Edit Activity") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Activity Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Text("Activity Type:")
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = activityType == ActivityType.BOOLEAN,
                    onClick = {
                        activityType = ActivityType.BOOLEAN
                        defaultValue = "false"
                    },
                    label = { Text("Boolean (✓/✗)") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = activityType == ActivityType.INTEGER,
                    onClick = {
                        activityType = ActivityType.INTEGER
                        defaultValue = "0"
                    },
                    label = { Text("Number") }
                )
            }

            if (activityType == ActivityType.INTEGER) {
                OutlinedTextField(
                    value = defaultValue,
                    onValueChange = { defaultValue = it },
                    label = { Text("Default Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = wealthAmount,
                onValueChange = { wealthAmount = it },
                label = { Text("Wealth Points (+ or -)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Selected Color:")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable { showColorPicker = !showColorPicker },
                colors = CardDefaults.cardColors(containerColor = Color(selectedColor.toULong()))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showColorPicker) "Tap to hide colors" else "Tap to choose color",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (showColorPicker) {
                SimpleColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (existingActivity != null) {
                            viewModel.updateActivity(
                                existingActivity!!.copy(
                                    name = name,
                                    description = description,
                                    color = selectedColor,
                                    type = activityType,
                                    defaultValue = defaultValue,
                                    wealthAmount = wealthAmount.toIntOrNull() ?: 0
                                )
                            )
                        } else {
                            viewModel.insertActivity(
                                Activity(
                                    name = name,
                                    description = description,
                                    color = selectedColor,
                                    type = activityType,
                                    defaultValue = defaultValue,
                                    wealthAmount = wealthAmount.toIntOrNull() ?: 0
                                )
                            )
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (activityId == null) "Add Activity" else "Update Activity")
            }

            if (existingActivity != null) {
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Activity")
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Activity?") },
            text = { Text("Are you sure you want to delete this activity? All associated tracking data will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingActivity?.let { viewModel.deleteActivity(it) }
                        showDeleteConfirm = false
                        navController.popBackStack()
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
fun SimpleColorPicker(selectedColor: Long, onColorSelected: (Long) -> Unit) {
    val colors = listOf(
        // Reds
        Color(0xFFFF0000), Color(0xFFE91E63), Color(0xFFF44336), Color(0xFFFF5722),
        Color(0xFFFF6B6B), Color(0xFFDC143C), Color(0xFFFF1744), Color(0xFFC62828),
        // Oranges
        Color(0xFFFF9800), Color(0xFFFF6F00), Color(0xFFFFAB40), Color(0xFFFFA726),
        // Yellows & Golds
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFFD54F), Color(0xFFFFEE58),
        // Greens
        Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF00C853), Color(0xFF00E676),
        Color(0xFF66BB6A), Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF76FF03),
        // Teals & Cyans
        Color(0xFF00BCD4), Color(0xFF26C6DA), Color(0xFF00ACC1), Color(0xFF0097A7),
        Color(0xFF00796B), Color(0xFF009688), Color(0xFF4DB6AC), Color(0xFF80CBC4),
        // Blues
        Color(0xFF2196F3), Color(0xFF0288D1), Color(0xFF01579B), Color(0xFF448AFF),
        Color(0xFF1976D2), Color(0xFF1E88E5), Color(0xFF42A5F5), Color(0xFF82B1FF),
        // Purples & Pinks
        Color(0xFF9C27B0), Color(0xFF7B1FA2), Color(0xFF4A148C), Color(0xFFAA00FF),
        Color(0xFF673AB7), Color(0xFF512DA8), Color(0xFFD500F9), Color(0xFFE040FB),
        // Browns
        Color(0xFF795548), Color(0xFF8D6E63), Color(0xFF6D4C41), Color(0xFF5D4037),
        // Grays & Blacks
        Color(0xFF9E9E9E), Color(0xFF757575), Color(0xFF616161), Color(0xFF424242),
        Color(0xFF212121), Color(0xFF000000), Color(0xFFB0BEC5), Color(0xFF78909C),
        // Light colors
        Color(0xFFFFCDD2), Color(0xFFFFF9C4), Color(0xFFC8E6C9), Color(0xFFB3E5FC),
        Color(0xFFE1BEE7), Color(0xFFBCAAA4), Color(0xFFEEEEEE), Color(0xFFD7CCC8)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier.height(300.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(colors.size) { index ->
            val color = colors[index]
            val isSelected = selectedColor == color.value.toLong()

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                    .clickable { onColorSelected(color.value.toLong()) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text("✓", color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }
}
