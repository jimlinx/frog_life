package com.froglife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: Long,  // Color as Long (ARGB)
    val pointsCost: Int = 0  // Points required to redeem this reward
)
