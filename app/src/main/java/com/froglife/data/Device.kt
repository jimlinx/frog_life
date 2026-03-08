package com.froglife.data

import java.util.UUID

data class Device(
    val deviceId: String = UUID.randomUUID().toString(),
    val name: String,
    val type: DeviceType,
    val lastSeen: Long = System.currentTimeMillis()
)

enum class DeviceType {
    MASTER, SLAVE
}
