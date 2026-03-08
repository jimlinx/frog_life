package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DayCommentDao {
    @Query("SELECT * FROM day_comments WHERE frogId = :frogId AND date = :date")
    fun getCommentForDay(frogId: Long, date: Long): Flow<DayComment?>

    @Query("SELECT * FROM day_comments WHERE frogId = :frogId AND date >= :startDate AND date <= :endDate")
    fun getCommentsInRange(frogId: Long, startDate: Long, endDate: Long): Flow<List<DayComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: DayComment): Long

    @Update
    suspend fun update(comment: DayComment)

    @Delete
    suspend fun delete(comment: DayComment)

    @Query("DELETE FROM day_comments WHERE frogId = :frogId AND date = :date")
    suspend fun deleteByFrogAndDate(frogId: Long, date: Long)

    @Query("SELECT * FROM day_comments ORDER BY date DESC")
    fun getAllComments(): Flow<List<DayComment>>

    @Query("DELETE FROM day_comments")
    suspend fun deleteAll()
}
