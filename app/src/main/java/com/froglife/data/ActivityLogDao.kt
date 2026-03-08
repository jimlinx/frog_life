package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE frogId = :frogId AND date >= :startDate AND date <= :endDate ORDER BY date")
    fun getLogsForFrogInDateRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE frogId = :frogId AND activityId = :activityId AND date >= :startDate AND date <= :endDate")
    fun getLogForFrogActivityDate(frogId: Long, activityId: Long, startDate: Long, endDate: Long): Flow<ActivityLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLog): Long

    @Update
    suspend fun update(log: ActivityLog)

    @Delete
    suspend fun delete(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE frogId = :frogId AND activityId = :activityId AND date >= :startDate AND date <= :endDate")
    suspend fun deleteLogForDate(frogId: Long, activityId: Long, startDate: Long, endDate: Long)

    @Query("SELECT * FROM activity_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("DELETE FROM activity_logs WHERE activityId = :activityId")
    suspend fun deleteAllLogsForActivity(activityId: Long)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAll()
}
