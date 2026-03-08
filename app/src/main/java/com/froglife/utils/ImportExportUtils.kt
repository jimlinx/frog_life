package com.froglife.utils

import com.froglife.data.FrogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Import data from repository (for background services without ViewModels)
 */
suspend fun importAllData(
    repository: FrogRepository,
    data: ExportData
) {
    withContext(Dispatchers.IO) {
        // Clear all existing data
        repository.clearAllData()

        // Import settings
        data.settings?.let { repository.insertSettings(it) }

        // Import frogs (map old IDs to new IDs)
        val frogIdMap = mutableMapOf<Long, Long>()
        data.frogs.forEach { frog ->
            val oldId = frog.id
            val newId = repository.insertFrog(frog.copy(id = 0))
            frogIdMap[oldId] = newId
        }

        // Import activities (map old IDs to new IDs)
        val activityIdMap = mutableMapOf<Long, Long>()
        data.activities.forEach { activity ->
            val oldId = activity.id
            val newId = repository.insertActivity(activity.copy(id = 0))
            activityIdMap[oldId] = newId
        }

        // Import rewards (map old IDs to new IDs)
        val rewardIdMap = mutableMapOf<Long, Long>()
        data.rewards.forEach { reward ->
            val oldId = reward.id
            val newId = repository.insertReward(reward.copy(id = 0))
            rewardIdMap[oldId] = newId
        }

        // Import frog-activity cross-references using new IDs
        data.frogActivityRefs.forEach { crossRef ->
            val newFrogId = frogIdMap[crossRef.frogId]
            val newActivityId = activityIdMap[crossRef.activityId]
            if (newFrogId != null && newActivityId != null) {
                repository.attachActivityToFrog(newFrogId, newActivityId)
            }
        }

        // Import activity logs using new IDs
        data.activityLogs.forEach { log ->
            val newFrogId = frogIdMap[log.frogId]
            val newActivityId = activityIdMap[log.activityId]
            if (newFrogId != null && newActivityId != null) {
                repository.insertActivityLog(log.copy(id = 0, frogId = newFrogId, activityId = newActivityId))
            }
        }

        // Import special dates using new frog IDs
        data.specialDates.forEach { specialDate ->
            val newFrogId = frogIdMap[specialDate.frogId]
            if (newFrogId != null) {
                repository.insertSpecialDate(specialDate.copy(id = 0, frogId = newFrogId))
            }
        }

        // Import day comments
        data.dayComments.forEach { dayComment ->
            repository.insertDayComment(dayComment.copy(id = 0))
        }

        // Import reward redemptions using new frog and reward IDs
        data.rewardRedemptions.forEach { redemption ->
            val newFrogId = frogIdMap[redemption.frogId]
            val newRewardId = rewardIdMap[redemption.rewardId]
            if (newFrogId != null && newRewardId != null) {
                repository.insertRedemption(redemption.copy(id = 0, frogId = newFrogId, rewardId = newRewardId))
            }
        }
    }
}
