package com.froglife

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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

class MainActivity : FragmentActivity() {
    private lateinit var repository: FrogRepository
    private lateinit var frogViewModel: FrogViewModel
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var rewardViewModel: RewardViewModel
    private var syncService: GCSSyncService? = null

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
        } catch (e: Exception) {
            // Sync service not available (credentials not found)
            syncService = null
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
