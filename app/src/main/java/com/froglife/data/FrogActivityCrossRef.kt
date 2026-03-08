package com.froglife.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "frog_activity_cross_ref",
    primaryKeys = ["frogId", "activityId"],
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
    indices = [Index("activityId")]
)
data class FrogActivityCrossRef(
    val frogId: Long,
    val activityId: Long
)
