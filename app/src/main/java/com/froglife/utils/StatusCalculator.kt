package com.froglife.utils

import com.froglife.data.AppSettings
import com.froglife.data.FrogStatus

object StatusCalculator {
    fun calculateStatus(wealthPoints: Int, settings: AppSettings): FrogStatus {
        return when {
            wealthPoints >= settings.diamondThreshold -> FrogStatus.DIAMOND
            wealthPoints >= settings.goldThreshold -> FrogStatus.GOLD
            wealthPoints >= settings.silverThreshold -> FrogStatus.SILVER
            wealthPoints >= settings.bronzeThreshold -> FrogStatus.BRONZE
            wealthPoints >= settings.copperThreshold -> FrogStatus.COPPER
            else -> FrogStatus.ROCK
        }
    }
}
