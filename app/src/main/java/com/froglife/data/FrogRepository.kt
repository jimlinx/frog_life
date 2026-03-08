package com.froglife.data

import kotlinx.coroutines.flow.Flow

class FrogRepository(private val database: FrogDatabase) {
    private val frogDao = database.frogDao()
    private val activityDao = database.activityDao()
    private val frogActivityDao = database.frogActivityDao()
    private val activityLogDao = database.activityLogDao()
    private val settingsDao = database.settingsDao()
    private val specialDateDao = database.specialDateDao()
    private val dayCommentDao = database.dayCommentDao()
    private val rewardDao = database.rewardDao()
    private val rewardRedemptionDao = database.rewardRedemptionDao()

    // Frogs
    fun getAllFrogs(): Flow<List<Frog>> = frogDao.getAllFrogs()
    fun getFrogById(id: Long): Flow<Frog?> = frogDao.getFrogById(id)
    fun getLeaderFrog(): Flow<Frog?> = frogDao.getLeaderFrog()
    suspend fun insertFrog(frog: Frog): Long = frogDao.insert(frog)
    suspend fun updateFrog(frog: Frog) = frogDao.update(frog)
    suspend fun deleteFrog(frog: Frog) = frogDao.delete(frog)
    suspend fun updateFrogWealthPoints(frogId: Long, points: Int) = frogDao.updateWealthPoints(frogId, points)

    // Activities
    fun getAllActivities(): Flow<List<Activity>> = activityDao.getAllActivities()
    fun getActivityById(id: Long): Flow<Activity?> = activityDao.getActivityById(id)
    fun getActivitiesForFrog(frogId: Long): Flow<List<Activity>> = activityDao.getActivitiesForFrog(frogId)
    suspend fun insertActivity(activity: Activity): Long = activityDao.insert(activity)
    suspend fun updateActivity(activity: Activity) = activityDao.update(activity)
    suspend fun deleteActivity(activity: Activity) {
        // Cascade delete: remove all activity logs for this activity
        activityLogDao.deleteAllLogsForActivity(activity.id)
        activityDao.delete(activity)
    }

    // Frog-Activity Relations
    suspend fun attachActivityToFrog(frogId: Long, activityId: Long) {
        frogActivityDao.insert(FrogActivityCrossRef(frogId, activityId))
    }
    suspend fun detachActivityFromFrog(frogId: Long, activityId: Long) {
        frogActivityDao.deleteByIds(frogId, activityId)
    }
    fun getCrossRefsForFrog(frogId: Long): Flow<List<FrogActivityCrossRef>> =
        frogActivityDao.getCrossRefsForFrog(frogId)
    fun getAllCrossRefs(): Flow<List<FrogActivityCrossRef>> = frogActivityDao.getAllCrossRefs()

    // Activity Logs
    fun getLogsForFrogInDateRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<ActivityLog>> =
        activityLogDao.getLogsForFrogInDateRange(frogId, startDate, endDate)
    fun getLogForFrogActivityDate(frogId: Long, activityId: Long, startDate: Long, endDate: Long): Flow<ActivityLog?> =
        activityLogDao.getLogForFrogActivityDate(frogId, activityId, startDate, endDate)
    suspend fun insertActivityLog(log: ActivityLog): Long = activityLogDao.insert(log)
    suspend fun updateActivityLog(log: ActivityLog) = activityLogDao.update(log)
    suspend fun deleteActivityLog(log: ActivityLog) = activityLogDao.delete(log)
    fun getAllActivityLogs(): Flow<List<ActivityLog>> = activityLogDao.getAllLogs()

    // Settings
    fun getSettings(): Flow<AppSettings?> = settingsDao.getSettings()
    suspend fun insertSettings(settings: AppSettings) = settingsDao.insert(settings)
    suspend fun updateSettings(settings: AppSettings) = settingsDao.update(settings)

    // Special Dates
    fun getSpecialDatesForFrog(frogId: Long): Flow<List<SpecialDate>> = specialDateDao.getSpecialDatesForFrog(frogId)
    fun getSpecialDateById(id: Long): Flow<SpecialDate?> = specialDateDao.getSpecialDateById(id)
    fun getSpecialDatesInRange(startDate: Long, endDate: Long): Flow<List<SpecialDate>> =
        specialDateDao.getSpecialDatesInRange(startDate, endDate)
    suspend fun insertSpecialDate(specialDate: SpecialDate): Long = specialDateDao.insert(specialDate)
    suspend fun updateSpecialDate(specialDate: SpecialDate) = specialDateDao.update(specialDate)
    suspend fun deleteSpecialDate(specialDate: SpecialDate) = specialDateDao.delete(specialDate)
    fun getAllSpecialDates(): Flow<List<SpecialDate>> = specialDateDao.getAllSpecialDates()

    // Day Comments
    fun getCommentForDay(frogId: Long, date: Long): Flow<DayComment?> = dayCommentDao.getCommentForDay(frogId, date)
    fun getCommentsInRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<DayComment>> =
        dayCommentDao.getCommentsInRange(frogId, startDate, endDate)
    suspend fun insertDayComment(comment: DayComment): Long = dayCommentDao.insert(comment)
    suspend fun updateDayComment(comment: DayComment) = dayCommentDao.update(comment)
    suspend fun deleteDayComment(comment: DayComment) = dayCommentDao.delete(comment)
    fun getAllDayComments(): Flow<List<DayComment>> = dayCommentDao.getAllComments()

    // Rewards
    fun getAllRewards(): Flow<List<Reward>> = rewardDao.getAllRewards()
    fun getRewardById(id: Long): Flow<Reward?> = rewardDao.getRewardById(id)
    suspend fun insertReward(reward: Reward): Long = rewardDao.insert(reward)
    suspend fun updateReward(reward: Reward) = rewardDao.update(reward)
    suspend fun deleteReward(reward: Reward) {
        // Cascade delete: remove all redemptions for this reward
        rewardRedemptionDao.deleteAllRedemptionsForReward(reward.id)
        rewardDao.delete(reward)
    }

    // Reward Redemptions
    fun getRedemptionsForFrogInDateRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<RewardRedemption>> =
        rewardRedemptionDao.getRedemptionsForFrogInDateRange(frogId, startDate, endDate)
    fun getRedemptionsForFrog(frogId: Long): Flow<List<RewardRedemption>> =
        rewardRedemptionDao.getRedemptionsForFrog(frogId)
    fun getAllRedemptions(): Flow<List<RewardRedemption>> = rewardRedemptionDao.getAllRedemptions()
    fun getRedemptionById(id: Long): Flow<RewardRedemption?> = rewardRedemptionDao.getRedemptionById(id)
    suspend fun insertRedemption(redemption: RewardRedemption): Long = rewardRedemptionDao.insert(redemption)
    suspend fun updateRedemption(redemption: RewardRedemption) = rewardRedemptionDao.update(redemption)
    suspend fun deleteRedemption(redemption: RewardRedemption) = rewardRedemptionDao.delete(redemption)

    // Clear all data (for import)
    suspend fun clearAllData() {
        // Delete in reverse dependency order
        dayCommentDao.deleteAll()
        specialDateDao.deleteAll()
        activityLogDao.deleteAll()
        rewardRedemptionDao.deleteAll()
        frogActivityDao.deleteAll()
        activityDao.deleteAll()
        rewardDao.deleteAll()
        frogDao.deleteAll()
    }
}
