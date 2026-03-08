package com.froglife

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.froglife.data.FrogDatabase
import com.froglife.data.FrogRepository
import com.froglife.sync.GCSSyncService
import com.froglife.ui.Screen
import com.froglife.ui.screens.*
import com.froglife.ui.theme.FrogLifeTheme
import com.froglife.viewmodel.ActivityViewModel
import com.froglife.viewmodel.CalendarViewModel
import com.froglife.viewmodel.FrogViewModel
import com.froglife.viewmodel.RewardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private lateinit var repository: FrogRepository
    private lateinit var frogViewModel: FrogViewModel
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var rewardViewModel: RewardViewModel
    private var syncService: GCSSyncService? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FrogLife", "Notification permission granted")
        } else {
            Log.d("FrogLife", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = FrogDatabase.getDatabase(applicationContext)
        repository = FrogRepository(database)
        frogViewModel = FrogViewModel(repository)
        activityViewModel = ActivityViewModel(repository)
        calendarViewModel = CalendarViewModel(repository)
        rewardViewModel = RewardViewModel(repository)

        // Initialize GCS sync service if credentials file exists
        try {
            syncService = GCSSyncService(applicationContext, repository)
            Log.d("FrogLife", "GCS sync service initialized")
        } catch (e: Exception) {
            Log.w("FrogLife", "GCS sync service not available (credentials not found)")
            syncService = null
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Try to get FCM token (optional - app works without it)
        syncService?.let { service ->
            try {
                // Import Firebase classes dynamically to avoid crash if not configured
                val firebaseMessaging = Class.forName("com.google.firebase.messaging.FirebaseMessaging")
                    .getDeclaredMethod("getInstance")
                    .invoke(null)

                val getTokenMethod = firebaseMessaging.javaClass.getMethod("getToken")
                val tokenTask = getTokenMethod.invoke(firebaseMessaging) as com.google.android.gms.tasks.Task<*>

                tokenTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result as? String
                        if (token != null) {
                            Log.d("FrogLife", "FCM Token obtained: ${token.take(20)}...")

                            // Upload token to GCS in background
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    service.uploadDeviceToken(token)
                                    Log.d("FrogLife", "FCM token uploaded to GCS")
                                } catch (e: Exception) {
                                    Log.e("FrogLife", "Failed to upload FCM token", e)
                                }
                            }
                        }
                    } else {
                        Log.w("FrogLife", "FCM not available (google-services.json missing or Firebase not configured)")
                        Log.w("FrogLife", "App will work without push notifications")
                    }
                }
            } catch (e: Exception) {
                // Firebase not configured - app will work without push notifications
                Log.w("FrogLife", "FCM not available: ${e.message}")
                Log.w("FrogLife", "App will work without push notifications. To enable FCM:")
                Log.w("FrogLife", "1. Download google-services.json from Firebase Console")
                Log.w("FrogLife", "2. Place it in app/ directory")
                Log.w("FrogLife", "3. Rebuild the app")
            }
        }

        setContent {
            FrogLifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route
                    ) {
                        composable(Screen.Main.route) {
                            MainScreen(navController = navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                frogViewModel = frogViewModel,
                                activityViewModel = activityViewModel,
                                rewardViewModel = rewardViewModel,
                                syncService = syncService
                            )
                        }
                        composable(Screen.ManageFrogs.route) {
                            ManageFrogsScreen(
                                navController = navController,
                                frogViewModel = frogViewModel,
                                rewardViewModel = rewardViewModel
                            )
                        }
                        composable(Screen.ManageActivities.route) {
                            ManageActivitiesScreen(
                                navController = navController,
                                viewModel = activityViewModel
                            )
                        }
                        composable(Screen.ManageRewards.route) {
                            ManageRewardsScreen(
                                navController = navController,
                                viewModel = rewardViewModel
                            )
                        }
                        composable(Screen.RewardHistory.route) {
                            RewardHistoryScreen(
                                navController = navController,
                                rewardViewModel = rewardViewModel,
                                frogViewModel = frogViewModel
                            )
                        }
                        composable(Screen.Calendar.route) {
                            CalendarScreen(
                                navController = navController,
                                frogViewModel = frogViewModel,
                                activityViewModel = activityViewModel,
                                calendarViewModel = calendarViewModel
                            )
                        }
                        composable(Screen.ViewFrog.route) { backStackEntry ->
                            val frogId = backStackEntry.arguments?.getString("frogId")?.toLongOrNull()
                            if (frogId != null) {
                                ViewFrogScreen(
                                    navController = navController,
                                    frogId = frogId,
                                    viewModel = frogViewModel,
                                    activity = this@MainActivity,
                                    rewardViewModel = rewardViewModel
                                )
                            }
                        }
                        composable(Screen.AddEditFrog.route) { backStackEntry ->
                            val frogIdStr = backStackEntry.arguments?.getString("frogId")
                            val frogId = if (frogIdStr != "new") frogIdStr?.toLongOrNull() else null
                            AddEditFrogScreen(
                                navController = navController,
                                frogId = frogId,
                                frogViewModel = frogViewModel,
                                activityViewModel = activityViewModel
                            )
                        }
                        composable(Screen.AddEditActivity.route) { backStackEntry ->
                            val activityIdStr = backStackEntry.arguments?.getString("activityId")
                            val activityId = if (activityIdStr != "new") activityIdStr?.toLongOrNull() else null
                            AddEditActivityScreen(
                                navController = navController,
                                activityId = activityId,
                                viewModel = activityViewModel
                            )
                        }
                        composable(Screen.AddEditReward.route) { backStackEntry ->
                            val rewardIdStr = backStackEntry.arguments?.getString("rewardId")
                            val rewardId = if (rewardIdStr != "new") rewardIdStr?.toLongOrNull() else null
                            AddEditRewardScreen(
                                navController = navController,
                                rewardId = rewardId,
                                viewModel = rewardViewModel
                            )
                        }
                        composable(Screen.PendingApprovals.route) {
                            syncService?.let {
                                PendingApprovalsScreen(
                                    navController = navController,
                                    syncService = it,
                                    frogViewModel = frogViewModel,
                                    activityViewModel = activityViewModel,
                                    rewardViewModel = rewardViewModel
                                )
                            }
                        }
                        composable(Screen.ImportFromBucket.route) {
                            syncService?.let {
                                ImportFromBucketScreen(
                                    navController = navController,
                                    syncService = it,
                                    frogViewModel = frogViewModel,
                                    activityViewModel = activityViewModel,
                                    rewardViewModel = rewardViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
