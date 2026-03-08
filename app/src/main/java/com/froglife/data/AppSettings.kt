package com.froglife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,  // Only one settings record
    val rockThreshold: Int = 10,
    val copperThreshold: Int = 50,
    val bronzeThreshold: Int = 100,
    val silverThreshold: Int = 200,
    val goldThreshold: Int = 400,
    val diamondThreshold: Int = 800,
    val lastMonthlyWinsProcessed: String? = null  // Last month we processed wins (format: "YYYY-MM")
)
