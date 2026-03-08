package com.froglife.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings)

    @Update
    suspend fun update(settings: AppSettings)
}
