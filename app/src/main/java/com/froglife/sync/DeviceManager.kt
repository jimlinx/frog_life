package com.froglife.sync

import android.content.Context
import com.froglife.data.DeviceType
import java.util.UUID

class DeviceManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)

    fun getDeviceId(): String {
        return prefs.getString("device_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
    }

    fun getDeviceType(): DeviceType {
        val type = prefs.getString("device_type", "SLAVE")
        return DeviceType.valueOf(type!!)
    }

    fun setDeviceType(type: DeviceType) {
        prefs.edit().putString("device_type", type.name).apply()
    }

    fun getDeviceName(): String {
        return prefs.getString("device_name", android.os.Build.MODEL) ?: android.os.Build.MODEL
    }

    fun setDeviceName(name: String) {
        prefs.edit().putString("device_name", name).apply()
    }
}
