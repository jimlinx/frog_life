package com.froglife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frogs")
data class Frog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val wealthPoints: Int = 10,
    val status: FrogStatus = FrogStatus.ROCK,
    val profilePicturePath: String? = null,  // Path to custom image or preset identifier
    val description: String = "",
    val isPresetIcon: Boolean = true,  // True if using preset anime frog icon
    val presetIconId: Int = 0,  // ID of preset icon (0-4 for 5 different frogs)
    val currentMonthPoints: Int = 0,  // Points earned in current month
    val monthlyWins: Int = 0,  // Number of months this frog has been the winner
    val lastMonthWinRecorded: String? = null  // Last month we recorded a win (format: "YYYY-MM")
)
