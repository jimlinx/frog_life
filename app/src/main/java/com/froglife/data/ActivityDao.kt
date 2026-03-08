package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY name")
    fun getAllActivities(): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityById(activityId: Long): Flow<Activity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: Activity): Long

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

    @Query("""
        SELECT activities.* FROM activities
        INNER JOIN frog_activity_cross_ref ON activities.id = frog_activity_cross_ref.activityId
        WHERE frog_activity_cross_ref.frogId = :frogId
        ORDER BY activities.name
    """)
    fun getActivitiesForFrog(frogId: Long): Flow<List<Activity>>

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}
