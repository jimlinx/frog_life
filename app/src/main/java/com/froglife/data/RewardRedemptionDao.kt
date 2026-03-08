package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardRedemptionDao {
    @Query("SELECT * FROM reward_redemptions WHERE frogId = :frogId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getRedemptionsForFrogInDateRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<RewardRedemption>>

    @Query("SELECT * FROM reward_redemptions WHERE frogId = :frogId ORDER BY date DESC")
    fun getRedemptionsForFrog(frogId: Long): Flow<List<RewardRedemption>>

    @Query("SELECT * FROM reward_redemptions ORDER BY date DESC")
    fun getAllRedemptions(): Flow<List<RewardRedemption>>

    @Query("SELECT * FROM reward_redemptions WHERE id = :redemptionId")
    fun getRedemptionById(redemptionId: Long): Flow<RewardRedemption?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(redemption: RewardRedemption): Long

    @Update
    suspend fun update(redemption: RewardRedemption)

    @Delete
    suspend fun delete(redemption: RewardRedemption)

    @Query("DELETE FROM reward_redemptions WHERE rewardId = :rewardId")
    suspend fun deleteAllRedemptionsForReward(rewardId: Long)

    @Query("DELETE FROM reward_redemptions")
    suspend fun deleteAll()
}
