package com.froglife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "special_dates")
data class SpecialDate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frogId: Long,
    val description: String,
    val date: Long, // Timestamp in milliseconds
    val createdAt: Long = System.currentTimeMillis()
)
