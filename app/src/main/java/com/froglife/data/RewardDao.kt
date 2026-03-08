package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards ORDER BY name")
    fun getAllRewards(): Flow<List<Reward>>

    @Query("SELECT * FROM rewards WHERE id = :rewardId")
    fun getRewardById(rewardId: Long): Flow<Reward?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: Reward): Long

    @Update
    suspend fun update(reward: Reward)

    @Delete
    suspend fun delete(reward: Reward)

    @Query("DELETE FROM rewards")
    suspend fun deleteAll()
}
