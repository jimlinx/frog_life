package com.froglife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: Long,  // Color as Long (ARGB)
    val type: ActivityType,
    val defaultValue: String = "false",  // "true"/"false" for BOOLEAN, number string for INTEGER
    val wealthAmount: Int = 0  // Positive or negative points
)
