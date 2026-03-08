package com.froglife.utils

import com.froglife.data.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder

data class ExportData(
    val frogs: List<Frog>,
    val activities: List<Activity>,
    val frogActivityRefs: List<FrogActivityCrossRef>,
    val activityLogs: List<ActivityLog>,
    val specialDates: List<SpecialDate>,
    val dayComments: List<DayComment>,
    val rewards: List<Reward>,
    val rewardRedemptions: List<RewardRedemption>,
    val settings: AppSettings?
)

object DataExporter {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJson(data: ExportData): String {
        return gson.toJson(data)
    }

    fun importFromJson(json: String): ExportData {
        return gson.fromJson(json, ExportData::class.java)
    }
}
