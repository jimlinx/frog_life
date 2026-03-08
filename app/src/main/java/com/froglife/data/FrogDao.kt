package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FrogDao {
    @Query("SELECT * FROM frogs ORDER BY wealthPoints DESC")
    fun getAllFrogs(): Flow<List<Frog>>

    @Query("SELECT * FROM frogs WHERE id = :frogId")
    fun getFrogById(frogId: Long): Flow<Frog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(frog: Frog): Long

    @Update
    suspend fun update(frog: Frog)

    @Delete
    suspend fun delete(frog: Frog)

    @Query("UPDATE frogs SET wealthPoints = :points WHERE id = :frogId")
    suspend fun updateWealthPoints(frogId: Long, points: Int)

    @Query("SELECT * FROM frogs ORDER BY wealthPoints DESC LIMIT 1")
    fun getLeaderFrog(): Flow<Frog?>

    @Query("DELETE FROM frogs")
    suspend fun deleteAll()
}
