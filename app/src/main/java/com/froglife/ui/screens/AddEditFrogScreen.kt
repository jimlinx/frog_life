package com.froglife.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.froglife.data.Frog
import com.froglife.viewmodel.ActivityViewModel
import com.froglife.viewmodel.FrogViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFrogScreen(
    navController: NavController,
    frogId: Long?,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var presetIconId by remember { mutableStateOf(0) }

    var existingFrog by remember { mutableStateOf<Frog?>(null) }
    var allActivities by remember { mutableStateOf(emptyList<com.froglife.data.Activity>()) }
    var attachedActivityIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(frogId) {
        // Force fresh load of all activities from repository
        allActivities = activityViewModel.getAllActivities().first()

        if (frogId != null && frogId > 0) {
            existingFrog = frogViewModel.allFrogs.first().find { it.id == frogId }
            existingFrog?.let {
                name = it.name
                description = it.description
                presetIconId = it.presetIconId
                // Load attached activities - use direct method to bypass cache
                val attached = activityViewModel.getActivitiesForFrogDirect(it.id).first()
                attachedActivityIds = attached.map { activity -> activity.id }.toSet()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (frogId == null) "Add Frog" else "Edit Frog") },
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
                label = { Text("Frog Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Top Update Frog button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            if (existingFrog != null) {
                                // Update existing frog
                                frogViewModel.updateFrog(
                                    existingFrog!!.copy(
                                        name = name,
                                        description = description,
                                        presetIconId = presetIconId
                                    )
                                )
                                val updateFrogId = existingFrog!!.id

                                // Update activity attachments
                                val currentAttached = activityViewModel.getActivitiesForFrogDirect(updateFrogId).first().map { it.id }.toSet()

                                // Attach new activities
                                attachedActivityIds.forEach { activityId ->
                                    if (!currentAttached.contains(activityId)) {
                                        activityViewModel.attachActivityToFrog(updateFrogId, activityId)
                                    }
                                }

                                // Detach removed activities
                                currentAttached.forEach { activityId ->
                                    if (!attachedActivityIds.contains(activityId)) {
                                        activityViewModel.detachActivityFromFrog(updateFrogId, activityId)
                                    }
                                }
                            } else {
                                // Insert new frog
                                frogViewModel.insertFrog(
                                    Frog(
                                        name = name,
                                        description = description,
                                        presetIconId = presetIconId,
                                        wealthPoints = 10
                                    )
                                )

                                // Get the newly inserted frog (it will be the latest one)
                                val newFrog = frogViewModel.allFrogs.first().maxByOrNull { it.id }
                                newFrog?.let { frog ->
                                    // Attach activities to new frog
                                    attachedActivityIds.forEach { activityId ->
                                        activityViewModel.attachActivityToFrog(frog.id, activityId)
                                    }
                                }
                            }

                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (frogId == null) "Add Frog" else "Update Frog")
            }

            HorizontalDivider()

            Text("Attach Activities:", style = MaterialTheme.typography.titleMedium)

            if (allActivities.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No activities available. Create activities in Manage Activities first.", color = Color.Gray)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allActivities.chunked(2).forEach { rowActivities ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowActivities.forEach { activity ->
                                val isAttached = attachedActivityIds.contains(activity.id)

                                // Safely convert color with fallback
                                val activityColor = try {
                                    Color(activity.color.toULong()).copy(alpha = 0.3f)
                                } catch (e: Exception) {
                                    Color(0xFF9C27B0).copy(alpha = 0.3f) // Fallback purple
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            attachedActivityIds = if (isAttached) {
                                                attachedActivityIds - activity.id
                                            } else {
                                                attachedActivityIds + activity.id
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAttached)
                                            activityColor
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (isAttached) 6.dp else 2.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                activity.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                "${if (activity.wealthAmount >= 0) "+" else ""}${activity.wealthAmount} pts",
                                                fontSize = 12.sp,
                                                color = if (activity.wealthAmount >= 0) Color.Green else Color.Red
                                            )
                                        }
                                        Checkbox(
                                            checked = isAttached,
                                            onCheckedChange = null
                                        )
                                    }
                                }
                            }
                            // Add spacer for odd number of items
                            if (rowActivities.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("Select Icon:", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(100) { i ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { presetIconId = i },
                        colors = CardDefaults.cardColors(
                            containerColor = if (presetIconId == i)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (presetIconId == i) 6.dp else 2.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getFrogIcon(i),
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }

            Button(
onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            if (existingFrog != null) {
                                // Update existing frog
                                frogViewModel.updateFrog(
                                    existingFrog!!.copy(
                                        name = name,
                                        description = description,
                                        presetIconId = presetIconId
                                    )
                                )
                                val updateFrogId = existingFrog!!.id

                                // Update activity attachments
                                val currentAttached = activityViewModel.getActivitiesForFrogDirect(updateFrogId).first().map { it.id }.toSet()

                                // Attach new activities
                                attachedActivityIds.forEach { activityId ->
                                    if (!currentAttached.contains(activityId)) {
                                        activityViewModel.attachActivityToFrog(updateFrogId, activityId)
                                    }
                                }

                                // Detach removed activities
                                currentAttached.forEach { activityId ->
                                    if (!attachedActivityIds.contains(activityId)) {
                                        activityViewModel.detachActivityFromFrog(updateFrogId, activityId)
                                    }
                                }
                            } else {
                                // Insert new frog
                                frogViewModel.insertFrog(
                                    Frog(
                                        name = name,
                                        description = description,
                                        presetIconId = presetIconId,
                                        wealthPoints = 10
                                    )
                                )

                                // Get the newly inserted frog (it will be the latest one)
                                val newFrog = frogViewModel.allFrogs.first().maxByOrNull { it.id }
                                newFrog?.let { frog ->
                                    // Attach activities to new frog
                                    attachedActivityIds.forEach { activityId ->
                                        activityViewModel.attachActivityToFrog(frog.id, activityId)
                                    }
                                }
                            }

                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (frogId == null) "Add Frog" else "Update Frog")
            }

            if (existingFrog != null) {
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Frog")
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Frog?") },
            text = { Text("Are you sure you want to delete this frog? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingFrog?.let { frogViewModel.deleteFrog(it) }
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

fun getFrogIcon(id: Int): String {
    return when (id) {
        0 -> "🐸"   // Frog
        1 -> "🐊"   // Crocodile
        2 -> "🦎"   // Lizard
        3 -> "🐢"   // Turtle
        4 -> "🦖"   // T-Rex
        5 -> "🐉"   // Dragon
        6 -> "🦕"   // Dinosaur
        7 -> "🐲"   // Dragon Face
        8 -> "🐍"   // Snake
        9 -> "🦂"   // Scorpion
        10 -> "🕷️"  // Spider
        11 -> "🦗"  // Cricket
        12 -> "🐛"  // Caterpillar
        13 -> "🦋"  // Butterfly
        14 -> "🐌"  // Snail
        15 -> "🐙"  // Octopus
        16 -> "🦑"  // Squid
        17 -> "🦀"  // Crab
        18 -> "🦞"  // Lobster
        19 -> "🦐"  // Shrimp
        20 -> "🐡"  // Blowfish
        21 -> "🐠"  // Tropical Fish
        22 -> "🐟"  // Fish
        23 -> "🐬"  // Dolphin
        24 -> "🦈"  // Shark
        25 -> "🐳"  // Whale
        26 -> "🐋"  // Whale 2
        27 -> "🦭"  // Seal
        28 -> "🐧"  // Penguin
        29 -> "🦆"  // Duck
        30 -> "🦅"  // Eagle
        31 -> "🦉"  // Owl
        32 -> "🦇"  // Bat
        33 -> "🐺"  // Wolf
        34 -> "🦊"  // Fox
        35 -> "🦝"  // Raccoon
        36 -> "🐱"  // Cat
        37 -> "🐯"  // Tiger Face
        38 -> "🦁"  // Lion
        39 -> "🐮"  // Cow
        40 -> "🐷"  // Pig
        41 -> "🐗"  // Boar
        42 -> "🐵"  // Monkey
        43 -> "🦍"  // Gorilla
        44 -> "🐼"  // Panda
        45 -> "🐨"  // Koala
        46 -> "🐻"  // Bear
        47 -> "🐻‍❄️" // Polar Bear
        48 -> "🦘"  // Kangaroo
        49 -> "🦙"  // Llama
        50 -> "🐰"  // Rabbit
        51 -> "🦔"  // Hedgehog
        52 -> "🦫"  // Beaver
        53 -> "🦦"  // Otter
        54 -> "🦥"  // Sloth
        55 -> "🦨"  // Skunk
        56 -> "🦡"  // Badger
        57 -> "🐘"  // Elephant
        58 -> "🦏"  // Rhino
        59 -> "🦛"  // Hippo
        60 -> "🦒"  // Giraffe
        61 -> "🦌"  // Deer
        62 -> "🐴"  // Horse
        63 -> "🦄"  // Unicorn
        64 -> "🦓"  // Zebra
        65 -> "🐔"  // Chicken
        66 -> "🐣"  // Hatching Chick
        67 -> "🐥"  // Baby Chick
        68 -> "🐦"  // Bird
        69 -> "🦚"  // Peacock
        70 -> "🦜"  // Parrot
        71 -> "🦩"  // Flamingo
        72 -> "🦢"  // Swan
        73 -> "🐝"  // Bee
        74 -> "🐞"  // Ladybug
        75 -> "🦟"  // Mosquito
        76 -> "🪲"  // Beetle
        77 -> "🪳"  // Cockroach
        78 -> "🪰"  // Fly
        79 -> "🪱"  // Worm
        80 -> "🦴"  // Bone
        81 -> "👻"  // Ghost
        82 -> "👽"  // Alien
        83 -> "🤖"  // Robot
        84 -> "🎃"  // Pumpkin
        85 -> "🌵"  // Cactus
        86 -> "🌲"  // Evergreen
        87 -> "🌻"  // Sunflower
        88 -> "🌸"  // Cherry Blossom
        89 -> "🍄"  // Mushroom
        90 -> "⭐"  // Star
        91 -> "🌟"  // Glowing Star
        92 -> "💎"  // Gem
        93 -> "🔥"  // Fire
        94 -> "⚡"  // Lightning
        95 -> "❄️"  // Snowflake
        96 -> "🌊"  // Wave
        97 -> "🌈"  // Rainbow
        98 -> "☀️"  // Sun
        99 -> "🌙"  // Moon
        else -> "🐸"
    }
}
