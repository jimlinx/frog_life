package com.froglife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.froglife.ui.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.froglife.sync.DeviceManager
import com.froglife.data.DeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "1.0"
    }

    val deviceManager = DeviceManager(context)
    val deviceType = deviceManager.getDeviceType()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐸 Frog Life", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Device type indicator
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = if (deviceType == DeviceType.MASTER)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (deviceType == DeviceType.MASTER) "👑 Master Device" else "📱 Slave Device",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (deviceType == DeviceType.MASTER)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                MenuButton(
                    text = "View Frog",
                    icon = "🔍",
                    backgroundColor = Color(0xFF4CAF50), // Green
                    onClick = { navController.navigate(Screen.ViewFrog.createRoute(0)) }
                )
                MenuButton(
                    text = "Calendar",
                    icon = "📅",
                    backgroundColor = Color(0xFFFFC107), // Yellow
                    onClick = { navController.navigate(Screen.Calendar.route) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                MenuButton(
                    text = "Reward History",
                    icon = "📜",
                    onClick = { navController.navigate(Screen.RewardHistory.route) }
                )
                MenuButton(
                    text = "Manage Frogs",
                    icon = "🐸",
                    onClick = { navController.navigate(Screen.ManageFrogs.route) }
                )
                MenuButton(
                    text = "Manage Activities",
                    icon = "📋",
                    onClick = { navController.navigate(Screen.ManageActivities.route) }
                )
                MenuButton(
                    text = "Manage Rewards",
                    icon = "🎁",
                    onClick = { navController.navigate(Screen.ManageRewards.route) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                MenuButton(
                    text = "Settings",
                    icon = "⚙️",
                    backgroundColor = Color.Gray,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
                }
            }

            // Version number in bottom right corner
            Text(
                text = "v$versionName",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: String,
    backgroundColor: Color? = null,
    onClick: () -> Unit
) {
    val isGrayButton = backgroundColor == Color.Gray

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: MaterialTheme.colorScheme.secondary
        ),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add white circle background for emoji when button is gray
            if (isGrayButton) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 28.sp
                    )
                }
            } else {
                Text(
                    text = icon,
                    fontSize = 32.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
