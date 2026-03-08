package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FrogActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: FrogActivityCrossRef)

    @Delete
    suspend fun delete(crossRef: FrogActivityCrossRef)

    @Query("DELETE FROM frog_activity_cross_ref WHERE frogId = :frogId AND activityId = :activityId")
    suspend fun deleteByIds(frogId: Long, activityId: Long)

    @Query("SELECT * FROM frog_activity_cross_ref WHERE frogId = :frogId")
    fun getCrossRefsForFrog(frogId: Long): Flow<List<FrogActivityCrossRef>>

    @Query("SELECT * FROM frog_activity_cross_ref")
    fun getAllCrossRefs(): Flow<List<FrogActivityCrossRef>>

    @Query("DELETE FROM frog_activity_cross_ref")
    suspend fun deleteAll()
}
