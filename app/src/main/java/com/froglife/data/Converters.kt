package com.froglife.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFrogStatus(value: FrogStatus): String {
        return value.name
    }

    @TypeConverter
    fun toFrogStatus(value: String): FrogStatus {
        return FrogStatus.valueOf(value)
    }

    @TypeConverter
    fun fromActivityType(value: ActivityType): String {
        return value.name
    }

    @TypeConverter
    fun toActivityType(value: String): ActivityType {
        return ActivityType.valueOf(value)
    }
}
