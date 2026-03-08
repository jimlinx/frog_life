package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialDateDao {
    @Query("SELECT * FROM special_dates WHERE frogId = :frogId ORDER BY date ASC")
    fun getSpecialDatesForFrog(frogId: Long): Flow<List<SpecialDate>>

    @Query("SELECT * FROM special_dates WHERE id = :id")
    fun getSpecialDateById(id: Long): Flow<SpecialDate?>

    @Insert
    suspend fun insert(specialDate: SpecialDate): Long

    @Update
    suspend fun update(specialDate: SpecialDate)

    @Delete
    suspend fun delete(specialDate: SpecialDate)

    @Query("SELECT * FROM special_dates WHERE date >= :startDate AND date <= :endDate")
    fun getSpecialDatesInRange(startDate: Long, endDate: Long): Flow<List<SpecialDate>>

    @Query("SELECT * FROM special_dates ORDER BY date ASC")
    fun getAllSpecialDates(): Flow<List<SpecialDate>>

    @Query("DELETE FROM special_dates")
    suspend fun deleteAll()
}
