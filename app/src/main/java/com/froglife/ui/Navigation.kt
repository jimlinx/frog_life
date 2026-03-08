package com.froglife.ui

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object ManageFrogs : Screen("manage_frogs")
    object ManageActivities : Screen("manage_activities")
    object ManageRewards : Screen("manage_rewards")
    object RewardHistory : Screen("reward_history")
    object Calendar : Screen("calendar")
    object ViewFrog : Screen("view_frog/{frogId}") {
        fun createRoute(frogId: Long) = "view_frog/$frogId"
    }
    object AddEditFrog : Screen("add_edit_frog/{frogId}") {
        fun createRoute(frogId: Long?) = "add_edit_frog/${frogId ?: "new"}"
    }
    object AddEditActivity : Screen("add_edit_activity/{activityId}") {
        fun createRoute(activityId: Long?) = "add_edit_activity/${activityId ?: "new"}"
    }
    object AddEditReward : Screen("add_edit_reward/{rewardId}") {
        fun createRoute(rewardId: Long?) = "add_edit_reward/${rewardId ?: "new"}"
    }
    object PendingApprovals : Screen("pending_approvals")
    object ImportFromBucket : Screen("import_from_bucket")
}
