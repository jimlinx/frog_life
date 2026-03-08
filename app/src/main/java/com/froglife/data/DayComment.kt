package com.froglife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_comments")
data class DayComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frogId: Long,
    val date: Long, // Start of day timestamp
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)
