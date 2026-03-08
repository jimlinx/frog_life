package com.froglife.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = Frog::class,
            parentColumns = ["id"],
            childColumns = ["frogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("frogId"), Index("activityId"), Index("date")]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frogId: Long,
    val activityId: Long,
    val date: Long,  // Date in milliseconds
    val value: String,  // "true"/"false" for BOOLEAN, number string for INTEGER
    val pointsEarned: Int = 0  // Calculated wealth points for this log entry
)
