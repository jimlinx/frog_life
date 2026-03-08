package com.froglife.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reward_redemptions",
    foreignKeys = [
        ForeignKey(
            entity = Frog::class,
            parentColumns = ["id"],
            childColumns = ["frogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Reward::class,
            parentColumns = ["id"],
            childColumns = ["rewardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("frogId"), Index("rewardId"), Index("date")]
)
data class RewardRedemption(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frogId: Long,
    val rewardId: Long,
    val date: Long,  // Date in milliseconds
    val pointsUsed: Int = 0  // Points spent for this redemption
)
